package com.github.krr.nats.listeners;

import com.github.krr.nats.annotations.NatsListener;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;

import java.lang.reflect.Method;

/**
 * Listens to a Nats message
 */
@Slf4j
@Data
@RequiredArgsConstructor
public class NatsEndpointListener {

  private final Method method;

  private final NatsListener natsListener;

  private final Object bean;

  private final String beanName;

  private BeanFactory beanFactory;

}
