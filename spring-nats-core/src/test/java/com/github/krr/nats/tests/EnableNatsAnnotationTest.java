/*
 *
 *  Copyright (c) 2018 the original author or authors.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.github.krr.nats.tests;

import berlin.yuna.natsserver.logic.NatsServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.krr.nats.annotations.EnableNats;
import com.github.krr.nats.annotations.NatsListener;
import com.github.krr.nats.config.NatsListenerAnnotationBeanPostProcessor;
import com.github.krr.nats.config.NatsListenerEndpointRegistry;
import com.github.krr.nats.connections.NatsCluster;
import com.github.krr.nats.converters.BeanToByteArrayConverter;
import com.github.krr.nats.converters.NatsStringMessageConverter;
import com.github.krr.nats.converters.ProtobufToByteArrayConverter;
import com.github.krr.nats.decorators.NatsConnection;
import com.github.krr.nats.interfaces.NatsConnectionFactory;
import com.github.krr.nats.interfaces.NatsEndpointListenerContainer;
import com.github.krr.nats.interfaces.NatsMessageConverter;
import com.github.krr.nats.test.config.EmbeddedNatsServerConfiguration;
import io.nats.client.Connection;
import io.nats.client.Nats;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.util.MultiValueMap;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * <Description>
 *
 * @author raghavan
 */
@Test
public class EnableNatsAnnotationTest {

  @ContextConfiguration
  public static class TestsWithDefaultNonStreamingConnectionFactory extends AbstractTestNGSpringContextTests {

    @Autowired
    private NatsCluster natsCluster;

    @Test
    public void mustProcessNatsListenersWhenAnnotatedWithEnableNatsAnnotation() {

      Assert.assertNotNull(applicationContext);
      NatsListenerEndpointRegistry registry = applicationContext.getBean(
          NatsListenerAnnotationBeanPostProcessor.NATS_ENDPOINT_LISTENER_REGISTRY_BEAN,
          NatsListenerEndpointRegistry.class);
      Assert.assertNotNull(registry);
      MultiValueMap<NatsListener, NatsEndpointListenerContainer> natsListenerMap = registry.getNatsListenerMap();
      Assert.assertEquals(natsListenerMap.size(), 2);
      List<NatsEndpointListenerContainer> xyzListeners = getListenersForTopic(natsListenerMap, "xyz");
      Assert.assertNotNull(xyzListeners);
      Assert.assertEquals(xyzListeners.size(), 1);
      Assert.assertEquals(xyzListeners.get(0).getMethod().getName(), "method1" );
    }

    @SuppressWarnings("SameParameterValue")
    private List<NatsEndpointListenerContainer> getListenersForTopic(MultiValueMap<NatsListener, NatsEndpointListenerContainer> natsListenerMap,
                                                                     String topic) {
      Optional<NatsListener> listener = natsListenerMap.keySet().stream().filter(k -> k.topic().equals(topic)).findFirst();
      return listener.map(natsListenerMap::get).orElse(null);
    }

    @Test
    public void mustInvokeSubscriberWhenMessageIsPublishedToXYZTopic() throws Exception {

      Assert.assertNotNull(applicationContext);
      CountDownLatch latch = applicationContext.getBean("latch1", CountDownLatch.class);
      Assert.assertNotNull(latch);
      NatsConnectionFactory connectionFactory = applicationContext.getBean("natsConnectionFactory",
                                                                           NatsConnectionFactory.class);
      Assert.assertNotNull(connectionFactory);
      NatsConnection connection = connectionFactory.getConnection("TWDNSCF::mustInvokeSubscriberWhenMessageIsPublishedToXYZTopic");
      Assert.assertNotNull(connection);
      connection.publish("xyz", applicationContext.getBean("randomInputString", String.class).getBytes());
      latch.await(3, TimeUnit.SECONDS);
      Assert.assertEquals(latch.getCount(), 0);
    }

    @Test
    public void mustInvokeSubscriberWithBeanPayloadMessageIsPublishedToAbcTopic() throws Exception {

      Assert.assertNotNull(applicationContext);
      CountDownLatch latch = applicationContext.getBean("latch2", CountDownLatch.class);
      Assert.assertNotNull(latch);
      Assert.assertNotNull(natsCluster);
      Connection connection = Nats.connect(natsCluster.getNatsUrl());
      Assert.assertNotNull(connection);
      TestBean testBean = applicationContext.getBean("testBean", TestBean.class);
      ObjectMapper mapper = new ObjectMapper();
      byte [] bytes = mapper.writeValueAsBytes(testBean);
      connection.publish("abc", bytes);
      latch.await(3, TimeUnit.SECONDS);
      Assert.assertEquals(latch.getCount(), 0);
    }

    @Configuration
    @EnableNats
    @Import(EmbeddedNatsServerConfiguration.class)
    @DirtiesContext
    public static class Config {

      @Bean
      public String randomInputString() {
        return RandomStringUtils.randomAlphabetic(10);
      }

      @Bean
      public TestBean testBean() {
        return new TestBean(randomInputString());
      }

      @Bean
      public CountDownLatch latch1() {
        return new CountDownLatch(1);
      }

      @Bean
      public CountDownLatch latch2() {
        return new CountDownLatch(1);
      }

      @Bean
      public Listener listener(CountDownLatch latch1, CountDownLatch latch2, String randomInputString, TestBean testBean) {
        return new Listener(latch1, latch2, randomInputString, testBean);
      }

      @Bean
      public List<NatsMessageConverter> converters() {
        return Arrays.asList(new NatsStringMessageConverter(), new ProtobufToByteArrayConverter(),
                             new BeanToByteArrayConverter<>());
      }

      @SuppressWarnings("unused")
      @Bean
      public NatsCluster natsCluster(NatsServer natsServer) {
        String natsHosts = "localhost:".concat(String.valueOf(natsServer.port()));
        return new NatsCluster(new String[]{natsHosts});
      }

    }

    @SuppressWarnings("WeakerAccess")
    @Component
    public static class Listener {

      private final CountDownLatch latch1;

      private final CountDownLatch latch2;

      private final String randomInputString;

      private final TestBean testBean;

      public Listener(CountDownLatch latch1, CountDownLatch latch2, String randomInput, TestBean testBean) {

        this.latch1 = latch1;
        this.randomInputString = randomInput;
        this.testBean = testBean;
        this.latch2 = latch2;
      }

      @NatsListener(topic = "xyz", type = NatsListener.NatsListenerType.SUBSCRIPTION, durable = false)
      public String method1(String input) {
        Assert.assertEquals(input, randomInputString);
        latch1.countDown();
        return "hello";
      }

      @NatsListener(topic = "abc", type = NatsListener.NatsListenerType.SUBSCRIPTION, durable = false)
      public void method2(TestBean input) {
        Assert.assertEquals(input, testBean);
        latch2.countDown();
      }

    }

    @SuppressWarnings("unused")
    @Data
    @RequiredArgsConstructor
    public static class TestBean {

      @NonNull
      private String randomString;

      public TestBean() {
        // need this for Jackson
      }

    }

  }
}
