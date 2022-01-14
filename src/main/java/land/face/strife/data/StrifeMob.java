package land.face.strife.data;

import com.tealcube.minecraft.bukkit.facecore.utilities.ChunkUtil;
import com.ticxo.modelengine.api.model.ModeledEntity;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import land.face.SnazzyPartiesPlugin;
import land.face.data.Party;
import land.face.strife.StrifePlugin;
import land.face.strife.data.ability.EntityAbilitySet;
import land.face.strife.data.buff.Buff;
import land.face.strife.data.buff.LoadedBuff;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.EquipmentCache;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.managers.LoreAbilityManager.TriggerType;
import land.face.strife.managers.StatUpdateManager;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.tasks.BarrierTask;
import land.face.strife.tasks.CombatCountdownTask;
import land.face.strife.tasks.EnergyTask;
import land.face.strife.tasks.FrostTask;
import land.face.strife.tasks.LifeTask;
import land.face.strife.tasks.MinionTask;
import land.face.strife.util.SpecialStatusUtil;
import land.face.strife.util.StatUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class StrifeMob {

  private final EquipmentCache equipmentCache = new EquipmentCache();
  private final Map<StrifeStat, Float> baseStats = new HashMap<>();
  @Getter
  private final Map<StrifeStat, Float> statCache = new HashMap<>();

  private final WeakReference<Champion> champion;
  private final WeakReference<LivingEntity> livingEntity;
  private WeakReference<UniqueEntity> uniqueEntity = null;
  private WeakReference<Spawner> spawner = new WeakReference<>(null);
  private WeakReference<ModeledEntity> modelEntity = null;

  private EntityAbilitySet abilitySet;
  private final Set<String> mods = new HashSet<>();
  private Set<String> factions = new HashSet<>();
  @Getter @Setter
  private UUID alliedGuild;
  private boolean charmImmune = false;
  private final Set<Buff> runningBuffs = new HashSet<>();
  private final Map<UUID, Float> takenDamage = new HashMap<>();

  private float energy = 0;
  private float maxEnergy = 0;
  @Getter @Setter
  private float maxRage = 0;
  private float barrier = 0;
  @Getter @Setter
  private float maxLife = 1;
  private float maxBarrier = 0;
  private float block = 0;
  @Getter
  private float maxBlock = 1000;
  @Getter
  private int frost;
  @Getter
  private float corruption;
  @Getter
  private int maxAirJumps;
  private boolean shielded;

  private boolean useEquipment;

  private CombatCountdownTask combatCountdownTask = null;

  private BarrierTask barrierTask = new BarrierTask(this);
  private LifeTask lifeTask = new LifeTask(this);
  private EnergyTask energyTask = new EnergyTask(this);
  private FrostTask frostTask = new FrostTask(this);
  private MinionTask minionTask = null;

  private final Set<StrifeMob> minions = new HashSet<>();

  private boolean buffsChanged = false;
  private long lastEquipmentUpdate = 100L;
  private long lastChampionUpdate = 100L;

  private long globalCooldownStamp = 1L;

  private final Map<Integer, Integer> multishotMap = new HashMap<>();

  public StrifeMob(Champion champion) {
    this.livingEntity = new WeakReference<>(champion.getPlayer());
    this.champion = new WeakReference<>(champion);
    useEquipment = true;
    statCache.clear();
    statCache.putAll(getFinalStats());
  }

  public StrifeMob(LivingEntity livingEntity) {
    this.livingEntity = new WeakReference<>(livingEntity);
    this.champion = new WeakReference<>(null);
    useEquipment = livingEntity instanceof Player;
    statCache.clear();
    statCache.putAll(getFinalStats());
  }

  public boolean canAttack() {
    if (uniqueEntity == null || uniqueEntity.get() == null) {
      return true;
    }
    if (!uniqueEntity.get().isAttackDisabledOnGlobalCooldown()) {
      return true;
    }
    return isGlobalCooldownReady();
  }

  public void setUniqueEntity(UniqueEntity uniqueEntity) {
    this.uniqueEntity = new WeakReference<>(uniqueEntity);
  }

  public void bumpGlobalCooldown(int millis) {
    globalCooldownStamp = System.currentTimeMillis() + millis;
  }

  public boolean isGlobalCooldownReady() {
    return globalCooldownStamp < System.currentTimeMillis();
  }

  public float getBarrier() {
    return barrier;
  }

  public float getMaxBarrier() {
    return maxBarrier;
  }

  public float getBlock() {
    return block;
  }

  public void setBlock(float block) {
    this.block = Math.min(block, maxBlock);
  }

  public void setBarrier(float barrier) {
    this.barrier = Math.min(barrier, maxBarrier);
  }

  public void setFrost(float newFrost) {
    this.frost = Math.max(0, Math.min((int) newFrost, 10000));
    getEntity().setFreezeTicks((int) ((float) this.frost / 72f));
  }

  public float addCorruption(float amount) {
    corruption += amount;
    return corruption;
  }

  public float removeCorruption(float amount) {
    corruption = Math.max(0, corruption - amount);
    return corruption;
  }

  public void setCorruption(float amount) {
    corruption = Math.max(0, amount);
  }

  public void setMaxBarrier(float maxBarrier) {
    this.maxBarrier = maxBarrier;
    barrier = Math.min(barrier, maxBarrier);
  }

  public void setMaxBlock(float maxBlock) {
    this.maxBlock = maxBlock;
    setBlock(Math.min(block, maxBlock));
  }

  public void restoreBarrier(float amount) {
    if (amount < 0.01) {
      return;
    }
    barrier = Math.min(barrier + amount, maxBarrier);
    if (barrierTask != null) {
      barrierTask.forceAbsorbHearts();
    }
  }

  public float damageBarrier(float amount) {
    float barrierDelayMultiplier = 1 + getStat(StrifeStat.BARRIER_START_SPEED) / 100;
    barrierTask.bumpBarrierTime(barrierDelayMultiplier);
    if (amount < 0.01) {
      return amount;
    }
    float diff = barrier - amount;
    if (diff > 0) {
      barrier -= amount;
      if (barrierTask != null) {
        barrierTask.forceAbsorbHearts();
      }
      BarrierTask.spawnBarrierParticles(getEntity(), amount);
      return 0;
    } else {
      if (barrier > 0) {
        BarrierTask.spawnBarrierParticles(getEntity(), barrier);
      }
      barrier = 0;
      if (barrierTask != null) {
        barrierTask.forceAbsorbHearts();
      }
      return -1 * diff;
    }
  }

  public void updateBarrierScale() {
    if (getEntity() instanceof Player && barrierTask != null) {
      barrierTask.updateBarrierScale();
    }
  }

  public void setBarrierDelayTicks(int ticks) {
    if (barrierTask != null) {
      barrierTask.setDelayTicks(ticks);
    }
  }

  public float getEnergy() {
    return energy;
  }

  public void setEnergy(float energy) {
    this.energy = Math.min(Math.max(0, energy), maxEnergy);
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

  public boolean cacheUpdateRequired() {
    return buffsChanged || lastEquipmentUpdate != equipmentCache.getLastUpdate() ||
        (getChampion() != null && getChampion().getLastChanged() != lastChampionUpdate);
  }

  public float getStat(StrifeStat stat) {
    if (cacheUpdateRequired()) {
      statCache.clear();
      statCache.putAll(getFinalStats());
      buffsChanged = false;
      lastEquipmentUpdate = equipmentCache.getLastUpdate();
      if (getChampion() != null) {
        lastChampionUpdate = getChampion().getLastChanged();
      }
      setMaxBlock(StatUtil.getStat(this, StrifeStat.BLOCK));
      if (getChampion() != null) {
        int agilityLevel = getChampion().getLifeSkillLevel(LifeSkillType.AGILITY);
        if (agilityLevel < 40) {
          maxAirJumps = 0;
        } else {
          int amount = 1 + (int) getStat(StrifeStat.AIR_JUMPS);
          if (agilityLevel > 59) {
            amount++;
          }
          maxAirJumps = amount;
        }
      }
    }
    return StatUtil.getStat(this, stat);
  }

  public Spawner getSpawner() {
    return spawner.get();
  }

  public void setSpawner(Spawner spawner) {
    this.spawner = new WeakReference<>(spawner);
  }

  public ModeledEntity getModelEntity() {
    if (modelEntity == null) {
      return null;
    }
    return modelEntity.get();
  }

  public void setModelEntity(ModeledEntity modelEntity) {
    this.modelEntity = new WeakReference<>(modelEntity);
  }

  public void forceSetStat(StrifeStat stat, float value) {
    baseStats.put(stat, value);
    buffsChanged = true;
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
    if (uniqueEntity == null) {
      return null;
    }
    return uniqueEntity.get().getId();
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
    return champion == null ? null : champion.get();
  }

  public Set<Buff> getBuffs() {
    return new HashSet<>(runningBuffs);
  }

  public void clearBuffs() {
    for (Buff buff : runningBuffs) {
      buff.cancel();
    }
    runningBuffs.clear();
    buffsChanged = true;
  }

  public Map<StrifeStat, Float> getFinalStats() {
    if (getChampion() != null) {
      return StatUpdateManager.combineMaps(getChampion().getCombinedCache(),
          getBuffStats(), equipmentCache.getCombinedStats());
    }
    return StatUpdateManager.combineMaps(baseStats, getBuffStats(),
        equipmentCache.getCombinedStats());
  }

  public Map<StrifeStat, Float> getBaseStats() {
    return baseStats;
  }

  public void setStats(Map<StrifeStat, Float> stats) {
    baseStats.clear();
    baseStats.putAll(stats);
    statCache.clear();
    statCache.putAll(getFinalStats());
  }

  public Buff getBuff(String buffId, UUID source) {
    for (Buff buff : runningBuffs) {
      if (buff.getId().equals(buffId)) {
        if ((buff.getSource() == null && source == null) || buff.getSource().equals(source)) {
          return buff;
        }
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

  public void addBuff(LoadedBuff loadedBuff, UUID source, float duration) {
    Buff buff = LoadedBuff.toRunningBuff(this, source, duration, loadedBuff);
    Buff oldBuff = getBuff(buff.getId(), buff.getSource());
    buffsChanged = true;
    if (oldBuff == null) {
      buff.refreshBuff(duration);
      runningBuffs.add(buff);
      return;
    }
    oldBuff.bumpBuff(duration);
  }

  public void removeBuff(String buffId, UUID source) {
    Buff buff = getBuff(buffId, source);
    if (buff == null) {
      return;
    }
    removeBuff(buff);
  }

  public void removeBuff(String buffId, UUID source, int stacks) {
    Buff buff = getBuff(buffId, source);
    if (buff == null) {
      return;
    }
    removeBuff(buff, stacks);
  }

  public void removeBuff(Buff buff) {
    buff.cancel();
    runningBuffs.remove(buff);
    buffsChanged = true;
  }

  public void removeBuff(Buff buff, int stacks) {
    if (buff.getStacks() <= stacks) {
      buff.cancel();
      runningBuffs.remove(buff);
      buffsChanged = true;
      return;
    }
    buff.setStacks(buff.getStacks() - stacks);
    buffsChanged = true;
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

  public Set<LoreAbility> getLoreAbilities() {
    Set<LoreAbility> abilities = new HashSet<>();
    for (Buff buff : runningBuffs) {
      abilities.addAll(buff.getAbilities());
    }
    abilities.addAll(equipmentCache.getCombinedAbilities());
    return abilities;
  }

  public Set<LoreAbility> getLoreAbilities(TriggerType triggerType) {
    return getLoreAbilities().stream().filter(loreAbility ->
        loreAbility.getTriggerType() == triggerType).collect(Collectors.toSet());
  }

  public Set<StrifeTrait> getTraits() {
    Set<StrifeTrait> traits = new HashSet<>(equipmentCache.getCombinedTraits());
    if (champion.get() != null) {
      traits.addAll(Objects.requireNonNull(champion.get()).getPathTraits());
    }
    return traits;
  }

  public boolean hasTrait(StrifeTrait trait) {
    for (Buff buff : runningBuffs) {
      if (buff.getTraits().contains(trait)) {
        return true;
      }
    }
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
    ChunkUtil.setDespawnOnUnload(minion.getEntity());
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
    if (minionTask == null || !getEntity().getPassengers().isEmpty()) {
      // Arbitrary High Number
      return 10000000;
    }
    if (minionTask.getLifespan() < 1) {
      return 0;
    }
    return livingEntity.get().getHealth() * (1 + (double) minionTask.getLifespan() / 10D);
  }

  public float getMultishotRatio(int shotId) {
    if (multishotMap.containsKey(shotId)) {
      int amount = multishotMap.get(shotId);
      multishotMap.put(shotId, amount + 1);
      return switch (amount) {
        case 1 -> 0.3f;
        case 2 -> 0.1f;
        default -> 0;
      };
    }
    multishotMap.put(shotId, 1);
    return 1;
  }

  public void clearMultishot() {
    multishotMap.clear();
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

    if (frostTask != null && !frostTask.isCancelled()) {
      frostTask.cancel();
    }
    frostTask = new FrostTask(this);
  }

  public void bumpCombat() {
    bumpCombat(false);
  }

  public void bumpCombat(boolean pvp) {
    if (isInCombat()) {
      combatCountdownTask.bump();
      if (pvp) {
        combatCountdownTask.setPvp();
      }
      return;
    }
    combatCountdownTask = new CombatCountdownTask(this);
    if (pvp) {
      combatCountdownTask.setPvp();
    }
    combatCountdownTask.runTaskTimer(StrifePlugin.getInstance(), 0L, 10L);
  }

  public void flagPvp() {
    if (livingEntity.get() instanceof Player && isInCombat()) {
      combatCountdownTask.setPvp();
    }
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

  public boolean isInPvp() {
    return combatCountdownTask != null && combatCountdownTask.isPvp();
  }

  public boolean diedFromPvp() {
    return getEntity() instanceof Player && getEntity().getKiller() != null || isInPvp();
  }

  public Map<StrifeStat, Float> getBuffStats() {
    Map<StrifeStat, Float> stats = new HashMap<>();
    for (Buff buff : runningBuffs) {
      stats.putAll(StatUpdateManager.combineMaps(stats, buff.getTotalStats()));
    }
    return stats;
  }
}
