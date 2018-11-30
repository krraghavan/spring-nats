package com.github.krr.nats.exceptions;


public class MessageConversionException extends Exception {

  public MessageConversionException(String message, Throwable e) {
    super(message, e);
  }
}
