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

import java.lang.reflect.Method;
import java.util.List;

@SuppressWarnings("WeakerAccess")
@Slf4j
@Data
public abstract class AbstractNatsEndpointListenerContainer implements NatsEndpointListenerContainer {

  protected final NatsConnectionFactory connectionFactory;

  protected final Object bean;

  protected final Method method;

  protected final List<NatsMessageConverter> converters;

  protected AbstractNatsEndpointListenerContainer(NatsConnectionFactory connectionFactory,
                                                  Object bean, Method method,
                                                  List<NatsMessageConverter> converters) {
    this.connectionFactory = connectionFactory;
    this.method = method;
    this.bean = bean;
    this.converters = converters;
  }

  protected String getClientName(NatsListener natsListener) {
    if(StringUtils.isEmpty(natsListener.clientId())) {
      // no client id specified - get default name.
      return getDefaultClientName();
    }
    return natsListener.clientId();
  }

  private String getDefaultClientName() {
    return method.getDeclaringClass().getName().concat("::").concat(method.getName());
  }

  @SuppressWarnings("unchecked")
  protected Object[] getMethodArgs(Method method, NatsMessage msg) {
    Class<?>[] parameterTypes = method.getParameterTypes();
    if(parameterTypes.length > 1) {
      throw new IllegalArgumentException("Nats subscription methods can only have a single argument");
    }
    if(parameterTypes.length == 1) {
      for(NatsMessageConverter converter : converters) {
        if(converter.supports(parameterTypes[0])) {
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


}
