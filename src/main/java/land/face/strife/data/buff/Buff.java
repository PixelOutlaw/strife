package land.face.strife.data.buff;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import land.face.strife.stats.StrifeStat;

public class Buff {

  private final String id;

  private UUID source = null;
  private final Map<StrifeStat, Float> buffStats;
  private final int maxStacks;

  private long endingTimestamp = System.currentTimeMillis();
  private int stacks;

  public Buff(String id, Map<StrifeStat, Float> buffStats, int maxStacks) {
    this.id = id;
    this.buffStats = buffStats;
    this.stacks = 1;
    this.maxStacks = maxStacks;
  }

  public String getId() {
    return id;
  }

  public UUID getSource() {
    return source;
  }

  public void setSource(UUID source) {
    this.source = source;
  }

  public Map<StrifeStat, Float> getTotalStats() {
    Map<StrifeStat, Float> stackedStats = new HashMap<>(buffStats);
    if (stacks == 1) {
      return stackedStats;
    }
    stackedStats.replaceAll((stat, value) -> value * stacks);
    return stackedStats;
  }

  public long getEndingTimestamp() {
    return endingTimestamp;
  }

  public void bumpBuff(double duration) {
    setExpireTimeFromDuration(duration);
    stacks = Math.min(stacks + 1, maxStacks);
    //Bukkit.getLogger().warning(" Stacks: " + stacks + "/" + maxStacks);
  }

  public int getStacks() {
    return stacks;
  }

  public void setStacks(int stacks) {
    this.stacks = stacks;
  }

  public boolean isExpired() {
    return System.currentTimeMillis() > endingTimestamp;
  }

  public void setExpireTimeFromDuration(double duration) {
    endingTimestamp = System.currentTimeMillis() + (long) (1000D * duration);
  }
}
