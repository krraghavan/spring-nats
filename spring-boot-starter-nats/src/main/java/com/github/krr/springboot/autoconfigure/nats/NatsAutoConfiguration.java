package com.github.krr.springboot.autoconfigure.nats;

import com.github.krr.nats.connections.NatsCluster;
import com.github.krr.nats.converters.BeanToByteArrayConverter;
import com.github.krr.nats.converters.NatsStringMessageConverter;
import com.github.krr.nats.converters.ProtobufToByteArrayConverter;
import com.github.krr.nats.core.NatsTemplate;
import com.github.krr.nats.factory.NatsConnectionFactoryImpl;
import com.github.krr.nats.factory.NatsConnectionFactoryOptions;
import com.github.krr.nats.factory.NatsStreamingServerConnectionFactory;
import com.github.krr.nats.interfaces.NatsConnectionFactory;
import com.github.krr.nats.interfaces.NatsMessageConverter;
import com.github.krr.nats.interfaces.NatsOperations;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
@ConditionalOnClass(NatsTemplate.class)
public class NatsAutoConfiguration {

  private static final String NATS_HOSTS = "nats://localhost:4222";


  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Bean
  @ConditionalOnMissingBean(NatsOperations.class)
  public NatsOperations natsTemplate(NatsConnectionFactory factory) {
    return new NatsTemplate(factory.getConnection("NatsAutoConfiguration::natsTemplate"),
                            messageConverters());
  }

  @Bean
  @ConditionalOnMissingBean(name = "messageConverters")
  public List<NatsMessageConverter> messageConverters() {
    return Arrays.asList(new NatsStringMessageConverter(), new ProtobufToByteArrayConverter(),
                         new BeanToByteArrayConverter());
  }

  @Bean
  @ConditionalOnMissingBean(NatsConnectionFactory.class)
  @ConditionalOnMissingClass("io.nats.streaming.StreamingConnection")
  public NatsConnectionFactory natsServerConnectionFactory(NatsCluster servers) {
    return new NatsConnectionFactoryImpl(servers, new NatsConnectionFactoryOptions());
  }

  @Bean
  @ConditionalOnMissingBean(NatsConnectionFactory.class)
  @ConditionalOnClass(name = "io.nats.streaming.StreamingConnection")
  @Qualifier("natsServerConnectionFactory")
  public NatsConnectionFactory natsServerConnectionFactoryStreaming(NatsCluster servers) {
    return new NatsStreamingServerConnectionFactory(servers, new NatsConnectionFactoryOptions());
  }

  @Bean
  @ConditionalOnMissingBean(NatsCluster.class)
  public NatsCluster natsCluster() {
    return new NatsCluster(new String[]{NATS_HOSTS});
  }

}
