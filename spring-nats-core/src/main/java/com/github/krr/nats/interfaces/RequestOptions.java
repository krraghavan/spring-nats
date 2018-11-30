package com.github.krr.nats.interfaces;

public interface RequestOptions {

  default boolean isAsync() {
    return false;
  }

  RequestOptions DEFAULT_REQUEST_OPTIONS = new RequestOptions() {
    @Override
    public boolean isAsync() {
      return false;
    }
  };
}
