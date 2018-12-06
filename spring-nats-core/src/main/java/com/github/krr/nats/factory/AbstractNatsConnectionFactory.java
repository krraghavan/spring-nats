package com.github.krr.nats.factory;

import com.github.krr.nats.interfaces.NatsConnectionFactory;
import com.github.krr.nats.connections.NatsCluster;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@SuppressWarnings("WeakerAccess")
@Data
@Slf4j
public abstract class AbstractNatsConnectionFactory implements NatsConnectionFactory {

  protected final NatsCluster natsCluster;

  protected final NatsConnectionFactoryOptions connectionOptions;

  /**
   * connected is an internal state.
   */
  @Getter(AccessLevel.PRIVATE)
  @Setter(AccessLevel.PRIVATE)
  private boolean connected = false;

  public AbstractNatsConnectionFactory(NatsCluster servers, NatsConnectionFactoryOptions options) {
    this.natsCluster = servers;
    this.connectionOptions = options;
  }

}
