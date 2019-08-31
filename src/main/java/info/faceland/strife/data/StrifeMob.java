package info.faceland.strife.data;

import info.faceland.strife.data.ability.EntityAbilitySet;
import info.faceland.strife.data.buff.Buff;
import info.faceland.strife.data.champion.Champion;
import info.faceland.strife.managers.StatUpdateManager;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.stats.StrifeTrait;
import info.faceland.strife.util.LogUtil;
import io.netty.util.internal.ConcurrentSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.LivingEntity;

public class StrifeMob {

  private final static int BUFF_CHECK_FREQUENCY_MS = 100;

  private final Map<StrifeStat, Double> baseStats = new HashMap<>();
  private final Map<StrifeStat, Double> statCache = new HashMap<>();
  private final Map<StrifeStat, Double> tempBonuses = new HashMap<>();

  private final Champion champion;
  private LivingEntity livingEntity;
  private EntityAbilitySet abilitySet;

  private boolean despawnOnUnload = false;

  private LivingEntity master;
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
    if (BUFF_CHECK_FREQUENCY_MS < System.currentTimeMillis() - buffCacheStamp) {
      statCache.clear();
      statCache.putAll(getFinalStats());
      buffCacheStamp = System.currentTimeMillis();
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

  public EntityAbilitySet getAbilitySet() {
    return abilitySet;
  }

  public void setAbilitySet(EntityAbilitySet abilitySet) {
    this.abilitySet = abilitySet;
  }

  public Champion getChampion() {
    return champion;
  }

  public Map<StrifeStat, Double> getFinalStats() {
    return StatUpdateManager.combineMaps(baseStats, getBuffStats(), tempBonuses);
  }

  public Map<StrifeStat, Double> getBaseStats() {
    return baseStats;
  }

  public void setStats(Map<StrifeStat, Double> stats) {
    baseStats.clear();
    baseStats.putAll(stats);
  }

  public boolean hasBuff(String buffId) {
    if (runningBuffs.containsKey(buffId)) {
      if (runningBuffs.get(buffId).isExpired()) {
        runningBuffs.remove(buffId);
        return false;
      }
      return true;
    }
    return false;
  }

  public int getBuffStacks(String buffId) {
    if (hasBuff(buffId)) {
      return runningBuffs.get(buffId).getStacks();
    }
    return 0;
  }

  public void addBuff(String buffId, Buff buff, double duration) {
    if (runningBuffs.get(buffId) == null || runningBuffs.get(buffId).isExpired()) {
      buff.setExpireTimeFromDuration(duration);
      runningBuffs.put(buffId, buff);
      LogUtil.printDebug("Adding new buff: " + buffId + " to " + livingEntity.getName());
      return;
    }
    runningBuffs.get(buffId).bumpBuff(duration);
    LogUtil.printDebug("Bumping buff: " + buffId + " for " + livingEntity.getName());
  }

  public boolean isMinionOf(StrifeMob strifeMob) {
    return master == strifeMob.getEntity();
  }

  public boolean isMasterOf(StrifeMob strifeMob) {
    return strifeMob.getMaster() == livingEntity;
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

  public void addMinion(StrifeMob strifeMob) {
    minions.add(strifeMob);
    strifeMob.setMaster(livingEntity);
  }

  public LivingEntity getMaster() {
    return master;
  }

  public void setMaster(LivingEntity master) {
    this.master = master;
  }

  public boolean isDespawnOnUnload() {
    return despawnOnUnload;
  }

  public void setDespawnOnUnload(boolean despawnOnUnload) {
    this.despawnOnUnload = despawnOnUnload;
  }

  private Map<StrifeStat, Double> getBuffStats() {
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
