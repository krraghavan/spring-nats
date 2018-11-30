package com.github.krr.nats.interfaces;

import com.github.krr.nats.decorators.NatsConnection;

public interface NatsConnectionFactory {

  NatsConnection getConnection(String clientName);

  boolean isStreamingServer();

}
