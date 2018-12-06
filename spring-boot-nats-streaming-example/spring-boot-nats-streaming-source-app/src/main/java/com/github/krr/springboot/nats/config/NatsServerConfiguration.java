package com.github.krr.springboot.nats.config;

import com.github.krr.nats.connections.NatsCluster;
import com.github.krr.nats.test.config.EmbeddedNatsServerConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;

@Slf4j
@Configuration
@Import(EmbeddedNatsServerConfiguration.class)
public class NatsServerConfiguration {

  @Bean
  public NatsCluster natsCluster(Environment environment) {
    String natsHosts = "localhost:".concat(String.valueOf(environment.getProperty("nats.server.port",
                                                                                  Integer.class,
                                                                                  19000)));
    return new NatsCluster(new String[]{natsHosts});
  }

}
