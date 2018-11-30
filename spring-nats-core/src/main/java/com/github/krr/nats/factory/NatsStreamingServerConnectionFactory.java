package com.github.krr.nats.factory;

import com.github.krr.nats.annotations.RuntimeDependency;
import com.github.krr.nats.config.ClassExistsCondition;
import com.github.krr.nats.connections.NatsCluster;
import com.github.krr.nats.connections.StreamingNatsConnection;
import com.github.krr.nats.decorators.NatsConnection;
import io.nats.client.Connection;
import io.nats.client.ConnectionListener;
import io.nats.client.ErrorListener;
import io.nats.streaming.StreamingConnection;
import io.nats.streaming.StreamingConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("WeakerAccess")
@Component
@Conditional(ClassExistsCondition.class)
@RuntimeDependency(classes = "io.nats.streaming.Options")
@Slf4j
public class NatsStreamingServerConnectionFactory extends NatsConnectionFactoryImpl {

  private Map<String, StreamingNatsConnection> connectionMap = new ConcurrentHashMap<>();

  public NatsStreamingServerConnectionFactory(NatsCluster servers, NatsConnectionFactoryOptions options) {
    super(servers, options);
  }

  @Override
  protected void beforeOptionsBuild(io.nats.client.Options.Builder builder) {
    log.trace("NatsStreamingServerConnectionFactory::beforeOptionsBuild");
    String connectionName = connectionOptions.getConnectionName();
    if(!StringUtils.isEmpty(connectionName)) {
      builder.connectionName(connectionName);
    }
    ErrorListener errorListener = connectionOptions.getErrorListener();
    if(errorListener != null) {
      builder.errorListener(errorListener);
    }
    ConnectionListener connectionListener = connectionOptions.getConnectionListener();
    if(connectionListener != null) {
      builder.connectionListener(connectionListener);
    }
  }

  @Override
  public NatsConnection getConnection(String clientName) {
    try {
      StreamingConnectionFactory cf = new StreamingConnectionFactory(connectionOptions.getClusterName(),
                                                                     clientName);
      Connection underlyingConnection = getUnderlyingConnection(clientName);
      cf.setNatsConnection(underlyingConnection);
      StreamingConnection streamingConnection = cf.createConnection();
      return connectionMap.computeIfAbsent(clientName, k -> new StreamingNatsConnection(streamingConnection
      ));
    }
    catch (IOException | InterruptedException e) {
      log.error("Could not connect to Nats streaming server", e);
    }
    throw new IllegalArgumentException("Error establishing streaming connection to Nats server");
  }

  @Override
  public boolean isStreamingServer() {
    return true;
  }

}
