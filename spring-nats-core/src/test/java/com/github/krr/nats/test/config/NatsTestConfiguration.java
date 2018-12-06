package com.github.krr.nats.test.config;

import berlin.yuna.natsserver.logic.NatsServer;
import com.github.krr.nats.connections.NatsCluster;
import com.github.krr.nats.factory.NatsConnectionFactoryImpl;
import com.github.krr.nats.factory.NatsConnectionFactoryOptions;
import com.github.krr.nats.interfaces.NatsConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@Slf4j
@Import(EmbeddedNatsServerConfiguration.class)
public class NatsTestConfiguration {

  @Bean
  public NatsConnectionFactory natsConnectionFactory(NatsCluster servers) {
    return new NatsConnectionFactoryImpl(servers, new NatsConnectionFactoryOptions());
  }

  @SuppressWarnings("unused")
  @Bean
  public NatsCluster natsCluster(NatsServer natsServer) {
    String natsHosts = "localhost:".concat(String.valueOf(natsServer.port()));
    return new NatsCluster(new String[]{natsHosts});
  }


}
