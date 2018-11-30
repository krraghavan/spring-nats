package com.github.krr.nats.factory;

import com.github.krr.nats.connections.NatsCluster;
import com.github.krr.nats.connections.NatsConnectionImpl;
import com.github.krr.nats.decorators.NatsConnection;
import com.github.krr.nats.exceptions.NatsServerConnectionException;
import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings({"WeakerAccess", "unused"})
@Component
@Slf4j
public class NatsConnectionFactoryImpl extends AbstractNatsConnectionFactory {

  private Map<String, NatsConnectionImpl> connectionMap = new ConcurrentHashMap<>();

  public NatsConnectionFactoryImpl(NatsCluster servers, NatsConnectionFactoryOptions options) {
    super(servers, options);
  }

  protected void beforeOptionsBuild(Options.Builder builder) {

  }

  protected void beforeConnect(Options options) {

  }

  protected void afterConnect(NatsConnection connection) {

  }

  protected Connection getUnderlyingConnection(String clientName) {
    return connectionMap.get(clientName).getConnection();
  }
  @Override
  public NatsConnection getConnection(String clientName) {
    Options.Builder builder = new Options.Builder();
    builder.servers(natsCluster.getHosts()).errorListener(connectionOptions.getErrorListener())
           .connectionListener(connectionOptions.getConnectionListener())
           .connectionName(connectionOptions.getConnectionName())
           .connectionTimeout(connectionOptions.getConnectionTimeout());

    beforeOptionsBuild(builder);
    Options options = builder.build();
    beforeConnect(options);
    NatsConnection connection = connectionMap.computeIfAbsent(clientName, k -> {
      try {
        return new NatsConnectionImpl(Nats.connect(options));
      }
      catch (IOException | InterruptedException e) {
        log.error("Could not connect to the Nats Server {}", options);
        throw new NatsServerConnectionException("Could not connect to NatsServer:" + options.toString(), e);
      }
    });
    afterConnect(connection);
    return connection;
  }

  @Override
  public boolean isStreamingServer() {
    return false;
  }
}
