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


import com.github.krr.nats.config.NatsBootstrapConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enables Nats support by injecting the bean post processor to process
 * @see NatsListener annotations on registered connections.
 *
 * @author raghavan
 * @since 0.1
 */
@SuppressWarnings("WeakerAccess")
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(NatsBootstrapConfiguration.class)
public @interface EnableNats {
}
