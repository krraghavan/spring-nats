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

package com.github.krr.nats.interfaces;

import com.github.krr.nats.exceptions.MessageConversionException;
import com.github.krr.nats.exceptions.MessagePublishingException;

public interface NatsOperations {

  /**
   * Interface to the NAT request response message type.
   *
   * @param request - the request object.
   * @param <O> - the returned response
   * @param <I> - the input message to send to the Nats server
   * @param returnClass - the return class type.
   * @return the response to the request.
   */
  <O, I> O requestResponse(String subject, I request, RequestOptions options, Class<O> returnClass) throws MessageConversionException;

  @SuppressWarnings("UnusedReturnValue")
  default <O, I> O requestResponse(String subject, I request, Class<O> returnClass) throws MessageConversionException {
    return requestResponse(subject, request, RequestOptions.DEFAULT_REQUEST_OPTIONS, returnClass);
  }

  <I> void publish(String topic, I request, RequestOptions options) throws MessagePublishingException, MessageConversionException;

  default <I> void publish(String topic, I request) throws MessagePublishingException, MessageConversionException {
    publish(topic, request, RequestOptions.DEFAULT_REQUEST_OPTIONS);
  }

}
