/*
 *
 *  Copyright (c) 2018 the original author or authors.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.github.krr.nats.config;

import com.github.krr.nats.annotations.NatsHandler;
import com.github.krr.nats.annotations.NatsListener;
import com.github.krr.nats.annotations.NatsListeners;
import com.github.krr.nats.listeners.AbstractNatsEndpointListenerContainer;
import com.github.krr.nats.listeners.NatsEndpointListener;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.*;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.expression.StandardBeanExpressionResolver;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.krr.nats.annotations.NatsListener.DEFAULT_NATS_SERVER_CONNECTION_FACTORY_BEAN_NAME;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON;

/**
 * <Description>
 *
 * @author raghavan
 */
@Slf4j
public class NatsListenerAnnotationBeanPostProcessor implements BeanPostProcessor, Ordered, BeanFactoryAware,
                                                                ApplicationListener<ContextRefreshedEvent>,
                                                                DisposableBean {

  public static final String DEFAULT_NATS_MESSAGE_CONVERTERS_BEAN_NAME = "natsMessageConverters";

  private static final Logger LOGGER = LoggerFactory.getLogger(NatsListenerAnnotationBeanPostProcessor.class);

  @SuppressWarnings("WeakerAccess")
  public static final String NATS_ENDPOINT_LISTENER_REGISTRY_BEAN = "com.github.krr.nats.config.ENDPOINT_LISTENER_REGISTRY";

  private NatsListenerEndpointRegistry endpointRegistry = new NatsListenerEndpointRegistry();

  private final Set<Class<?>> nonAnnotatedClasses = Collections.newSetFromMap(new ConcurrentHashMap<>(64));

  private BeanFactory beanFactory;

  private BeanExpressionResolver resolver = new StandardBeanExpressionResolver();

  private BeanExpressionContext expressionContext;

  @Override
  public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
    this.beanFactory = beanFactory;
    if (beanFactory instanceof ConfigurableListableBeanFactory) {
      ConfigurableListableBeanFactory configurableListableBeanFactory = (ConfigurableListableBeanFactory) beanFactory;
      this.resolver = configurableListableBeanFactory.getBeanExpressionResolver();
      configurableListableBeanFactory.registerSingleton(NATS_ENDPOINT_LISTENER_REGISTRY_BEAN, endpointRegistry);
      this.expressionContext = new BeanExpressionContext(configurableListableBeanFactory,
                                                         configurableListableBeanFactory
                                                             .getRegisteredScope(SCOPE_SINGLETON));
    }
  }

  @Override
  public int getOrder() {
    return LOWEST_PRECEDENCE;
  }

  @Override
  public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
    return bean;
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    if (!this.nonAnnotatedClasses.contains(bean.getClass())) {
      Class<?> targetClass = AopUtils.getTargetClass(bean);
      Collection<NatsListener> classLevelListeners = findListenerAnnotations(targetClass);
      final boolean hasClassLevelListeners = classLevelListeners.size() > 0;
      final List<Method> multiMethods = new ArrayList<>();
      Map<Method, Set<NatsListener>> annotatedMethods = MethodIntrospector.selectMethods(targetClass,
                                                                                         (MethodIntrospector.MetadataLookup<Set<NatsListener>>) method -> {
                                                                                           Set<NatsListener> listenerMethods = findListenerAnnotations(
                                                                                               method);
                                                                                           return (!listenerMethods
                                                                                               .isEmpty() ? listenerMethods : null);
                                                                                         });
      if (hasClassLevelListeners) {
        Set<Method> methodsWithHandler = MethodIntrospector.selectMethods(targetClass,
                                                                          (ReflectionUtils.MethodFilter) method ->
                                                                              AnnotationUtils.findAnnotation(method,
                                                                                                             NatsHandler.class) != null);
        multiMethods.addAll(methodsWithHandler);
      }
      if (annotatedMethods.isEmpty()) {
        this.nonAnnotatedClasses.add(bean.getClass());
        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace("No @NatsListener annotations found on bean type: " + bean.getClass());
        }
      }
      else {
        // Non-empty set of methods
        for (Map.Entry<Method, Set<NatsListener>> entry : annotatedMethods.entrySet()) {
          Method method = entry.getKey();
          for (NatsListener listener : entry.getValue()) {
            processNatsListener(listener, method, bean, beanName);
          }
        }
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug(annotatedMethods.size() + " @NatsListener methods processed on bean '"
                       + beanName + "': " + annotatedMethods);
        }
      }
      if (hasClassLevelListeners) {
        processMultiMethodListeners(classLevelListeners, multiMethods, bean, beanName);
      }
    }
    return bean;

  }

  private Collection<NatsListener> findListenerAnnotations(Class<?> clazz) {
    Set<NatsListener> listeners = new HashSet<>();
    NatsListener ann = AnnotationUtils.findAnnotation(clazz, NatsListener.class);
    if (ann != null) {
      listeners.add(ann);
    }
    NatsListeners anns = AnnotationUtils.findAnnotation(clazz, NatsListeners.class);
    if (anns != null) {
      listeners.addAll(Arrays.asList(anns.value()));
    }
    return listeners;
  }

  /*
   * AnnotationUtils.getRepeatableAnnotations does not look at interfaces
   */
  private Set<NatsListener> findListenerAnnotations(Method method) {
    Set<NatsListener> listeners = new HashSet<>();
    NatsListener ann = AnnotatedElementUtils.findMergedAnnotation(method, NatsListener.class);
    if (ann != null) {
      listeners.add(ann);
    }
    NatsListeners anns = AnnotationUtils.findAnnotation(method, NatsListeners.class);
    if (anns != null) {
      listeners.addAll(Arrays.asList(anns.value()));
    }
    return listeners;
  }

  @SuppressWarnings("unchecked")
  private void processNatsListener(NatsListener natsListener, Method method, Object bean, String beanName) {
    Method methodToUse = checkProxy(method, bean);
    NatsEndpointListener endpointListener = new NatsEndpointListener(method, natsListener, bean, beanName);
    endpointListener.setBeanFactory(this.beanFactory);
    String containerFactoryBeanName = resolveExpressionAsString(natsListener.natsConnectionFactory(),
                                                                DEFAULT_NATS_SERVER_CONNECTION_FACTORY_BEAN_NAME);

//    List<NatsMessageConverter> converters = getNatsMessageConverters();
//    if(CollectionUtils.isEmpty(converters)) {
//      throw new IllegalStateException("No converters registered for serializing or deserializing messages."  +
//                                      "Expecting List<NatsMessageConverter> types in application context.");
//    }
    if (StringUtils.hasText(containerFactoryBeanName)) {
      AbstractNatsEndpointListenerContainer container = natsListener.type().listenerContainer(natsListener,
                                                                                              bean, methodToUse);
      endpointRegistry.registerListener(natsListener, container);
      if (beanFactory instanceof ConfigurableListableBeanFactory) {
        ((ConfigurableListableBeanFactory) beanFactory).registerSingleton(container.getClientName(), container);
      }
    }
    else {
      throw new IllegalStateException("No container factory bean with name " + containerFactoryBeanName +
                                      " exists in application context");
    }
  }

  private String resolveExpressionAsString(String value, String attribute) {
    Object resolved = resolveExpression(value);
    if (resolved instanceof String) {
      return (String) resolved;
    }
    else {
      throw new IllegalStateException("The [" + attribute + "] must resolve to a String. "
                                      + "Resolved to [" + resolved.getClass() + "] for [" + value + "]");
    }
  }

  private Object resolveExpression(String value) {
    String resolvedValue = resolve(value);

    return this.resolver.evaluate(resolvedValue, this.expressionContext);
  }

  /**
   * Resolve the specified value if possible.
   *
   * @param value the value to resolve
   * @return the resolved value
   * @see ConfigurableBeanFactory#resolveEmbeddedValue
   */
  private String resolve(String value) {
    if (this.beanFactory instanceof ConfigurableBeanFactory) {
      return ((ConfigurableBeanFactory) this.beanFactory).resolveEmbeddedValue(value);
    }
    return value;
  }

  private void processMultiMethodListeners(Collection<NatsListener> classLevelListeners, List<Method> multiMethods,
                                           Object bean, String beanName) {
//    List<Method> checkedMethods = new ArrayList<Method>();
//    Method defaultMethod = null;
//    for (Method method : multiMethods) {
//      Method checked = checkProxy(method, bean);
//      if (AnnotationUtils.findAnnotation(method, KafkaHandler.class).isDefault()) { // NOSONAR never null
//        final Method toAssert = defaultMethod;
//        Assert.state(toAssert == null, () -> "Only one @KafkaHandler can be marked 'isDefault', found: "
//                                             + toAssert.toString() + " and " + method.toString());
//        defaultMethod = checked;
//      }
//      checkedMethods.add(checked);
//    }
//    for (KafkaListener classLevelListener : classLevelListeners) {
//      MultiMethodKafkaListenerEndpoint<K, V> endpoint =
//          new MultiMethodKafkaListenerEndpoint<K, V>(checkedMethods, defaultMethod, bean);
//      endpoint.setBeanFactory(this.beanFactory);
//      processListener(endpoint, classLevelListener, bean, bean.getClass(), beanName);
//    }
  }

  private Method checkProxy(Method methodArg, Object bean) {
    Method method = methodArg;
    if (AopUtils.isJdkDynamicProxy(bean)) {
      try {
        // Found a @NatsListener method on the target class for this JDK proxy ->
        // is it also present on the proxy itself?
        method = bean.getClass().getMethod(method.getName(), method.getParameterTypes());
        Class<?>[] proxiedInterfaces = ((Advised) bean).getProxiedInterfaces();
        for (Class<?> iface : proxiedInterfaces) {
          try {
            method = iface.getMethod(method.getName(), method.getParameterTypes());
            break;
          }
          catch (NoSuchMethodException noMethod) {
            //
          }
        }
      }
      catch (SecurityException ex) {
        ReflectionUtils.handleReflectionException(ex);
      }
      catch (NoSuchMethodException ex) {
        throw new IllegalStateException(String.format(
            "@NatsListener method '%s' found on bean target class '%s', " +
            "but not found in any interface(s) for bean JDK proxy. Either " +
            "pull the method up to an interface or switch to subclass (CGLIB) " +
            "proxies by setting proxy-target-class/proxyTargetClass " +
            "attribute to 'true'", method.getName(), method.getDeclaringClass().getSimpleName()), ex);
      }
    }
    return method;
  }

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    // after the context is refreshed - start all the listeners.
    log.error("Starting all listeners");
    endpointRegistry.getNatsListenerMap().forEach((key, value) -> {
      value.forEach(v -> {
        if (v instanceof AbstractNatsEndpointListenerContainer) {
          AbstractNatsEndpointListenerContainer v1 = (AbstractNatsEndpointListenerContainer) v;
          log.error("Starting listener {}", v1.getClientName());
          v1.onApplicationEvent(event);
        }
      });
    });
  }

  @Override
  public void destroy() {
    log.error("Shutting down all listeners");
    endpointRegistry.getNatsListenerMap().forEach((key, value) -> {
      log.error("Shutting down listener for {}", key);
      value.forEach(v -> {
        if (v instanceof AbstractNatsEndpointListenerContainer) {
          AbstractNatsEndpointListenerContainer v1 = (AbstractNatsEndpointListenerContainer) v;
          log.error("Shutting down {}", v1.getClientName());
          try {
            v1.destroy();
          }
          catch (Exception e) {
            log.error("Error shutting down listener {}", v1.getClientName(), e);
          }
        }
      });
    });
  }
}


