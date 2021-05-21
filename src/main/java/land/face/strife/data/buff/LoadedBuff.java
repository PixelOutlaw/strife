package land.face.strife.data.buff;

import java.util.Map;
import land.face.strife.stats.StrifeStat;

public class LoadedBuff {

  private final String id;
  private final Map<StrifeStat, Float> stats;
  private final int maxStacks;
  private final float seconds;

  public LoadedBuff(String id, Map<StrifeStat, Float> stats, int maxStacks, float seconds) {
    this.id = id;
    this.stats = stats;
    this.maxStacks = maxStacks;
    this.seconds = seconds;
  }

  public String getId() {
    return id;
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

  public static Buff toRunningBuff(LoadedBuff buff) {
    return new Buff(buff.id, buff.stats, buff.maxStacks);
  }

}
