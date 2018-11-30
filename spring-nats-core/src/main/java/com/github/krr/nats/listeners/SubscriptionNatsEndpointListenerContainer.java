package com.github.krr.nats.listeners;

import com.github.krr.nats.annotations.NatsListener;
import com.github.krr.nats.connections.NatsConnectionImpl;
import com.github.krr.nats.interfaces.NatsConnectionFactory;
import com.github.krr.nats.interfaces.NatsMessageConverter;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
public class SubscriptionNatsEndpointListenerContainer extends AbstractNatsEndpointListenerContainer {

  protected boolean durable;

  protected String durableName;

  private Map<String, Dispatcher> dispatchers = new ConcurrentHashMap<>();

  public SubscriptionNatsEndpointListenerContainer(NatsConnectionFactory connectionFactory,
                                                   Object bean, Method method,
                                                   List<NatsMessageConverter> converters) {
    super(connectionFactory, bean, method, converters);
  }

  @Override
  public void start(NatsListener natsListener) {

    NatsConnectionImpl natsConnectionWrapper = (NatsConnectionImpl) connectionFactory.getConnection(getClientName(natsListener));
    Connection connection = natsConnectionWrapper.getConnection();

    Dispatcher dispatcher = connection.createDispatcher(msg -> {
      // call the method after the appropriate deserializations.
      Object[] args = getMethodArgs(method, msg::getData);
      try {
        method.invoke(bean, args);
      }
      catch (IllegalAccessException | InvocationTargetException e) {
        log.debug("Error accessing or invoking target method {} on bean {}", method.getName(),
                  bean.getClass().getName());
        throw new IllegalStateException("Error invoking method on bean", e);
      }

    });
    dispatchers.put(natsListener.topic(), dispatcher);
    dispatcher.subscribe(natsListener.topic());
  }

  @Override
  public void stop() {
    for (String topic : dispatchers.keySet()) {
      Dispatcher dispatcher = dispatchers.get(topic);
      dispatcher.unsubscribe(topic);
    }
  }
}
