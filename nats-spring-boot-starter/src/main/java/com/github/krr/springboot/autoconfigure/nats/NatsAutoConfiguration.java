package com.github.krr.springboot.autoconfigure.nats;

import com.github.krr.nats.config.NatsListenerAnnotationBeanPostProcessor;
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
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.List;

import static java.util.UUID.randomUUID;

@Configuration
@ConditionalOnClass(NatsTemplate.class)
public class NatsAutoConfiguration {

  private static final String NATS_HOSTS = "nats://localhost:4222";

  @SuppressWarnings("WeakerAccess")
  public static final String DEFAULT_NATS_TEMPLATE_CLIENT_NAME = "NatsAutoConfiguration__natsTemplate";


  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Bean
  @ConditionalOnMissingBean(NatsTemplate.class)
  public NatsOperations natsTemplate(NatsConnectionFactory factory) {
    return new NatsTemplate(factory, DEFAULT_NATS_TEMPLATE_CLIENT_NAME.concat(randomUUID().toString()),
                            natsMessageConverters());
  }

  @Bean
  @ConditionalOnMissingBean(name = NatsListenerAnnotationBeanPostProcessor.DEFAULT_NATS_MESSAGE_CONVERTERS_BEAN_NAME)
  public List<NatsMessageConverter> natsMessageConverters() {
    return Arrays.asList(new NatsStringMessageConverter(), new ProtobufToByteArrayConverter(),
                         new BeanToByteArrayConverter());
  }

  @Bean
  @ConditionalOnMissingBean(name = "natsClusterId")
  public String natsClusterId(Environment env) {
    int natsServerPort = env.getProperty("nats.server.port", Integer.class, 1422);
    return "test-cluster-".concat(String.valueOf(natsServerPort));
  }

  @Bean
  @ConditionalOnMissingBean(value = NatsConnectionFactory.class)
  @ConditionalOnMissingClass("io.nats.streaming.StreamingConnection")
  public NatsConnectionFactory natsServerConnectionFactory(NatsCluster servers, String natsClusterId) {
    return new NatsConnectionFactoryImpl(servers, natsTemplateConnectionFactoryOptions(natsClusterId));
  }

  @Bean(name = "natsServerConnectionFactory")
  @ConditionalOnMissingBean(NatsConnectionFactory.class)
  @ConditionalOnClass(name = "io.nats.streaming.StreamingConnection")
  public NatsConnectionFactory natsServerConnectionFactoryStreaming(NatsCluster servers, String natsClusterId) {
    return new NatsStreamingServerConnectionFactory(servers, natsTemplateConnectionFactoryOptions(natsClusterId));
  }

  @Bean
  @ConditionalOnMissingBean(name = "natsTemplateConnectionFactoryOptions")
  @Qualifier("natsServerConnectionFactory")
  public NatsConnectionFactoryOptions natsTemplateConnectionFactoryOptions(String natsClusterId) {
    NatsConnectionFactoryOptions natsConnectionFactoryOptions = new NatsConnectionFactoryOptions();
    natsConnectionFactoryOptions.setClusterName(natsClusterId);
    return natsConnectionFactoryOptions;
  }

  @Bean
  @ConditionalOnMissingBean(NatsCluster.class)
  public NatsCluster natsCluster() {
    return new NatsCluster(new String[]{NATS_HOSTS});
  }

}
