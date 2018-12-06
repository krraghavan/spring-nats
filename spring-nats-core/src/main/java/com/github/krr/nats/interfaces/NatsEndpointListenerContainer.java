package com.github.krr.nats.interfaces;

import java.lang.reflect.Method;

public interface NatsEndpointListenerContainer {

  void start();

  void stop();

  Method getMethod();
}
