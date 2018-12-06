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

package com.github.krr.nats.core;

import com.github.krr.nats.decorators.NatsConnection;
import com.github.krr.nats.exceptions.MessageConversionException;
import com.github.krr.nats.exceptions.MessagePublishingException;
import com.github.krr.nats.interfaces.NatsConnectionFactory;
import com.github.krr.nats.interfaces.NatsMessageConverter;
import com.github.krr.nats.interfaces.NatsOperations;
import com.github.krr.nats.interfaces.RequestOptions;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.ConverterNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.core.convert.TypeDescriptor.forObject;
import static org.springframework.core.convert.TypeDescriptor.valueOf;

/**
 * A concrete implementation of the NatsOperations class
 *
 * @author raghavan
 * @since 0.1
 */
@SuppressWarnings("WeakerAccess")
@Component
public class NatsTemplate implements NatsOperations {

  /**
   * The underlying Nats connection over which the messages are sent
   */
  private final NatsConnectionFactory natsConnectionFactory;

  /**
   * A list of converters used to convert messages to byte []
   */
  private final List<NatsMessageConverter> messageConverters;

  private final String clientName;

  public NatsTemplate(NatsConnectionFactory natsConnectionFactory, String clientName,
                      List<NatsMessageConverter> messageConverters) {
    this.natsConnectionFactory = natsConnectionFactory;
    this.clientName = clientName;
    this.messageConverters = messageConverters;
  }

  @Override
  public <O, I> O requestResponse(String subject, I request, RequestOptions options,
                                  Class<O> returnClass) throws MessageConversionException {
    // convert messages to byte arrays
    byte [] input = convertMessage(request, options);
    byte[] response = natsConnection().requestResponse(subject, input);
    return convertMessage(response, returnClass);
  }

  private NatsConnection natsConnection() {
   return natsConnectionFactory.getConnection(clientName);
  }

  @SuppressWarnings("unchecked")
  private <O> O convertMessage(byte[] response, Class<O> returnClass) {
    for(NatsMessageConverter messageConverter : messageConverters) {
      if(messageConverter.supports(returnClass)) {
        try {
          return (O) messageConverter.fromBytes(response, returnClass);
        }
        catch (MessageConversionException e) {
          throw new ConversionFailedException(forObject(response), valueOf(returnClass), response, e);
        }
      }
    }
    throw new ConverterNotFoundException(forObject(response), valueOf(returnClass));
  }

  @SuppressWarnings({"unchecked", "unused"})
  private <I> byte[] convertMessage(I request, RequestOptions options) throws MessageConversionException {
    Class<?> aClass = request.getClass();
    for(NatsMessageConverter messageConverter : messageConverters) {
      if(messageConverter.supports(aClass)) {
        return messageConverter.toBytes(request);
      }
    }
    throw new ConverterNotFoundException(forObject(request), valueOf(byte [].class));
  }

  @Override
  public <I> void publish(String topic, I request,
                          RequestOptions options) throws MessagePublishingException, MessageConversionException {
    byte [] input = convertMessage(request, options);
    natsConnectionFactory.getConnection(clientName).publish(topic, input);
  }
}
