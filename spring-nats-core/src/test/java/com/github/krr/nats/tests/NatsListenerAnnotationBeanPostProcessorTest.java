package com.github.krr.nats.tests;

import com.github.krr.nats.config.NatsListenerAnnotationBeanPostProcessor;
import com.github.krr.nats.converters.NatsStringMessageConverter;
import com.github.krr.nats.converters.ProtobufToByteArrayConverter;
import com.github.krr.nats.interfaces.NatsMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("Duplicates")
@Test
public class NatsListenerAnnotationBeanPostProcessorTest  {

  @ContextConfiguration
  public static class Test1 extends AbstractTestNGSpringContextTests {
    @Test
    public void mustGetNatsMessageConverters() {
      NatsListenerAnnotationBeanPostProcessor beanPostProcessor = new NatsListenerAnnotationBeanPostProcessor();
      beanPostProcessor.setBeanFactory(applicationContext);
//      List<NatsMessageConverter> natsMessageConverters = beanPostProcessor.getNatsMessageConverters();
//      Assert.assertNotNull(natsMessageConverters);
//      Assert.assertEquals(natsMessageConverters.size(), 2);
    }

    @Configuration
    public static class NatsListenerAnnotationBPPTestConfiguration {

      @Bean
      public List<NatsMessageConverter> converters() {
        return Arrays.asList(new NatsStringMessageConverter(), new ProtobufToByteArrayConverter());
      }

    }

  }

  @ContextConfiguration
  public static class Test2 extends AbstractTestNGSpringContextTests {

    @Test
    public void mustBeAbleToOverrideConvertersBeanWithCustomValue() {
      NatsListenerAnnotationBeanPostProcessor beanPostProcessor = new NatsListenerAnnotationBeanPostProcessor();
      beanPostProcessor.setBeanFactory(applicationContext);
//      List<NatsMessageConverter> natsMessageConverters = beanPostProcessor.getNatsMessageConverters();
//      Assert.assertNotNull(natsMessageConverters);
//      Assert.assertEquals(natsMessageConverters.size(), 3);
    }

    @Configuration
    public static class NatsListenerAnnotationBPPTestConfiguration {

      @Bean
      public List<NatsMessageConverter> converters() {
        return Arrays.asList(new NatsStringMessageConverter(), new ProtobufToByteArrayConverter());
      }

      @Bean
      public List<NatsMessageConverter> customConverterList() {
        return Arrays.asList(new NatsStringMessageConverter(), new ProtobufToByteArrayConverter(),
                             new NatsStringMessageConverter());
      }

      @Bean
      public String converterBeanName() {
        return "customConverterList";
      }
    }

  }


}