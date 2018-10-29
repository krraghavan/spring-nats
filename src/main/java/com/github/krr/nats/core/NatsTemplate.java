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

package com.github.krr.nats.core;

import com.github.krr.nats.interfaces.NatsMessageConverter;
import com.github.krr.nats.interfaces.NatsOperations;

import java.util.List;

/**
 * A concrete implementation of the NatsOperations class
 *
 * @author raghavan
 * @since 0.1
 */
public class NatsTemplate implements NatsOperations {

  private final List<NatsMessageConverter<?>> messageConverters;

  public NatsTemplate(List<NatsMessageConverter<?>> messageConverters) {
    this.messageConverters = messageConverters;
  }

  @Override
  public <O, I> O requestResponse(I request) {
    return null;
  }
}
