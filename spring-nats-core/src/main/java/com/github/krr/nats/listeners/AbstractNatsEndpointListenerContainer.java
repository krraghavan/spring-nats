package com.github.krr.nats.listeners;

import com.github.krr.nats.annotations.NatsListener;
import com.github.krr.nats.decorators.NatsMessage;
import com.github.krr.nats.exceptions.MessageConversionException;
import com.github.krr.nats.interfaces.NatsConnectionFactory;
import com.github.krr.nats.interfaces.NatsEndpointListenerContainer;
import com.github.krr.nats.interfaces.NatsMessageConverter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.krr.nats.config.NatsListenerAnnotationBeanPostProcessor.DEFAULT_NATS_MESSAGE_CONVERTERS_BEAN_NAME;

@SuppressWarnings("WeakerAccess")
@Slf4j
@Data
public abstract class AbstractNatsEndpointListenerContainer implements NatsEndpointListenerContainer,
                                                                       ApplicationListener<ContextRefreshedEvent>,
                                                                       ApplicationContextAware,
                                                                       DisposableBean {

  public static final String DEFAULT_NATS_CONNECTION_FACTORY_BEAN_NAME = "natsServerConnectionFactory";

  protected final NatsListener natsListener;

  private NatsConnectionFactory connectionFactory;

  protected final Object bean;

  protected final Method method;

  private List<NatsMessageConverter> converters;

  protected ApplicationContext applicationContext;

  private static final AtomicInteger CLIENT_COUNT = new AtomicInteger(0);

  protected String clientName;

  protected AbstractNatsEndpointListenerContainer(NatsListener natsListener, Object bean, Method method) {
    this.natsListener = natsListener;
    this.method = method;
    this.bean = bean;
    createClientName(natsListener);
  }

  private void createClientName(NatsListener natsListener) {
    if (StringUtils.isEmpty(natsListener.clientId())) {
      // no client id specified - get default name.
      this.clientName = getDefaultClientName();
      return;
    }
    clientName = natsListener.clientId();
  }

  private String getDefaultClientName() {
    return method.getDeclaringClass().getSimpleName().concat("-")
                 .concat(method.getName())
                 .concat("-listener-")
                 .concat(String.valueOf(CLIENT_COUNT.getAndIncrement()));
  }

  @SuppressWarnings("unchecked")
  protected Object[] getMethodArgs(Method method, NatsMessage msg) {
    Class<?>[] parameterTypes = method.getParameterTypes();
    if (parameterTypes.length > 1) {
      throw new IllegalArgumentException("Nats subscription methods can only have a single argument");
    }
    if (parameterTypes.length == 1) {
      for (NatsMessageConverter converter : getNatsMessageConverters()) {
        if (converter.supports(parameterTypes[0])) {
          try {
            return new Object[]{converter.fromBytes(msg.getData(), parameterTypes[0])};
          }
          catch (MessageConversionException e) {
            throw new IllegalStateException("Error converting byte [] to " + parameterTypes[0] + " using " +
                                            converter.getClass().getName());
          }
        }
      }
      throw new IllegalStateException("No converter found that converts byte [] to " + parameterTypes[0]);

    }
    return new Object[0];
  }

  @SuppressWarnings("unchecked")
  protected List<NatsMessageConverter> getNatsMessageConverters() {
    String converterBeanName = getConverterBeanName();
    if (converters == null) {
      return (List<NatsMessageConverter>) applicationContext.getBean(converterBeanName);
    }
    throw new IllegalStateException(
        "No bean with name 'natsMessageConverters' or specified by the 'natsConverterBeanName' " +
        "spring bean available in application context");
  }

  private String getConverterBeanName() {

    try {
      return applicationContext.getBean("natsConverterBeanName", String.class);
    }
    catch (NoSuchBeanDefinitionException e) {
      // this is ok since this is an optional bean
    }
    catch (Exception e) {
      throw new IllegalStateException(e);
    }
    return DEFAULT_NATS_MESSAGE_CONVERTERS_BEAN_NAME;
  }

  private String getConnectionFactoryBeanName() {

    try {
      return applicationContext.getBean("natsConnectionFactoryBeanName", String.class);
    }
    catch (NoSuchBeanDefinitionException e) {
      // this is ok since this is an optional bean
    }
    catch (Exception e) {
      throw new IllegalStateException(e);
    }
    return DEFAULT_NATS_CONNECTION_FACTORY_BEAN_NAME;
  }

  protected synchronized NatsConnectionFactory getConnectionFactory() {
    String connectionFactoryBeanName = getConnectionFactoryBeanName();
    if (connectionFactory == null) {
      connectionFactory = applicationContext.getBean(connectionFactoryBeanName, NatsConnectionFactory.class);
      return connectionFactory;
    }
    throw new IllegalStateException("No connectionFactory bean in context with name " + connectionFactoryBeanName);
  }

  @Override
  public void onApplicationEvent(ContextRefreshedEvent event) {
    // after the context is refreshed - start all the listeners.
    this.applicationContext = event.getApplicationContext();
    start();
  }

  @Override
  public void destroy() {
    log.error("Shutting down listener for {}", clientName);
    this.stop();
  }
}
