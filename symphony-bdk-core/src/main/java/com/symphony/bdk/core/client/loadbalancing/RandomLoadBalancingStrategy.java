package com.symphony.bdk.core.client.loadbalancing;

import com.symphony.bdk.core.config.model.BdkServerConfig;

import org.apiguardian.api.API;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The {@link LoadBalancingStrategy} corresponding to the
 * {@link com.symphony.bdk.core.config.model.BdkLoadBalancingMode#RANDOM} mode.
 */
@API(status = API.Status.INTERNAL)
public class RandomLoadBalancingStrategy implements LoadBalancingStrategy {

  private final List<BdkServerConfig> nodes;
  private final ThreadLocalRandom localRandom;
  private AtomicInteger currentIndex;

  /**
   *
   * @param nodes the list of nodes to be load balanced across in a random way.
   */
  public RandomLoadBalancingStrategy(List<BdkServerConfig> nodes) {
    this.nodes = new ArrayList<>(nodes);
    this.currentIndex = new AtomicInteger(-1);
    this.localRandom = ThreadLocalRandom.current();
  }

  /**
   * Gets a new base path by taking a random item in {@link #nodes}.
   *
   * @return the base path of a randomly selected node.
   */
  @Override
  public String getNewBasePath() {
    final int newValue = localRandom.nextInt(0, nodes.size());
    currentIndex.set(newValue);

    return nodes.get(newValue).getBasePath();
  }
}
