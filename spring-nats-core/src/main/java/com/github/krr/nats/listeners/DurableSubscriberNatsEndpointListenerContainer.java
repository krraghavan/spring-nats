package com.github.krr.nats.listeners;

import com.github.krr.nats.annotations.NatsListener;
import com.github.krr.nats.connections.StreamingNatsConnection;
import com.github.krr.nats.exceptions.MessageSubscriptionException;
import com.github.krr.nats.interfaces.NatsMessageConverter;
import com.github.krr.nats.interfaces.NatsConnectionFactory;
import io.nats.streaming.StreamingConnection;
import io.nats.streaming.Subscription;
import io.nats.streaming.SubscriptionOptions;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
public class DurableSubscriberNatsEndpointListenerContainer extends SubscriptionNatsEndpointListenerContainer {

  private List<Subscription> subscriptions = new ArrayList<>();

  public DurableSubscriberNatsEndpointListenerContainer(NatsConnectionFactory connectionFactory,
                                                        Object bean, Method method,
                                                        List<NatsMessageConverter> converters) {
    super(connectionFactory, bean, method, converters);
  }

  @Override
  public void start(NatsListener natsListener) {
    SubscriptionOptions.Builder optionsBuilder = new SubscriptionOptions.Builder();
    if (durable) {
      if (StringUtils.isEmpty(durableName)) {
        throw new IllegalArgumentException("When a subscription is durable, the durable name must be specified");
      }
      optionsBuilder.durableName(durableName);
    }
    // set other options here.
    StreamingNatsConnection connWrapper = (StreamingNatsConnection) connectionFactory.getConnection(getClientName(natsListener));
    StreamingConnection connection = connWrapper.getStreamingConnection();
    try {
      Subscription subscription = connection.subscribe(natsListener.topic(), msg -> {
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

      }, optionsBuilder.build());
      subscriptions.add(subscription);
    }
    catch (IOException | InterruptedException | TimeoutException e) {
      log.debug("Error handling subscription", e);
      throw new MessageSubscriptionException(e);
    }

  }

  @Override
  public void stop() {
    for (Subscription subscription : subscriptions) {
      try {
        subscription.close();
      }
      catch (IOException e) {
        log.error("Error closing subscription to topic {}.  This could cause leaks", subscription.getSubject(), e);
      }
    }
  }
}
