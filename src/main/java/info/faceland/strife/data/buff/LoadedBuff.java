package info.faceland.strife.data.buff;

import info.faceland.strife.stats.StrifeStat;
import java.util.Map;

public class LoadedBuff {

  private final String name;
  private final Map<StrifeStat, Double> stats;
  private final int maxStacks;
  private final double seconds;

  public LoadedBuff(String name, Map<StrifeStat, Double> stats, int maxStacks, double seconds) {
    this.name = name;
    this.stats = stats;
    this.maxStacks = maxStacks;
    this.seconds = seconds;
  }

  public String getName() {
    return name;
  }

  public Map<StrifeStat, Double> getStats() {
    return stats;
  }

  public int getMaxStacks() {
    return maxStacks;
  }

  public double getSeconds() {
    return seconds;
  }

  public static Buff createBuffFromLoadedBuff(LoadedBuff loadedBuff) {
    return new Buff(loadedBuff.stats, loadedBuff.maxStacks);
  }
}
