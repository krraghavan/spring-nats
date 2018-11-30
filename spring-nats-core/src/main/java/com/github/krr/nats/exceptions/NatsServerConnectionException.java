package com.github.krr.nats.exceptions;

public class NatsServerConnectionException extends RuntimeException {

  public NatsServerConnectionException(String message, Throwable cause) {
    super(message, cause);
  }
}
