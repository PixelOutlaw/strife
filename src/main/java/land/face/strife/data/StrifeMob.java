package land.face.strife.data;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import land.face.SnazzyPartiesPlugin;
import land.face.data.Party;
import land.face.strife.StrifePlugin;
import land.face.strife.data.ability.EntityAbilitySet;
import land.face.strife.data.buff.Buff;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.EquipmentCache;
import land.face.strife.data.effects.FiniteUsesEffect;
import land.face.strife.managers.LoreAbilityManager.TriggerType;
import land.face.strife.managers.StatUpdateManager;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.tasks.BarrierTask;
import land.face.strife.tasks.CombatCountdownTask;
import land.face.strife.tasks.EnergyTask;
import land.face.strife.tasks.LifeTask;
import land.face.strife.tasks.MinionTask;
import land.face.strife.util.SpecialStatusUtil;
import land.face.strife.util.StatUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class StrifeMob {

  private final static int CACHE_DELAY = 100;

  private final EquipmentCache equipmentCache = new EquipmentCache();
  private final Map<StrifeStat, Float> baseStats = new HashMap<>();
  private final Map<StrifeStat, Float> statCache = new HashMap<>();

  private String uniqueEntityId = null;

  private final WeakReference<Champion> champion;
  private final WeakReference<LivingEntity> livingEntity;

  private EntityAbilitySet abilitySet;
  private final Set<String> mods = new HashSet<>();
  private Set<String> factions = new HashSet<>();
  private UUID alliedGuild;
  private final Set<FiniteUsesEffect> tempEffects = new HashSet<>();
  private boolean charmImmune = false;
  private final Set<Buff> runningBuffs = new HashSet<>();
  private final Map<UUID, Float> takenDamage = new HashMap<>();

  private float energy = 0;
  private float maxEnergy = 0;
  private float barrier = 0;
  private float maxBarrier = 0;
  private boolean shielded;

  private boolean useEquipment;

  private CombatCountdownTask combatCountdownTask = null;

  private BarrierTask barrierTask = new BarrierTask(this);
  private LifeTask lifeTask = new LifeTask(this);
  private EnergyTask energyTask = new EnergyTask(this);
  private MinionTask minionTask = null;

  private final Set<StrifeMob> minions = new HashSet<>();

  private long cacheStamp = 1L;

  public StrifeMob(Champion champion) {
    this.livingEntity = new WeakReference<>(champion.getPlayer());
    this.champion = new WeakReference<>(champion);
    useEquipment = true;
  }

  public StrifeMob(LivingEntity livingEntity) {
    this.livingEntity = new WeakReference<>(livingEntity);
    this.champion = new WeakReference<>(null);
    useEquipment = livingEntity instanceof Player;
  }

  public float getBarrier() {
    return barrier;
  }

  public float getMaxBarrier() {
    return maxBarrier;
  }

  public void setMaxBarrier(float maxBarrier) {
    this.maxBarrier = maxBarrier;
  }

  public void restoreBarrier(float amount) {
    if (amount < 0) {
      Bukkit.getLogger().warning("Tried to restore a negative barrier amount!");
      return;
    }
    float maxBarrier = StatUtil.getMaximumBarrier(this);
    barrier = Math.min(barrier + amount, maxBarrier);
    barrierTask.updateArmorBar(this, barrier, maxBarrier);
  }

  public float damageBarrier(float amount) {
    if (amount < 0) {
      Bukkit.getLogger().warning("Tried to damage barrier by a negative amount!");
      return amount;
    }
    barrierTask.bumpBarrierTime();
    float diff = barrier - amount;
    if (diff > 0) {
      barrier -= amount;
      barrierTask.updateArmorBar(this, barrier, StatUtil.getMaximumBarrier(this));
      BarrierTask.spawnBarrierParticles(getEntity(), amount);
      return 0;
    } else {
      if (barrier > 0) {
        BarrierTask.spawnBarrierParticles(getEntity(), barrier);
      }
      barrier = 0;
      barrierTask.updateArmorBar(this, 0);
      return -1 * diff;
    }
  }

  public float getEnergy() {
    return energy;
  }

  public void setEnergy(float energy) {
    this.energy = Math.min(Math.max(0, energy), maxEnergy);
    if (getEntity() instanceof Player) {
      Player player = (Player) getEntity();
      player.setFoodLevel((int) Math.min(20D, 20D * energy / maxEnergy));
    }
  }

  public float getMaxEnergy() {
    return maxEnergy;
  }

  public void setMaxEnergy(float maxEnergy) {
    this.maxEnergy = maxEnergy;
  }

  public void trackDamage(StrifeMob attacker, float amount) {
    trackDamage(attacker.getEntity().getUniqueId(), amount);
  }

  public void trackDamage(UUID uuid, float amount) {
    takenDamage.put(uuid, takenDamage.getOrDefault(uuid, 0f) + amount);
  }

  public Player getTopDamager() {
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

  public static Set<Player> getKillers(StrifeMob mob) {
    Map<Party, Float> partyScores = new HashMap<>();
    Set<Player> killers = new HashSet<>();
    UUID topUUID = null;
    float topScore = 0;
    for (UUID uuid : mob.takenDamage.keySet()) {
      Player player = Bukkit.getPlayer(uuid);
      if (player == null || !player.isValid()) {
        continue;
      }
      float score = mob.takenDamage.get(uuid);
      Party party = SnazzyPartiesPlugin.getInstance().getPartyManager().getParty(player);
      if (party == null) {
        if (score > topScore) {
          killers.clear();
          killers.add(player);
          topScore = score;
          topUUID = player.getUniqueId();
        }
        continue;
      }
      float partyScore = partyScores.getOrDefault(party, 0f);
      partyScore += score;
      if (partyScore > topScore && topUUID != party.getLeader().getUUID()) {
        killers.clear();
        killers.addAll(SnazzyPartiesPlugin.getInstance().getPartyManager()
            .getNearbyPlayers(party, mob.getEntity().getLocation(), 30));
        topScore = partyScore;
        topUUID = party.getLeader().getUUID();
      }
      partyScores.put(party, partyScore);
    }
    return killers;
  }

  public float getStat(StrifeStat stat) {
    if (System.currentTimeMillis() >= cacheStamp) {
      statCache.clear();
      statCache.putAll(getFinalStats());
      cacheStamp = System.currentTimeMillis() + CACHE_DELAY;
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

  public boolean isUseEquipment() {
    return useEquipment;
  }

  public void setUseEquipment(boolean useEquipment) {
    this.useEquipment = useEquipment;
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
    if (getChampion() != null) {
      return StatUpdateManager.combineMaps(getChampion().getCombinedCache(), getBuffStats());
    }
    return StatUpdateManager.combineMaps(baseStats, getBuffStats(), equipmentCache.getCombinedStats());
  }

  public Map<StrifeStat, Float> getBaseStats() {
    return baseStats;
  }

  public void setStats(Map<StrifeStat, Float> stats) {
    baseStats.clear();
    baseStats.putAll(stats);
    cacheStamp = 1L;
  }

  public Buff getBuff(String buffId, UUID source) {
    Iterator<Buff> iterator = runningBuffs.iterator();
    while (iterator.hasNext()) {
      Buff buff = iterator.next();
      if (buff == null || buff.isExpired()) {
        iterator.remove();
        continue;
      }
      if (!buffId.equals(buff.getId())) {
        continue;
      }
      if (source == null) {
        if (buff.getSource() == null) {
          return buff;
        }
        continue;
      }
      if (source.equals(buff.getSource())) {
        return buff;
      }
    }
    return null;
  }

  public int getBuffStacks(String buffId, UUID source) {
    Buff buff = getBuff(buffId, source);
    if (buff == null) {
      return 0;
    }
    return buff.getStacks();
  }

  public void addBuff(Buff buff, double duration) {
    Buff oldBuff = getBuff(buff.getId(), buff.getSource());
    cacheStamp = 1L;
    if (oldBuff == null) {
      buff.setExpireTimeFromDuration(duration);
      runningBuffs.add(buff);
      return;
    }
    oldBuff.bumpBuff(duration);
  }

  public void removeBuff(String buffId, UUID source) {
    removeBuff(buffId, source, Integer.MAX_VALUE);
  }

  public void removeBuff(String buffId, UUID source, int stacks) {
    Buff buff = getBuff(buffId, source);
    if (buff == null) {
      return;
    }
    if (buff.getStacks() <= stacks) {
      runningBuffs.remove(buff);
      return;
    }
    buff.setStacks(buff.getStacks() - stacks);
  }

  public boolean isMinionOf(StrifeMob strifeMob) {
    return getMaster() == strifeMob.getEntity();
  }

  public boolean isMasterOf(StrifeMob strifeMob) {
    return strifeMob.getMaster() == this;
  }

  public boolean isMasterOf(LivingEntity entity) {
    for (StrifeMob strifeMob : minions) {
      if (strifeMob.getEntity() == entity) {
        return true;
      }
    }
    return false;
  }

  public EquipmentCache getEquipmentCache() {
    return equipmentCache;
  }

  public Map<TriggerType, Set<LoreAbility>> getLoreAbilities() {
    return equipmentCache.getCombinedAbilities();
  }

  public Set<StrifeTrait> getTraits() {
    Set<StrifeTrait> traits = new HashSet<>(equipmentCache.getCombinedTraits());
    if (champion.get() == null) {
      traits.addAll(Objects.requireNonNull(champion.get()).getPathTraits());
    }
    return traits;
  }

  public boolean hasTrait (StrifeTrait trait) {
    if (champion.get() == null) {
      return equipmentCache.getCombinedTraits().contains(trait);
    }
    return equipmentCache.getCombinedTraits().contains(trait) ||
        Objects.requireNonNull(champion.get()).getPathTraits().contains(trait);
  }

  public void removeMinion(StrifeMob minion) {
    minions.remove(minion);
  }

  public Set<StrifeMob> getMinions() {
    minions.removeIf(minion -> minion == null || minion.getEntity() == null || !minion.getEntity().isValid());
    return new HashSet<>(minions);
  }

  public void addMinion(StrifeMob minion, int lifespan) {
    minions.add(minion);
    minion.forceSetStat(StrifeStat.MINION_MULT_INTERNAL, getStat(StrifeStat.MINION_DAMAGE));
    minion.forceSetStat(StrifeStat.ACCURACY_MULT, 0f);
    minion.forceSetStat(StrifeStat.ACCURACY, StatUtil.getAccuracy(this));
    SpecialStatusUtil.setDespawnOnUnload(minion.getEntity());
    minion.setMaster(this, lifespan);
  }

  public StrifeMob getMaster() {
    return minionTask == null ? null : minionTask.getMaster();
  }

  public void setMaster(StrifeMob master, int lifespan) {
    minionTask = new MinionTask(this, master, lifespan);
  }

  public void addHealingOverTime(float amount, int ticks) {
    lifeTask.addHealingOverTime(amount, ticks);
  }

  public void addEnergyOverTime(float amount, int ticks) {
    energyTask.addEnergyOverTime(amount, ticks);
  }

  public Set<FiniteUsesEffect> getTempEffects() {
    return tempEffects;
  }

  public boolean isCharmImmune() {
    return charmImmune;
  }

  public void setCharmImmune(boolean charmImmune) {
    this.charmImmune = charmImmune;
  }

  public void minionDeath() {
    if (minionTask == null) {
      return;
    }
    minionTask.forceStartDeath();
  }

  public double getMinionRating() {
    if (minionTask == null) {
      // Arbitrary High Number
      return 10000000;
    }
    if (minionTask.getLifespan() < 1) {
      return 0;
    }
    return livingEntity.get().getHealth() * (1 + (double) minionTask.getLifespan() / 10D);
  }

  public void restartTimers() {
    if (lifeTask != null && !lifeTask.isCancelled()) {
      lifeTask.cancel();
    }
    lifeTask = new LifeTask(this);
    if (barrierTask != null && !barrierTask.isCancelled()) {
      barrierTask.cancel();
    }
    barrierTask = new BarrierTask(this);
    if (energyTask != null && !energyTask.isCancelled()) {
      energyTask.cancel();
    }
    energyTask = new EnergyTask(this);
  }

  public void bumpCombat() {
    if (isInCombat()) {
      combatCountdownTask.bump();
      return;
    }
    combatCountdownTask = new CombatCountdownTask(this);
    combatCountdownTask.runTaskTimer(StrifePlugin.getInstance(), 0L, 10L);
  }

  public void endCombat() {
    if (!combatCountdownTask.isCancelled()) {
      combatCountdownTask.cancel();
    }
    combatCountdownTask = null;
  }

  public boolean isInCombat() {
    return combatCountdownTask != null;
  }

  public Map<StrifeStat, Float> getBuffStats() {
    Map<StrifeStat, Float> stats = new HashMap<>();
    Iterator<Buff> iterator = runningBuffs.iterator();
    while (iterator.hasNext()) {
      Buff buff = iterator.next();
      if (buff == null || buff.isExpired()) {
        iterator.remove();
        continue;
      }
      stats.putAll(StatUpdateManager.combineMaps(stats, buff.getTotalStats()));
    }
    return stats;
  }
}
