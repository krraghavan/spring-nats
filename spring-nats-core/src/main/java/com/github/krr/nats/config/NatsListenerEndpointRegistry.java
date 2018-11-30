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

import com.github.krr.nats.annotations.NatsListener;
import com.github.krr.nats.interfaces.NatsEndpointListenerContainer;
import lombok.Getter;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * <Description>
 *
 * @author raghavan
 */
@SuppressWarnings({"FieldCanBeLocal", "WeakerAccess"})
public class NatsListenerEndpointRegistry implements DisposableBean, ApplicationListener<ContextRefreshedEvent> {

  @Getter
  private MultiValueMap<NatsListener, NatsEndpointListenerContainer> natsListenerMap = new LinkedMultiValueMap<>();

  @Override
  public void destroy() {

  }

  @Override
  public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {

  }

  public void registerListener(NatsListener listener, NatsEndpointListenerContainer endpointListenerContainer) {
    natsListenerMap.add(listener, endpointListenerContainer);
  }
}
