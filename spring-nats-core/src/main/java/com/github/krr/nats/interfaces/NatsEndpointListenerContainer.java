package com.github.krr.nats.interfaces;

import com.github.krr.nats.annotations.NatsListener;

import java.lang.reflect.Method;

public interface NatsEndpointListenerContainer {

  void start(NatsListener listener);

  void stop();

  Method getMethod();
}
