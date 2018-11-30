package com.github.krr.nats.connections;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

@Data
@RequiredArgsConstructor
@Slf4j
public class NatsCluster {

  public static final String NATS_PROTOCOL = "nats://";

  /**
   * The list of NATS natsCluster to connect to in the form <server>:<port>
   */
  private final String [] hosts;

  public String[] getHosts() {
    return Arrays.stream(hosts).map(NATS_PROTOCOL::concat).toArray(String[]::new);
  }

  public String getNatsUrl() {
    return StringUtils.join(getHosts(), ",");
  }
}
