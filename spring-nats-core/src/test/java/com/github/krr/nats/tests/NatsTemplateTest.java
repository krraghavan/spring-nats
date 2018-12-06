package com.github.krr.nats.tests;

import berlin.yuna.natsserver.logic.NatsServer;
import com.github.krr.nats.connections.NatsCluster;
import com.github.krr.nats.converters.NatsStringMessageConverter;
import com.github.krr.nats.core.NatsTemplate;
import com.github.krr.nats.factory.NatsConnectionFactoryImpl;
import com.github.krr.nats.factory.NatsConnectionFactoryOptions;
import com.github.krr.nats.interfaces.NatsConnectionFactory;
import com.github.krr.nats.interfaces.NatsOperations;
import com.github.krr.nats.test.config.EmbeddedNatsServerConfiguration;
import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.Nats;
import io.nats.client.Subscription;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

import static java.util.Collections.singletonList;

@ContextConfiguration
public class NatsTemplateTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private NatsOperations natsTemplate;

  @Autowired
  private NatsServer natsServer;

  private String getNatsHosts() {
    return "localhost:".concat(String.valueOf(natsServer.port()));
  }

  @Test
  public void mustPublishMessageToNatsServerFromTemplate() throws Exception {
    Assert.assertNotNull(natsTemplate);
    String testTopic = RandomStringUtils.randomAlphabetic(10);
    String testMessage = RandomStringUtils.randomAlphabetic(20);
    Connection connection = Nats.connect("nats://".concat(getNatsHosts()));
    Subscription subscription = connection.subscribe(testTopic);
    natsTemplate.publish(testTopic, testMessage);
    Message message = subscription.nextMessage(Duration.ofSeconds(3));
    Assert.assertNotNull(message);
    Assert.assertEquals(message.getSubject(), testTopic);
    Assert.assertEquals(new String(message.getData()), testMessage);
    connection.close();
  }

  @Test
  public void mustGetResponseToARequestResponseMessage() throws Exception {
    Assert.assertNotNull(natsTemplate);
    String testTopic = RandomStringUtils.randomAlphabetic(10);
    String testMessage = RandomStringUtils.randomAlphabetic(20);
    String testResponse = RandomStringUtils.randomAlphabetic(30);
    Connection connection = Nats.connect("nats://".concat(getNatsHosts()));
    Subscription subscription = connection.subscribe(testTopic);
    // subscribe on a separate thread since request-response blocks.
    Disposable stringMono = Mono.just(subscription).doOnNext(m -> {
      Message message;
      try {
        message = subscription.nextMessage(Duration.ofSeconds(5));
        Assert.assertNotNull(message);
        // request response works by publishing a response to the replyTo topic of
        // the subscription message.
        connection.publish(message.getReplyTo(), testResponse.getBytes());
      }
      catch (InterruptedException e) {
        Assert.fail("Did not get message within 3 seconds");
      }
    }).subscribeOn(Schedulers.newSingle("SubscriberThread")).subscribe();
    String responseMessage = natsTemplate.requestResponse(testTopic, testMessage, String.class);
    Assert.assertNotNull(responseMessage);
    Assert.assertEquals(responseMessage, testResponse);
    connection.close();
    stringMono.dispose();
  }


  @Configuration
  @Import({
      EmbeddedNatsServerConfiguration.class,
  })
  @DirtiesContext
  public static class NatsTemplateTestConfiguration {

    @Bean
    public NatsOperations natsOperations(NatsConnectionFactory factory) {
      return new NatsTemplate(factory,
                              "NatsAutoConfiguration::natsTemplate", singletonList(new NatsStringMessageConverter()));
    }

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
}