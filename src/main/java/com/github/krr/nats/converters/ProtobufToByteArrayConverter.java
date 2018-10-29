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

package com.github.krr.nats.converters;

import com.github.krr.nats.interfaces.NatsMessageConverter;
import com.google.protobuf.Message;

/**
 * Converts to an from Protobufs to byte []
 *
 * @author raghavan
 */
public class ProtobufToByteArrayConverter implements NatsMessageConverter<Message> {

  @Override
  public byte[] toBytes(Message input) {
    return new byte[0];
  }

  @Override
  public Message fromBytes(byte[] bytes) {
    return null;
  }
}