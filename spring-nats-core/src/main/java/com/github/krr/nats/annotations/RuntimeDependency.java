package com.github.krr.nats.annotations;

import java.lang.annotation.*;

@SuppressWarnings("WeakerAccess")
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RuntimeDependency {

  String [] classes();
}
