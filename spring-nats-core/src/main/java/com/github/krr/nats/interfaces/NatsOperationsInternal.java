package com.github.krr.nats.interfaces;

import com.github.krr.nats.exceptions.MessagePublishingException;

import java.time.Duration;

public interface NatsOperationsInternal {

  void publish(String topic, byte [] request) throws MessagePublishingException;

  byte [] requestResponse(String subject, byte [] request);

  byte[] requestResponse(String subject, byte[] request, Duration timeout);
}
