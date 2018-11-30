package com.github.krr.nats.decorators;

import com.github.krr.nats.exceptions.MessagePublishingException;

public interface NatsConnection {

  void subscribe(String topic, NatsMessageHandler<NatsMessage> callbackFn);

  byte[] requestResponse(String subject, byte[] input);

  void publish(String topic, byte[] input) throws MessagePublishingException;
}
