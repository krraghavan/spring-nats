package com.github.krr.nats.converters;

import com.github.krr.nats.interfaces.NatsMessageConverter;

public class NatsStringMessageConverter implements NatsMessageConverter<String> {

  @Override
  public byte[] toBytes(String input) {
    return input.getBytes();
  }

  @Override
  public String fromBytes(byte[] bytes, Class<String> clazz) {
    return new String(bytes);
  }

  @Override
  public boolean supports(Class<String> aClass) {
    return aClass == String.class;
  }
}
