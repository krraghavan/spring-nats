package com.github.krr.springboot.nats.rest.controllers;

import com.github.krr.nats.core.NatsTemplate;
import com.github.krr.nats.exceptions.MessageConversionException;
import com.github.krr.nats.exceptions.MessagePublishingException;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/source")
@Lazy
public class SourceRestController {

  private final NatsTemplate natsTemplate;

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Autowired
  public SourceRestController(NatsTemplate natsTemplate) {
    this.natsTemplate = natsTemplate;
  }

  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<SourceMessage> hello(@RequestBody SourceMessage sourceMessage) {

    try {
      sourceMessage.setMessage("From Source Application:".concat(sourceMessage.getMessage()));
      natsTemplate.publish("sampleTopic", sourceMessage);
      return ResponseEntity.ok()
                           .body(new SourceMessage("Message published - check target application"));
    }
    catch (MessagePublishingException | MessageConversionException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                           .body(new SourceMessage("Could not publish message.  Failed with exception:" + e.getMessage()));
    }
  }

  @SuppressWarnings("WeakerAccess")
  @Data
  @NoArgsConstructor
  public static class SourceMessage {

    private String message;

    public SourceMessage(String s) {
      this.message = s;
    }
  }
}
