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

package com.github.krr.nats.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 * <Description>
 *
 * @author raghavan
 */
@Configuration
public class NatsBootstrapConfiguration {

  @Bean(name = NatsConfigurationUtils.NATS_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME)
  @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
  public NatsListenerAnnotationBeanPostProcessor natsListenerAnnotationProcessor() {
    return new NatsListenerAnnotationBeanPostProcessor();
  }

  @Bean(name = NatsConfigurationUtils.NATS_LISTENER_ENDPOINT_REGISTRY_BEAN_NAME)
  public NatsListenerEndpointRegistry defaultNatsListenerEndpointRegistry() {
    return new NatsListenerEndpointRegistry();
  }

}
