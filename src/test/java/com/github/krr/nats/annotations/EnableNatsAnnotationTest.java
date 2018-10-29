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

package com.github.krr.nats.annotations;

import com.github.krr.nats.config.NatsListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <Description>
 *
 * @author raghavan
 */
@ContextConfiguration
@DirtiesContext
public class EnableNatsAnnotationTest extends AbstractTestNGSpringContextTests {

  @Autowired
  private Listener listener;

  @Autowired
  private NatsListenerEndpointRegistry endpointRegistry;


  @Test
  public void mustProcessNatsListenersWhenAnnotatedWithEnableNatsAnnotation() {

    Assert.assertEquals(getListenerCount(listener), applicationContext);
  }

  @Configuration
  @EnableNats
  public static class Config {

    @Bean
    public Listener listener() {
      return new Listener();
    }

  }

  @Component
  static class Listener {

    @NatsListener
    public String method1(String input) {
      return "hello";
    }
  }
}
