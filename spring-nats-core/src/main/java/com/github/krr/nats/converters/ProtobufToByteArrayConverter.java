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

import com.github.krr.nats.exceptions.MessageConversionException;
import com.github.krr.nats.interfaces.NatsMessageConverter;
import com.google.protobuf.Any;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

/**
 * Converts to and from Protobufs to byte []
 *
 * @author raghavan
 */
@Component
@Slf4j
public class ProtobufToByteArrayConverter implements NatsMessageConverter<Message> {

  @Override
  public byte[] toBytes(Message input) {
    return input.toByteArray();
  }

  @Override
  public Message fromBytes(byte[] bytes, Class<Message> clazz) throws MessageConversionException {
    log.trace("Converting {} to Message", clazz);
    try {
      Any any = Any.parseFrom(bytes);
      return any.unpack(clazz);
    }
    catch (InvalidProtocolBufferException e) {
      throw new MessageConversionException("Could not convert message", e);
    }
  }

  @Override
  public boolean supports(Class<Message> aClass) {
    return ClassUtils.isAssignable(Message.class, aClass);
  }
}