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

package com.github.krr.nats.support;

import com.github.krr.nats.annotations.NatsHandler;
import com.github.krr.nats.annotations.NatsListener;
import com.github.krr.nats.annotations.NatsListeners;
import com.github.krr.nats.config.NatsListenerEndpointRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <Description>
 *
 * @author raghavan
 */
public class NatsListenerAnnotationBeanPostProcessor implements BeanPostProcessor, Ordered, BeanFactoryAware,
                                                                SmartInitializingSingleton {

  private static final Logger LOGGER = LoggerFactory.getLogger(NatsListenerAnnotationBeanPostProcessor.class);

  private NatsListenerEndpointRegistry endpointRegistry;

  private final Set<Class<?>> nonAnnotatedClasses = Collections.newSetFromMap(new ConcurrentHashMap<>(64));
  @Override
  public void setBeanFactory(BeanFactory beanFactory) throws BeansException {

  }

  @Override
  public void afterSingletonsInstantiated() {

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
                                                                                           Set<NatsListener> listenerMethods = findListenerAnnotations(method);
                                                                                           return (!listenerMethods.isEmpty() ? listenerMethods : null);
                                                                                         });
      if (hasClassLevelListeners) {
        Set<Method> methodsWithHandler = MethodIntrospector.selectMethods(targetClass,
                                                                          (ReflectionUtils.MethodFilter) method ->
                                                                              AnnotationUtils.findAnnotation(method, NatsHandler.class) != null);
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

  public void setEndpointRegistry(NatsListenerEndpointRegistry endpointRegistry) {
    this.endpointRegistry = endpointRegistry;
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

  protected void processNatsListener(NatsListener natsListener, Method method, Object bean, String beanName) {
//    Method methodToUse = checkProxy(method, bean);
//    MethodKafkaListenerEndpoint<K, V> endpoint = new MethodKafkaListenerEndpoint<K, V>();
//    endpoint.setMethod(methodToUse);
//    endpoint.setBeanFactory(this.beanFactory);
//    String errorHandlerBeanName = resolveExpressionAsString(natsListener.errorHandler(), "errorHandler");
//    if (StringUtils.hasText(errorHandlerBeanName)) {
//      endpoint.setErrorHandler(this.beanFactory.getBean(errorHandlerBeanName, KafkaListenerErrorHandler.class));
//    }
//    processListener(endpoint, natsListener, bean, methodToUse, beanName);
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
        // Found a @KafkaListener method on the target class for this JDK proxy ->
        // is it also present on the proxy itself?
        method = bean.getClass().getMethod(method.getName(), method.getParameterTypes());
        Class<?>[] proxiedInterfaces = ((Advised) bean).getProxiedInterfaces();
        for (Class<?> iface : proxiedInterfaces) {
          try {
            method = iface.getMethod(method.getName(), method.getParameterTypes());
            break;
          }
          catch (NoSuchMethodException noMethod) {
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

}


