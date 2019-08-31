package info.faceland.strife.data.buff;

import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.util.LogUtil;
import java.util.HashMap;
import java.util.Map;

public class Buff {

  private final Map<StrifeStat, Float> buffStats;
  private final int maxStacks;

  private long endingTimestamp = System.currentTimeMillis();
  private int stacks;

  public Buff(Map<StrifeStat, Float> buffStats, int maxStacks) {
    this.buffStats = buffStats;
    this.stacks = 1;
    this.maxStacks = maxStacks;
  }

  public Map<StrifeStat, Float> getTotalStats() {
    Map<StrifeStat, Float> stackedStats = new HashMap<>(buffStats);
    if (stacks == 1) {
      return stackedStats;
    }
    for (StrifeStat stat : stackedStats.keySet()) {
      stackedStats.put(stat, stackedStats.get(stat) * stacks);
    }
    return stackedStats;
  }

  public long getEndingTimestamp() {
    return endingTimestamp;
  }

  public void bumpBuff(double duration) {
    setExpireTimeFromDuration(duration);
    stacks = Math.min(stacks+1, maxStacks);
    LogUtil.printDebug(" Stacks: " + stacks + "/" + maxStacks);
  }

  public int getStacks() {
    return stacks;
  }

  public boolean isExpired() {
    return System.currentTimeMillis() > endingTimestamp;
  }

  public void setExpireTimeFromDuration(double duration) {
    endingTimestamp = System.currentTimeMillis() + (long) (1000D * duration);
  }
}
