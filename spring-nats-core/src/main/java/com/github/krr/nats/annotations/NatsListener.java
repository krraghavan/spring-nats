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

package com.github.krr.nats.annotations;

import com.github.krr.nats.interfaces.NatsMessageConverter;
import com.github.krr.nats.interfaces.NatsConnectionFactory;
import com.github.krr.nats.listeners.AbstractNatsEndpointListenerContainer;
import com.github.krr.nats.listeners.DurableSubscriberNatsEndpointListenerContainer;
import com.github.krr.nats.listeners.SubscriptionNatsEndpointListenerContainer;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.List;

/**
 *
 *
 * @author raghavan
 * @since 0.1
 */
@SuppressWarnings("unused")
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Repeatable(NatsListeners.class)
public @interface NatsListener {

  enum NatsListenerType {
    SUBSCRIPTION {
      @Override
      public AbstractNatsEndpointListenerContainer listenerContainer(NatsListener natsListener,
                                                                     NatsConnectionFactory connectionFactory,
                                                                     Object bean, Method method,
                                                                     List<NatsMessageConverter> converters) {
        if(connectionFactory.isStreamingServer()) {
          return new DurableSubscriberNatsEndpointListenerContainer(connectionFactory, bean, method,
                                                                    converters);
        }
        return new SubscriptionNatsEndpointListenerContainer(connectionFactory, bean, method, converters);
      }
    },
    QUEUE {
      @Override
      public AbstractNatsEndpointListenerContainer listenerContainer(NatsListener natsListener,
                                                                     NatsConnectionFactory connectionFactory,
                                                                     Object bean, Method method,
                                                                     List<NatsMessageConverter> converters) {
        return new SubscriptionNatsEndpointListenerContainer(connectionFactory, bean, method, converters);
      }
    };

    public abstract AbstractNatsEndpointListenerContainer listenerContainer(NatsListener natsListener,
                                                                            NatsConnectionFactory connectionFactory,
                                                                            Object bean, Method method,
                                                                            List<NatsMessageConverter> converters);
  }

  /**
   * A bean name or SpEl expression for the connection factory
   */
  String natsConnectionFactory() default "natsConnectionFactory";

  /**
   * The type of listener
   * @return the Nats listener type
   */
  NatsListenerType type();

  /**
   * The topic to subscribe to.  For request response messages, this topic is used
   * as the publishing topic and the reply to topic of this subscription is used
   * to post the response.
   *
   * @return - the name of the topic to subscribe to.
   */
  String topic();

  /**
   * A name for this subscription.  It is recommended to use user friendly names.
   * IF not set defaults to the ClassName::methodName of the listener method
   * that this annotation is present on.
   *
   * @return the clientId of this client.
   */
  String clientId() default "";

  /**
   * Controls whether the ack for this subscription is set manually or automatically
   *
   * @return - true if acks are manually sent.  Defaults to false.
   */
  boolean manualAck() default false;

  /**
   * The timeout waiting for an acknowledgement
   *
   * @return - the number of seconds to wait before timing out a subscriber listener invocation.  The
   * NATS server will resend the message.
   */
  int ackWait() default 30;

  /**
   * The number of messages that can concurrently be in flight without acknowledgement
   * Use this setting to rate limit fast publishers to match subscriber capabilities.
   *
   * @return - the number of unacknowledged messages that can be in flight.
   */
  int maxInFlight() default 5;

  /**
   * Whether this subscription should be durable or not.  The default is true.  This only
   * applies when the NATS streaming server is used.  If not this option is ignored.  Setting this
   * to false makes the subscription non-durable and no replay is possible.
   *
   * @return whether the subscription is durable or not.
   */
  boolean durable() default true;

  /**
   * A name assigned to this durable subscription.  If not specified, defaults to the className::methodName
   * of the method and class on which this annotation is present.
   *
   * @return - the durable name of the subscription.  The client_id + durableName form a unique tuple for
   * messages
   */
  String durableName() default "";

  /**
   * The queue group to which this listener belongs.  Only one subscriber from each queue group
   * will receive a message.  The subscribers are chosen at random so using the same queue grooup
   * will effectively distribute the requests amongst multiple consumers in parallel.
   *
   * @return - the name of the queue group
   */
  String queueGroupId() default "";

}
