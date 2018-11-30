package com.github.krr.nats.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.krr.nats.exceptions.MessageConversionException;
import com.github.krr.nats.interfaces.NatsMessageConverter;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * This is a fallback bean that should almost always successfully serialize/deserialize
 * an object.  Ensure that this is the last in the list of converters.
 *
 * @param <T>
 */
@Component
public class BeanToByteArrayConverter<T> implements NatsMessageConverter<T> {

  private ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public byte[] toBytes(T input) throws MessageConversionException {
    try {
      return objectMapper.writeValueAsBytes(input);
    }
    catch (JsonProcessingException e) {
      throw new MessageConversionException("Could not serialize class " + input.getClass(), e);
    }
  }

  @Override
  public T fromBytes(byte[] bytes, Class<T> clazz) throws MessageConversionException {
    try {
      return objectMapper.readValue(bytes, clazz);
    }
    catch (IOException e) {
      throw new MessageConversionException("Could not deserialize to " + clazz, e);
    }
  }

  @Override
  public boolean supports(Class<T> aClass) {
    return true;
  }
}
