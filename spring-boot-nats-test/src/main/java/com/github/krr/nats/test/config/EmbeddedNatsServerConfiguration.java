package com.github.krr.nats.test.config;

import berlin.yuna.natsserver.logic.NatsServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.PreDestroy;
import java.io.IOException;

import static org.springframework.util.SocketUtils.findAvailableTcpPort;

@Configuration
public class EmbeddedNatsServerConfiguration {

  private NatsServer natsServer;

  @Bean
  public NatsServer natsServer(Environment env) throws IOException {

    int natsServerPort = env.getProperty("nats.server.port", Integer.class, findAvailableTcpPort(10000, 20000));

    String natsPort = String.valueOf(natsServerPort);
    String natsClusterId = natsClusterId(natsPort);

    int natsHttpPort = env.getProperty("nats.server.http.port", Integer.class, findAvailableTcpPort(20001, 30000));

    String natsServerPortHttpStr = String.valueOf(natsHttpPort);

    String[] natsServerConfig = {"port:".concat(natsPort),
                                 "cluster_id:".concat(natsClusterId),
                                 "http_port:".concat(natsServerPortHttpStr)};

    System.out.println("***** NATS SERVER CAN BE MONITORED ON " + natsServerPortHttpStr + " *****");
    natsServer = new NatsServer(natsServerConfig);
    natsServer.start();
    return natsServer;
  }

  @Bean
  public String natsClusterId(String natsPort) {
    return "test-cluster-".concat(natsPort);
  }

  @PreDestroy
  public void shutdown() {
    if (natsServer != null) {
      natsServer.stop();
    }
  }
}
