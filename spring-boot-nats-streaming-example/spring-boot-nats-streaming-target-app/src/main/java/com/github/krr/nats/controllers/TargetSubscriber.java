package com.github.krr.nats.controllers;

import com.github.krr.nats.annotations.NatsListener;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TargetSubscriber {

  private int count = 0;

  @NatsListener(topic = "sampleTopic", type = NatsListener.NatsListenerType.SUBSCRIPTION)
  public void receive(TargetMessage targetMessage) {
    if(targetMessage != null) {
      log.error("Got message [{}] from source [{}]", count++, targetMessage);
      return;
    }
    log.error("Null Message received");
  }

  @SuppressWarnings("WeakerAccess")
  @Data
  @NoArgsConstructor
  @ToString
  public static class TargetMessage {

    private String message;

  }
}
