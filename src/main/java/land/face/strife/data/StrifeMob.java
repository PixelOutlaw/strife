package land.face.strife.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import land.face.strife.data.ability.EntityAbilitySet;
import land.face.strife.data.buff.Buff;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.effects.FiniteUsesEffect;
import land.face.strife.managers.StatUpdateManager;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.timers.EntityAbilityTimer;
import land.face.strife.util.LogUtil;
import land.face.strife.util.StatUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class StrifeMob {

  private final static int BUFF_CHECK_FREQUENCY_MS = 100;

  private final Map<StrifeStat, Float> baseStats = new HashMap<>();
  private final Map<StrifeStat, Float> statCache = new HashMap<>();
  private final Map<StrifeStat, Float> tempBonuses = new HashMap<>();

  private final Champion champion;
  private LivingEntity livingEntity;
  private EntityAbilitySet abilitySet;
  private String uniqueEntityId = null;
  private Set<String> mods = new HashSet<>();
  private Set<String> factions = new HashSet<>();

  private Set<FiniteUsesEffect> tempEffects = new HashSet<>();

  private boolean despawnOnUnload = false;
  private boolean charmImmune = false;

  private LivingEntity master = null;

  private final Set<StrifeMob> minions = new HashSet<>();
  private final Map<String, Buff> runningBuffs = new ConcurrentHashMap<>();

  private final Map<UUID, Float> takenDamage = new HashMap<>();

  private EntityAbilityTimer abilityTimer;

  private long buffCacheStamp = System.currentTimeMillis();

  public StrifeMob(Champion champion) {
    this.livingEntity = champion.getPlayer();
    this.champion = champion;
  }

  public StrifeMob(LivingEntity livingEntity) {
    this.livingEntity = livingEntity;
    this.champion = null;
  }

  public void killAllTasks() {
    if (abilityTimer != null) {
      abilityTimer.cancel();
    }
  }

  public void trackDamage(StrifeMob attacker, float amount) {
    trackDamage(attacker.getEntity().getUniqueId(), amount);
  }

  public void trackDamage(UUID uuid, float amount) {
    takenDamage.put(uuid, takenDamage.getOrDefault(uuid, 0f) + amount);
  }

  public Player getKiller() {
    Player killer = null;
    float topDamage = 0;
    for (UUID uuid : takenDamage.keySet()) {
      Player p = Bukkit.getPlayer(uuid);
      if (p != null && takenDamage.get(uuid) > topDamage) {
        topDamage = takenDamage.get(uuid);
        killer = p;
      }
    }
    return killer;
  }

  public float getStat(StrifeStat stat) {
    if (runningBuffs.isEmpty()) {
      return baseStats.getOrDefault(stat, 0f);
    }
    if (System.currentTimeMillis() - buffCacheStamp > BUFF_CHECK_FREQUENCY_MS) {
      statCache.clear();
      statCache.putAll(getFinalStats());
      buffCacheStamp = System.currentTimeMillis();
    }
    return statCache.getOrDefault(stat, 0f);
  }

  public void forceSetStat(StrifeStat stat, float value) {
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

  public String getUniqueEntityId() {
    return uniqueEntityId;
  }

  public void setUniqueEntityId(String uniqueEntityId) {
    this.uniqueEntityId = uniqueEntityId;
  }

  public Set<String> getMods() {
    return mods;
  }

  public Set<String> getFactions() {
    return factions;
  }

  public void setFactions(Set<String> factions) {
    this.factions = factions;
  }

  public Champion getChampion() {
    return champion;
  }

  public Map<StrifeStat, Float> getFinalStats() {
    return StatUpdateManager.combineMaps(baseStats, getBuffStats(), tempBonuses);
  }

  public Map<StrifeStat, Float> getBaseStats() {
    return baseStats;
  }

  public void flattenBaseStats() {
    float flattenedArmor = baseStats.getOrDefault(StrifeStat.ARMOR, 0f) *
        (1 + (baseStats.get(StrifeStat.ARMOR_MULT) / 100));
    baseStats.put(StrifeStat.ARMOR, flattenedArmor);
    baseStats.remove(StrifeStat.ARMOR_MULT);
    float flattenedWarding = baseStats.getOrDefault(StrifeStat.WARDING, 0f) *
        (1 + (baseStats.get(StrifeStat.WARD_MULT) / 100));
    baseStats.put(StrifeStat.WARDING, flattenedWarding);
    baseStats.remove(StrifeStat.WARD_MULT);
    float flattenedRegen = baseStats.getOrDefault(StrifeStat.REGENERATION, 0f) *
        (1 + (baseStats.get(StrifeStat.REGEN_MULT) / 100));
    baseStats.put(StrifeStat.REGENERATION, flattenedRegen);
    baseStats.remove(StrifeStat.REGEN_MULT);
  }

  public void setStats(Map<StrifeStat, Float> stats) {
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
    Iterator<StrifeMob> it = minions.iterator();
    while (it.hasNext()) {
      StrifeMob minion = it.next();
      if (minion == null || minion.getEntity() == null || !minion.getEntity().isValid()) {
        minions.remove(minion);
      }
    }
    return minions;
  }

  public void addMinion(StrifeMob strifeMob) {
    minions.add(strifeMob);
    strifeMob.forceSetStat(StrifeStat.MINION_MULT_INTERNAL, getStat(StrifeStat.MINION_DAMAGE));
    strifeMob.forceSetStat(StrifeStat.ACCURACY_MULT, 0f);
    strifeMob.forceSetStat(StrifeStat.ACCURACY, StatUtil.getAccuracy(this));
    strifeMob.setDespawnOnUnload(true);
    strifeMob.setMaster(livingEntity);
  }

  public LivingEntity getMaster() {
    return master;
  }

  public void setMaster(LivingEntity master) {
    this.master = master;
  }

  public Set<FiniteUsesEffect> getTempEffects() {
    return tempEffects;
  }

  public boolean isDespawnOnUnload() {
    return despawnOnUnload;
  }

  public void setDespawnOnUnload(boolean despawnOnUnload) {
    this.despawnOnUnload = despawnOnUnload;
  }

  public boolean isCharmImmune() {
    return charmImmune;
  }

  public void setCharmImmune(boolean charmImmune) {
    this.charmImmune = charmImmune;
  }

  public void setAbilityTimer(EntityAbilityTimer abilityTimer) {
    this.abilityTimer = abilityTimer;
  }

  private Map<StrifeStat, Float> getBuffStats() {
    Map<StrifeStat, Float> stats = new HashMap<>();
    for (Buff buff : runningBuffs.values()) {
      if (buff.isExpired()) {
        runningBuffs.remove(buff.getId());
        continue;
      }
      for (StrifeStat stat : buff.getTotalStats().keySet()) {
        if (stats.containsKey(stat)) {
          stats.put(stat, stats.get(stat) + buff.getTotalStats().get(stat));
        } else {
          stats.put(stat, buff.getTotalStats().get(stat));
        }
      }
    }
    return stats;
  }
}
