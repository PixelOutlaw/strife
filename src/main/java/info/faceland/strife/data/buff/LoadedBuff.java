package info.faceland.strife.data.buff;

import info.faceland.strife.stats.StrifeStat;
import java.util.Map;

public class LoadedBuff {

  private final String name;
  private final Map<StrifeStat, Double> flatStats;
  private final Map<StrifeStat, Double> multStats;
  private final int maxStacks;
  private final int tickDuration;
  private final boolean slowFalloff;

  public LoadedBuff(String name, Map<StrifeStat, Double> flatStats,
      Map<StrifeStat, Double> multStats, int maxStacks, int tickDuration, boolean slowFalloff) {
    this.name = name;
    this.flatStats = flatStats;
    this.multStats = multStats;
    this.maxStacks = maxStacks;
    this.tickDuration = tickDuration;
    this.slowFalloff = slowFalloff;
  }

  public String getName() {
    return name;
  }

  public Map<StrifeStat, Double> getFlatStats() {
    return flatStats;
  }

  public Map<StrifeStat, Double> getMultStats() {
    return multStats;
  }

  public int getMaxStacks() {
    return maxStacks;
  }

  public int getTickDuration() {
    return tickDuration;
  }

  public boolean isSlowFalloff() {
    return slowFalloff;
  }
}
