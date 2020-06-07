package land.face.strife.data;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import land.face.strife.data.ability.EntityAbilitySet;
import land.face.strife.data.buff.Buff;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.effects.FiniteUsesEffect;
import land.face.strife.managers.StatUpdateManager;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
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

  private WeakReference<Champion> champion;
  private WeakReference<LivingEntity> livingEntity;
  private EntityAbilitySet abilitySet;
  private String uniqueEntityId = null;
  private Set<String> mods = new HashSet<>();

  private Set<String> factions = new HashSet<>();
  private UUID alliedGuild;

  private Set<FiniteUsesEffect> tempEffects = new HashSet<>();

  private boolean despawnOnUnload = false;
  private boolean charmImmune = false;

  private WeakReference<LivingEntity> master;

  private final Set<StrifeMob> minions = new HashSet<>();
  private final Set<Buff> runningBuffs = new HashSet<>();

  private final Map<UUID, Float> takenDamage = new HashMap<>();

  private long buffCacheStamp = System.currentTimeMillis();

  public StrifeMob(Champion champion) {
    this.livingEntity = new WeakReference<>(champion.getPlayer());
    this.champion = new WeakReference<>(champion);
  }

  public StrifeMob(LivingEntity livingEntity) {
    this.livingEntity = new WeakReference<>(livingEntity);
    this.champion = new WeakReference<>(null);
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
    return livingEntity.get();
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

  public UUID getAlliedGuild() {
    return alliedGuild;
  }

  public void setAlliedGuild(UUID alliedGuild) {
    this.alliedGuild = alliedGuild;
  }

  public Set<String> getFactions() {
    return factions;
  }

  public void setFactions(Set<String> factions) {
    this.factions = factions;
  }

  public Champion getChampion() {
    return champion == null ? null : champion.get();
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

  public Buff hasBuff(String buffId, UUID source) {
    Iterator iterator = runningBuffs.iterator();
    while (iterator.hasNext()) {
      Buff buff = (Buff) iterator.next();
      if (buff == null || buff.isExpired()) {
        iterator.remove();
      } else {
        if (source == null) {
          if (buffId.equals(buff.getId()) && buff.getSource() == null) {
            return buff;
          }
        } else {
          if (buffId.equals(buff.getId()) && source.equals(buff.getSource())) {
            return buff;
          }
        }
      }
    }
    return null;
  }

  public int getBuffStacks(String buffId, UUID source) {
    Buff buff = hasBuff(buffId, source);
    if (buff == null) {
      return 0;
    }
    return buff.getStacks();
  }

  public void addBuff(String buffId, UUID source, Buff buff, double duration) {
    Buff oldBuff = hasBuff(buffId, source);
    if (oldBuff == null) {
      LogUtil.printDebug("Adding new buff: " + buffId + " to " + livingEntity.get().getName());
      buff.setExpireTimeFromDuration(duration);
      runningBuffs.add(buff);
      return;
    }
    LogUtil.printDebug("Bumping buff: " + buffId + " for " + livingEntity.get().getName());
    buff.bumpBuff(duration);
  }

  public boolean isMinionOf(StrifeMob strifeMob) {
    return getMaster() == strifeMob.getEntity();
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
    if (getChampion() == null) {
      return false;
    }
    return Objects.requireNonNull(champion.get()).hasTrait(trait);
  }

  public Set<StrifeMob> getMinions() {
    Iterator iterator = minions.iterator();
    while (iterator.hasNext()) {
      StrifeMob minion = (StrifeMob) iterator.next();
      if (minion == null || minion.getEntity() == null || !minion.getEntity().isValid()) {
        iterator.remove();
      }
    }
    return new HashSet<>(minions);
  }

  public void addMinion(StrifeMob strifeMob) {
    minions.add(strifeMob);
    strifeMob.forceSetStat(StrifeStat.MINION_MULT_INTERNAL, getStat(StrifeStat.MINION_DAMAGE));
    strifeMob.forceSetStat(StrifeStat.ACCURACY_MULT, 0f);
    strifeMob.forceSetStat(StrifeStat.ACCURACY, StatUtil.getAccuracy(this));
    strifeMob.setDespawnOnUnload(true);
    strifeMob.setMaster(livingEntity.get());
  }

  public LivingEntity getMaster() {
    return master == null ? null : master.get();
  }

  public void setMaster(LivingEntity master) {
    this.master = new WeakReference<>(master);
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

  public Map<StrifeStat, Float> getBuffStats() {
    Map<StrifeStat, Float> stats = new HashMap<>();
    Iterator iterator = runningBuffs.iterator();
    while (iterator.hasNext()) {
      Buff buff = (Buff) iterator.next();
      if (buff == null || buff.isExpired()) {
        iterator.remove();
      } else {
        for (StrifeStat stat : buff.getTotalStats().keySet()) {
          if (stats.containsKey(stat)) {
            stats.put(stat, stats.get(stat) + buff.getTotalStats().get(stat));
          } else {
            stats.put(stat, buff.getTotalStats().get(stat));
          }
        }
      }
    }
    return stats;
  }
}
