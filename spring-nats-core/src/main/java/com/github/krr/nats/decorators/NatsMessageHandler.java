package com.github.krr.nats.decorators;

/**
 * An interface to normalize the handling of subscription messages from two diff
 * types of NATS servers.  The consumers don't have to be concerned with whether
 * the underlying server is streaming or not.
 *
 * @param <T>
 *
 * @author raghavan
 */
@FunctionalInterface
public interface NatsMessageHandler<T> {

  void handleMessage(T message);

}
