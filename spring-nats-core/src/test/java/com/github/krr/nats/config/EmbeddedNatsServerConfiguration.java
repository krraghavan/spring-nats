package com.github.krr.nats.config;

import berlin.yuna.natsserver.logic.NatsServer;
import com.github.krr.nats.connections.NatsCluster;
import com.github.krr.nats.factory.NatsConnectionFactoryOptions;
import com.github.krr.nats.factory.NatsConnectionFactoryImpl;
import com.github.krr.nats.interfaces.NatsConnectionFactory;
import lombok.Getter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.SocketUtils;

import javax.annotation.PreDestroy;
import java.io.IOException;

@Configuration
public class EmbeddedNatsServerConfiguration {

  private NatsServer natsServer;

  @Getter
  private String natsHosts;

  @Bean
  public NatsServer natsServer() throws IOException {

    int NATS_SERVER_PORT = SocketUtils.findAvailableTcpPort(10000, 20000);

    int NATS_SERVER_PORT_HTTP = SocketUtils.findAvailableTcpPort(20001, 30000);

    String NATS_SERVER_PORT_HTTP_STR = String.valueOf(NATS_SERVER_PORT_HTTP);

    final String PORT_STR = String.valueOf(NATS_SERVER_PORT);

    String NATS_CLUSTER_ID = "test-cluster-".concat(PORT_STR);

    String [] NATS_SERVER_CONFIG = {"port:".concat(PORT_STR),
                                                   "cluster_id:".concat(NATS_CLUSTER_ID),
                                                   "http_port:".concat(NATS_SERVER_PORT_HTTP_STR)};

    natsHosts = "localhost:".concat(PORT_STR);


    natsServer = new NatsServer(NATS_SERVER_CONFIG);
    natsServer.start();
    System.out.println("***** NATS SERVER CAN BE MONITORED ON " + NATS_SERVER_PORT_HTTP_STR + " *****");
    return natsServer;
  }

  @Bean
  public NatsConnectionFactory natsConnectionFactory(NatsCluster servers) {
    return new NatsConnectionFactoryImpl(servers, new NatsConnectionFactoryOptions());
  }

  @SuppressWarnings("unused")
  @Bean
  public NatsCluster natsCluster(NatsServer natsServer) {
    return new NatsCluster(new String[]{natsHosts});
  }

  @PreDestroy
  public void shutdown() {
    if (natsServer != null) {
      natsServer.stop();
    }
  }
}
