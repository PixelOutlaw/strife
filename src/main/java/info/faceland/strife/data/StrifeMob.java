package info.faceland.strife.data;

import info.faceland.strife.data.buff.Buff;
import info.faceland.strife.data.champion.Champion;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.stats.StrifeTrait;
import io.netty.util.internal.ConcurrentSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.LivingEntity;

public class StrifeMob {

  private final static int BUFF_CHECK_FREQUANCY_MS = 200;

  private final Map<StrifeStat, Double> baseStats = new HashMap<>();
  private final Map<StrifeStat, Double> statCache = new HashMap<>();

  private final Champion champion;
  private LivingEntity livingEntity;

  private boolean despawnOnUnload = false;

  private final Set<StrifeMob> minions = new ConcurrentSet<>();
  private final Map<String, Buff> runningBuffs = new ConcurrentHashMap<>();

  private long buffCacheStamp = System.currentTimeMillis();

  public StrifeMob(Champion champion) {
    this.livingEntity = champion.getPlayer();
    this.champion = champion;
  }

  public StrifeMob(LivingEntity livingEntity) {
    this.livingEntity = livingEntity;
    this.champion = null;
  }

  public double getStat(StrifeStat stat) {
    if (runningBuffs.isEmpty()) {
      return baseStats.getOrDefault(stat, 0D);
    }
    if (System.currentTimeMillis() - buffCacheStamp > BUFF_CHECK_FREQUANCY_MS) {
      statCache.clear();
      statCache.putAll(getFinalStats());
    }
    return statCache.getOrDefault(stat, 0D);
  }

  public void forceSetStat(StrifeStat stat, double value) {
    baseStats.put(stat, value);
  }

  public LivingEntity getEntity() {
    return livingEntity;
  }

  public void setLivingEntity(LivingEntity livingEntity) {
    this.livingEntity = livingEntity;
  }

  public Champion getChampion() {
    return champion;
  }

  public Map<StrifeStat, Double> getFinalStats() {
    Map<StrifeStat, Double> returnMap = new HashMap<>(baseStats);
    returnMap.putAll(getBuffStats());
    return returnMap;
  }

  public void setStats(Map<StrifeStat, Double> stats) {
    baseStats.clear();
    baseStats.putAll(stats);
  }

  public void addBuff(String buffId, Buff buff, double duration) {
    duration *= 1 + getStat(StrifeStat.EFFECT_DURATION) / 100;
    if (runningBuffs.get(buffId) == null || runningBuffs.get(buffId).isExpired()) {
      buff.setExpireTimeFromDuration(duration);
      runningBuffs.put(buffId, buff);
      return;
    }
    runningBuffs.get(buffId).bumpBuff(duration);
  }

  public boolean isMinionOf(StrifeMob strifeMob) {
    return strifeMob.getMinions().contains(this);
  }

  public boolean isMasterOf(StrifeMob strifeMob) {
    return getMinions().contains(strifeMob);
  }

  public boolean isMasterOf(LivingEntity entity) {
    for (StrifeMob strifeMob : minions) {
      if (strifeMob.getEntity() == entity) {
        return true;
      }
    }
    return false;
  }

  public boolean hasTrait(StrifeTrait trait) {
    if (champion == null) {
      return false;
    }
    return champion.hasTrait(trait);
  }

  public Set<StrifeMob> getMinions() {
    for (StrifeMob minion : minions) {
      if (minion == null || minion.getEntity() == null || !minion.getEntity().isValid()) {
        minions.remove(minion);
      }
    }
    return minions;
  }

  public boolean isDespawnOnUnload() {
    return despawnOnUnload;
  }

  public void setDespawnOnUnload(boolean despawnOnUnload) {
    this.despawnOnUnload = despawnOnUnload;
  }

  public Map<StrifeStat, Double> getBuffStats() {
    Map<StrifeStat, Double> stats = new HashMap<>();
    for (String buffId : runningBuffs.keySet()) {
      if (runningBuffs.get(buffId).isExpired()) {
        runningBuffs.remove(buffId);
        continue;
      }
      stats.putAll(runningBuffs.get(buffId).getTotalStats());
    }
    return stats;
  }
}
