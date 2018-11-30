package com.github.krr.nats.connections;

import com.github.krr.nats.decorators.NatsConnection;
import com.github.krr.nats.decorators.NatsMessage;
import com.github.krr.nats.decorators.NatsMessageHandler;
import com.github.krr.nats.exceptions.MessagePublishingException;
import io.nats.streaming.StreamingConnection;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Slf4j
public class StreamingNatsConnection extends NatsConnectionImpl implements NatsConnection {

  @NonNull
  @Getter
  private final StreamingConnection streamingConnection;

  public StreamingNatsConnection(StreamingConnection connection) {

    super(connection.getNatsConnection());
    streamingConnection = connection;
  }

  @Override
  public void publish(String topic, byte[] request) throws MessagePublishingException {
    try {
      streamingConnection.publish(topic, request);
    }
    catch (IOException | InterruptedException | TimeoutException e) {
      log.debug("Error publishing message ", e);
      throw new MessagePublishingException(e);
    }
  }

  @Override
  public void subscribe(String topic, NatsMessageHandler<NatsMessage> callbackFn) {

  }
}
