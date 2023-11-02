package land.face.strife.data;

import com.sentropic.guiapi.gui.Alignment;
import com.sentropic.guiapi.gui.GUIComponent;
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
import land.face.strife.events.BlockChangeEvent;
import land.face.strife.events.RuneChangeEvent;
import land.face.strife.managers.GuiManager;
import land.face.strife.managers.LoreAbilityManager.TriggerType;
import land.face.strife.managers.StatUpdateManager;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.tasks.BarrierTask;
import land.face.strife.tasks.CombatCountdownTask;
import land.face.strife.tasks.EnergyTask;
import land.face.strife.tasks.FrostTask;
import land.face.strife.tasks.InvincibleTask;
import land.face.strife.tasks.LifeTask;
import land.face.strife.tasks.MinionTask;
import land.face.strife.tasks.RageTask;
import land.face.strife.tasks.ThreatTask;
import land.face.strife.util.SpecialStatusUtil;
import land.face.strife.util.StatUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

public class StrifeMob {

  @Getter
  private final EquipmentCache equipmentCache = new EquipmentCache();
  @Getter
  private final Map<StrifeStat, Float> baseStats = new HashMap<>();
  @Getter
  private final Map<StrifeStat, Float> statCache = new HashMap<>();

  @Getter @Setter
  private int expiryAge = 0;

  private Champion champion;
  private final WeakReference<LivingEntity> livingEntity;
  private WeakReference<UniqueEntity> uniqueEntity = null;
  private WeakReference<Spawner> spawner = new WeakReference<>(null);
  private WeakReference<ModeledEntity> modelEntity = null;
  private WeakReference<StrifeMob> owner = new WeakReference<>(null);

  @Getter
  private EntityAbilitySet abilitySet;
  @Getter
  private final Set<String> mods = new HashSet<>();
  @Getter @Setter
  private Set<String> factions = new HashSet<>();
  @Getter @Setter
  private UUID alliedGuild;
  @Getter @Setter
  private boolean charmImmune = false;
  private final Set<Buff> runningBuffs = new HashSet<>();
  private final Map<UUID, Float> takenDamage = new HashMap<>();
  private final Set<UUID> reflectedTargets = new HashSet<>();

  @Getter @Setter
  private float threatLevel = 0;
  @Getter
  private final Map<UUID, Float> threatTargets = new HashMap<>();

  @Getter @Setter
  private float maxPrayer;
  @Getter
  private float energy = 0;
  @Getter
  private float maxEnergy = 0;
  @Getter @Setter
  private float maxRage = 0;
  @Getter @Setter
  private int maxEarthRunes = 3;
  @Getter
  private int earthRunes = 0;
  @Getter
  private float barrier = 0;
  @Getter @Setter
  private float maxLife = 1;
  @Getter
  private float maxBarrier = 0;
  @Getter
  private float block = 0;
  @Getter
  private float maxBlock = 1000;
  @Getter
  private float frost;
  @Getter @Setter
  private int frostGraceTicks;
  @Getter
  private float corruption;
  @Getter @Setter
  private float bleed;
  @Getter
  private int maxAirJumps;

  @Getter
  private boolean useEquipment;

  private CombatCountdownTask combatCountdownTask = null;

  private BarrierTask barrierTask = new BarrierTask(this);
  private LifeTask lifeTask = new LifeTask(this);
  private EnergyTask energyTask = new EnergyTask(this);
  private FrostTask frostTask = new FrostTask(this);
  private ThreatTask threatTask = new ThreatTask(this);
  @Getter
  private RageTask rageTask = null;
  private InvincibleTask invincibleTask = null;
  private MinionTask minionTask = null;

  private final Set<StrifeMob> minions = new HashSet<>();

  @Setter
  private boolean reCache = false;
  private long lastEquipmentUpdate = 100L;
  private long lastChampionUpdate = 100L;

  private long globalCooldownStamp = 1L;

  private final Map<Integer, Integer> multishotMap = new HashMap<>();

  public StrifeMob(LivingEntity livingEntity, Champion champion) {
    this.livingEntity = new WeakReference<>(livingEntity);
    this.champion = champion;
    useEquipment = champion != null;
    statCache.clear();
    statCache.putAll(getFinalStats());
    //Bukkit.getLogger().info("[Strife] StrMob created! Type:" + livingEntity.getType() + " name:" + livingEntity.getName() + " loc:" + livingEntity.getLocation().toVector());
  }

  public int getLevel() {
    if (livingEntity.get() instanceof Player) {
      //noinspection DataFlowIssue
      return ((Player) livingEntity.get()).getLevel();
    }
    return SpecialStatusUtil.getMobLevel(livingEntity.get());
  }

  public boolean canAttack() {
    if (uniqueEntity == null || uniqueEntity.get() == null) {
      return true;
    }
    //noinspection DataFlowIssue
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

  public void setBlock(float block) {
    float start = this.block;
    this.block = Math.min(block, maxBlock);
    if (start != block) {
      Bukkit.getPluginManager().callEvent(new BlockChangeEvent(this, start, this.block));
    }
  }

  public void setBarrier(float barrier) {
    this.barrier = Math.min(barrier, maxBarrier);
  }

  public void setEarthRunes(int runes) {
    int start = earthRunes;
    this.earthRunes = Math.max(0, Math.min(runes, maxEarthRunes));
    if (start != earthRunes) {
      Bukkit.getPluginManager().callEvent(new RuneChangeEvent(this, start, earthRunes));
    }
  }

  private void updateFrostDisplay() {
    getEntity().setFreezeTicks((int) (140f * frost / 100f));
  }

  public void addFrost(float added) {
    frostGraceTicks = 2;
    added *= 1 - getStat(StrifeStat.ICE_RESIST) / 100;
    setFrost(frost + added);
  }

  public void removeFrost(float subbed) {
    frost = Math.max(frost - subbed, 0);
    updateFrostDisplay();
  }

  public void setFrost(float value) {
    frost = Math.max(0, Math.min(value, 100));
    updateFrostDisplay();
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
      BarrierTask.spawnBarrierParticles(getEntity(), amount);
      return 0;
    } else {
      if (barrier > 0.5) {
        BarrierTask.spawnBarrierParticles(getEntity(), barrier);
      }
      barrier = 0;
      return -1 * diff;
    }
  }

  public void setBarrierDelayTicks(int ticks) {
    if (barrierTask != null) {
      barrierTask.setDelayTicks(ticks);
    }
  }

  public void setEnergy(float energy) {
    this.energy = Math.min(Math.max(0, energy), maxEnergy);
  }

  public void setMaxEnergy(float maxEnergy) {
    this.maxEnergy = maxEnergy;
  }

  public float getPrayer() {
    if (champion == null) {
      return 0;
    }
    return champion.getSaveData().getPrayerPoints();
  }

  public void setPrayer(float amount) {
    if (champion == null) {
      return;
    }
    champion.getSaveData().setPrayerPoints(Math.min(maxPrayer, amount));
  }

  public float getRage() {
    if (rageTask == null) {
      return 0;
    }
    return rageTask.getRage();
  }

  public void endRageTask() {
    if (rageTask != null) {
      if (getEntity() instanceof Player) {
        StrifePlugin.getInstance().getGuiManager().getGui((Player) getEntity()).update(
                new GUIComponent("rage-bar", GuiManager.EMPTY, 0, 0, Alignment.CENTER));
      }
      rageTask.cancel();
      rageTask = null;
    }
  }

  public void changeRage(float amount) {
    if (amount > 0) {
      if (rageTask == null) {
        rageTask = new RageTask(this);
      }
      if (hasTrait(StrifeTrait.BLOOD_BOIL) && bleed > 0) {
        amount *= 1.3f;
      }
      rageTask.bumpRage(amount);
    } else {
      if (rageTask == null) {
        return;
      }
      rageTask.reduceRage(-amount);
    }
  }

  public boolean isBleeding() {
    return bleed > 0;
  }

  public void addBleed(float amount) {
    addBleed(amount, false);
  }

  public void addBleed(float amount, boolean overrideBarrier) {
    if (!overrideBarrier && barrier > 0.9) {
      return;
    }
    bleed += amount;
  }

  public void clearBleed() {
    bleed = 0;
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
    return reCache || lastEquipmentUpdate != equipmentCache.getLastUpdate() ||
        (getChampion() != null && getChampion().getLastChanged() != lastChampionUpdate);
  }

  public float getStat(StrifeStat stat) {
    if (cacheUpdateRequired()) {
      statCache.clear();
      statCache.putAll(getFinalStats());
      reCache = false;
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

  public StrifeMob getOwner() {
    if (owner == null) {
      return null;
    }
    return owner.get();
  }

  public void setOwner(StrifeMob owner) {
    this.owner = new WeakReference<>(owner);
  }

  public void forceSetStat(StrifeStat stat, float value) {
    baseStats.put(stat, value);
    reCache = true;
  }

  public LivingEntity getEntity() {
    return livingEntity.get();
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

  public UniqueEntity getUniqueEntity() {
    return uniqueEntity == null ? null : uniqueEntity.get();
  }

  public Champion getChampion() {
    Champion result = champion == null ? null : champion;
    if (result == null && getEntity() instanceof Player) {
      Bukkit.getLogger().warning("[Strife] Invalid champion for " + getEntity().getName()+ "... Resolving...");
      champion = StrifePlugin.getInstance().getChampionManager().getChampion((Player) getEntity());
      return champion;
    }
    return result;
  }

  public boolean isPlayer() {
    return getChampion() != null;
  }

  public boolean canReflectAt(UUID uuid) {
    return !reflectedTargets.contains(uuid);
  }

  public void cacheReflect(UUID uuid) {
    reflectedTargets.add(uuid);
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(),
        () -> reflectedTargets.remove(uuid), 30L);
  }

  public Set<Buff> getBuffs() {
    return new HashSet<>(runningBuffs);
  }

  public void clearBuffs() {
    for (Buff buff : runningBuffs) {
      buff.cancel();
    }
    runningBuffs.clear();
    reCache = true;
  }

  public Map<StrifeStat, Float> getFinalStats() {
    if (getChampion() != null) {
      return StatUpdateManager.combineMaps(getChampion().getCombinedCache(),
          getBuffStats(), equipmentCache.getCombinedStats());
    }
    return StatUpdateManager.combineMaps(baseStats, getBuffStats(),
        equipmentCache.getCombinedStats());
  }

  public void setStats(Map<StrifeStat, Float> stats) {
    baseStats.clear();
    baseStats.putAll(stats);
    statCache.clear();
    statCache.putAll(getFinalStats());
  }

  public boolean isInvincible() {
    return invincibleTask != null;
  }

  public void applyInvincible(int ticks) {
    if (ticks < 1) {
      Bukkit.getLogger().info("[Strife] You may not apply invincibility shorter than 1 tick");
      return;
    }
    if (invincibleTask == null) {
      invincibleTask = new InvincibleTask(this, ticks);
    } else {
      invincibleTask.bump(ticks);
    }
  }

  public void cancelInvincibility() {
    if (invincibleTask != null) {
      invincibleTask.sendGuiUpdate();
      if (invincibleTask.isCancelled()) {
        invincibleTask.cancel();
      }
      invincibleTask = null;
    }
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
    reCache = true;
    if (oldBuff == null) {
      buff.refreshBuff(duration);
      runningBuffs.add(buff);
      return;
    }
    oldBuff.bumpBuff(duration);
    if (buff.getBuffStats().containsKey(StrifeStat.BARRIER)) {
      getStat(StrifeStat.BARRIER);
      StatUtil.getStat(this, StrifeStat.BARRIER);
    }
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
    reCache = true;
    if (buff.getBuffStats().containsKey(StrifeStat.BARRIER)) {
      getStat(StrifeStat.BARRIER);
      StatUtil.getStat(this, StrifeStat.BARRIER);
    }
  }

  public void removeBuff(Buff buff, int stacks) {
    if (buff.getStacks() <= stacks) {
      buff.cancel();
      runningBuffs.remove(buff);
      reCache = true;
      return;
    }
    buff.setStacks(buff.getStacks() - stacks);
    reCache = true;
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
    if (champion != null) {
      traits.addAll(Objects.requireNonNull(champion).getPathTraits());
    }
    return traits;
  }

  public boolean hasTrait(StrifeTrait trait) {
    for (Buff buff : runningBuffs) {
      if (buff.getTraits().contains(trait)) {
        return true;
      }
    }
    if (champion == null) {
      return equipmentCache.getCombinedTraits().contains(trait);
    }
    return equipmentCache.getCombinedTraits().contains(trait) ||
        Objects.requireNonNull(champion).getPathTraits().contains(trait);
  }

  public void removeMinion(StrifeMob minion) {
    minions.remove(minion);
  }

  public Set<StrifeMob> getMinions() {
    minions.removeIf(minion -> minion == null || minion.getEntity() == null || !minion.getEntity().isValid());
    return new HashSet<>(minions);
  }

  public void addMinion(StrifeMob minion, int lifespan, boolean thrall) {
    minions.add(minion);
    float newMaxLife = minion.getStat(StrifeStat.HEALTH);
    if (thrall) {
      minion.forceSetStat(StrifeStat.MINION_MULT_INTERNAL, getStat(StrifeStat.MINION_DAMAGE) / 10);
      newMaxLife = newMaxLife * (1 + getStat(StrifeStat.MINION_LIFE) / 1000);
    } else {
      minion.forceSetStat(StrifeStat.MINION_MULT_INTERNAL, getStat(StrifeStat.MINION_DAMAGE));
      newMaxLife = newMaxLife * (1 + getStat(StrifeStat.MINION_LIFE) / 100);
    }
    minion.forceSetStat(StrifeStat.HEALTH, newMaxLife);
    minion.forceSetStat(StrifeStat.HEALTH_MULT, 0);
    minion.setMaxLife(newMaxLife);
    //noinspection DataFlowIssue
    minion.getEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(newMaxLife);
    minion.getEntity().setHealth(newMaxLife);
    minion.forceSetStat(StrifeStat.ACCURACY_MULT, 0f);
    minion.forceSetStat(StrifeStat.ACCURACY, StatUtil.getStat(this, StrifeStat.ACCURACY));
    SpecialStatusUtil.setMobLevel(minion.getEntity(), getLevel());
    ChunkUtil.setDespawnOnUnload(minion.getEntity());
    minion.setMaster(this, lifespan);
  }

  public StrifeMob getMaster() {
    return minionTask == null ? null : minionTask.getMaster();
  }

  public void setMaster(StrifeMob master, int lifespan) {
    minionTask = new MinionTask(this, master, lifespan);
    if (getEntity() instanceof Mob) {
      getFactions().clear();
      ((Mob) getEntity()).setTarget(null);
    }
  }

  public void addHealingOverTime(float amount, int ticks) {
    lifeTask.addHealingOverTime(amount, ticks);
  }

  public void addEnergyOverTime(float amount, int ticks) {
    energyTask.addEnergyOverTime(amount, ticks);
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
    //noinspection DataFlowIssue
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

    if (threatTask != null && !threatTask.isCancelled()) {
      threatTask.cancel();
    }
    threatTask = new ThreatTask(this);

    if (rageTask != null) {
      rageTask.reduceRage(1000000);
      rageTask.cancel();
    }
  }

  public void bumpCombat(StrifeMob mob) {
    bumpCombat(mob, false);
  }

  public void bumpCombat(StrifeMob mob, boolean pvp) {
    if (isInCombat()) {
      combatCountdownTask.bump(mob);
      if (pvp) {
        combatCountdownTask.setPvp();
      }
      return;
    }
    combatCountdownTask = new CombatCountdownTask(this, mob);
    if (pvp) {
      combatCountdownTask.setPvp();
    }
    combatCountdownTask.runTaskTimer(StrifePlugin.getInstance(), 0L, 4L);
  }

  public void flagPvp() {
    if (livingEntity.get() instanceof Player && isInCombat()) {
      combatCountdownTask.setPvp();
    }
  }

  public void endCombat() {
    if (combatCountdownTask == null) {
      return;
    }
    if (!combatCountdownTask.isCancelled()) {
      combatCountdownTask.clearBars();
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
