package com.github.krr.nats.connections;

import com.github.krr.nats.decorators.NatsConnection;
import com.github.krr.nats.decorators.NatsMessageHandler;
import com.github.krr.nats.exceptions.MessagePublishingException;
import com.github.krr.nats.decorators.NatsMessage;
import com.github.krr.nats.interfaces.NatsOperationsInternal;
import io.nats.client.Connection;
import io.nats.client.Message;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@RequiredArgsConstructor
public class NatsConnectionImpl implements NatsOperationsInternal, NatsConnection {

  @NonNull
  @Getter
  private final Connection connection;

  @Override
  public void publish(String topic, byte[] request) throws MessagePublishingException {
    connection.publish(topic, request);
  }

  @Override
  public byte[] requestResponse(String subject, byte[] request) {

    Future<Message> responseMessage = connection.request(subject, request);
    try {
      return responseMessage.get().getData();
    }
    catch (InterruptedException | ExecutionException e) {
      log.error("Could not get response from server", e);
    }
    return new byte[0];
  }

  @Override
  public byte[] requestResponse(String subject, byte[] request, Duration timeout) {
    Future<Message> responseMessage = connection.request(subject, request);
    try {
      return responseMessage.get(timeout.getSeconds(), TimeUnit.SECONDS).getData();
    }
    catch (InterruptedException | ExecutionException | TimeoutException e) {
      log.error("Could not get response from server in time", e);
    }
    return new byte[0];
  }

  @Override
  public void subscribe(String topic, NatsMessageHandler<NatsMessage> callbackFn) {

  }
}
