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

/**
 * <Description>
 *
 * @author raghavan
 */
@SuppressWarnings("WeakerAccess")
public abstract class NatsConfigurationUtils {

  /**
   * The bean name of the internally managed Nats listener annotation processor.
   */
  public static final String NATS_LISTENER_ANNOTATION_PROCESSOR_BEAN_NAME = "com.github.krr.nats.config.internalNatsListenerAnnotationProcessor";

  /**
   * The bean name of the internally managed Nats listener endpoint registry.
   */
  public static final String NATS_LISTENER_ENDPOINT_REGISTRY_BEAN_NAME = "com.github.krr.nats.config.internalNatsListenerEndpointRegistry";
}
