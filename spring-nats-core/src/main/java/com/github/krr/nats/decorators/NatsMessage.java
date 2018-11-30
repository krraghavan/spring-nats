package com.github.krr.nats.decorators;

/**
 * A marker interface designed to normalize the streaming and non-streaming
 * Message classes from the two diff Java client libraries
 *
 * @author raghavan
 */
public interface NatsMessage {

  byte [] getData();
}
