package info.faceland.strife.data.buff;

import info.faceland.strife.stats.StrifeStat;
import java.util.Map;

public class LoadedBuff {

  private final String name;
  private final Map<StrifeStat, Float> stats;
  private final int maxStacks;
  private final float seconds;

  public LoadedBuff(String name, Map<StrifeStat, Float> stats, int maxStacks, float seconds) {
    this.name = name;
    this.stats = stats;
    this.maxStacks = maxStacks;
    this.seconds = seconds;
  }

  public String getName() {
    return name;
  }

  public Map<StrifeStat, Float> getStats() {
    return stats;
  }

  public int getMaxStacks() {
    return maxStacks;
  }

  public double getSeconds() {
    return seconds;
  }

}
