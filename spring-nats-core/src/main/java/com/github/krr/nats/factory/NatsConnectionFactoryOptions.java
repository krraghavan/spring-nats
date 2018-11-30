package com.github.krr.nats.factory;

import io.nats.client.ConnectionListener;
import io.nats.client.ErrorListener;
import lombok.Data;

import java.time.Duration;

/**
 * Connection options for NatsConnectionFactory.
 *
 * @todo - security stuff - uname/password or token, SSL.
 */
@Data
public class NatsConnectionFactoryOptions {

  /**
   * The maximum number of reconnect attempts.
   */
  private int maxReconnects;

  private Duration ackTimeout;

  private Duration connectionTimeout = Duration.ofSeconds(5);

  private String connectionName;

  private boolean trackAdvancedStats;

  private ConnectionListener connectionListener;

  private ErrorListener errorListener;

  private boolean lazyConnect = true;

  private String clusterName;

  private String clientName;

}
