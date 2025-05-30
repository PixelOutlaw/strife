package land.face.strife.managers;

import com.tealcube.minecraft.bukkit.facecore.utilities.MoveUtil;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.UnicodeUtil;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.loot.utils.ItemRenderer;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.TargetResponse;
import land.face.strife.data.ability.Ability;
import land.face.strife.data.ability.Ability.TargetType;
import land.face.strife.data.ability.AbilityIconData;
import land.face.strife.data.ability.CooldownTracker;
import land.face.strife.data.ability.EntityAbilitySet;
import land.face.strife.data.ability.EntityAbilitySet.Phase;
import land.face.strife.data.ability.EntityAbilitySet.TriggerAbilityType;
import land.face.strife.data.champion.ChampionSaveData;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.champion.StrifeAttribute;
import land.face.strife.data.conditions.Condition;
import land.face.strife.data.effects.AreaEffect.TargetingPriority;
import land.face.strife.data.effects.Effect;
import land.face.strife.data.effects.LookAt;
import land.face.strife.events.AbilityCastEvent;
import land.face.strife.stats.AbilitySlot;
import land.face.strife.stats.StrifeStat;
import land.face.strife.timers.EntityAbilityTimer;
import land.face.strife.timers.SoulTimer;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.HitDetectionUtil;
import land.face.strife.util.LogUtil;
import land.face.strife.util.PlayerDataUtil;
import land.face.strife.util.StatUtil;
import land.face.strife.util.TargetingUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.NotNull;

public class AbilityManager {

  private final StrifePlugin plugin;

  @Getter
  private final Map<String, Ability> loadedAbilities = new HashMap<>();
  private final Map<LivingEntity, Set<CooldownTracker>> cdMap = new WeakHashMap<>();
  private final Map<UUID, Set<CooldownTracker>> savedCooldowns = new ConcurrentHashMap<>();

  @Getter
  private Map<String, String> tooltips = new HashMap<>();

  public AbilityManager(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  public Ability getAbility(String name) {
    if (StringUtils.isBlank(name)) {
      return null;
    }
    if (loadedAbilities.containsKey(name)) {
      return loadedAbilities.get(name);
    }
    LogUtil.printWarning("Attempted to get unknown ability '" + name + "'.");
    return null;
  }

  public boolean execute(final Ability ability, final StrifeMob caster, StrifeMob target, AbilitySlot slot) {
    return execute(ability, caster, target, slot, false);
  }

  public boolean execute(Ability ability, StrifeMob caster, StrifeMob target, AbilitySlot slot, boolean ignoreReqs) {
    if (!ignoreReqs) {
      float cooldown = caster.getEntity().isSneaking() ? ability.getSneakCooldown() : ability.getCooldown();
      if (cooldown != 0 && !canBeCast(caster, ability)) {
        doOnCooldownPrompt(caster, ability);
        return false;
      }
      if (!hasEnoughResource(caster, target, ability)) {
        doNoEnergyPrompt(caster, ability);
        return false;
      }
      if (!PlayerDataUtil.areConditionsMet(caster, target, ability.getConditions())) {
        doRequirementNotMetPrompt(caster, ability);
        return false;
      }
    }
    TargetResponse response = getTargets(caster, target, ability);
    if (response.getLocation() == null && (response.getEntities() == null || response.getEntities().isEmpty())) {
      doTargetNotFoundPrompt(caster, ability);
      return false;
    }
    var cooldownTracker = getCooldownTracker(caster.getEntity(), ability.getId());
    int chargesRemaining = cooldownTracker == null ? ability.getMaxCharges() : cooldownTracker.getChargesLeft();
    if (ability.getTargetType() == TargetType.TOGGLE) {
      boolean toggledOn = toggleAbility(caster, ability, slot);
      if (!toggledOn) {
        return true;
      }
    } else {
      coolDownAbility(caster, target, ability, slot);
    }
    if (caster.getChampion() != null && ability.getAbilityIconData() != null) {
      if (MoveUtil.hasMoved((Player) caster.getEntity(), 30000)) {
        float violationLevel = plugin.getViolationManager().fetchViolationMult((Player) caster.getEntity());
        caster.getChampion().getDetailsContainer().registerAbilityUse(ability, violationLevel);
      }
    }
    if (caster.getEntity() instanceof Player) {
      if (((Player) caster.getEntity()).getGameMode() != GameMode.CREATIVE) {
        if (ability.isHealthCost()) {
          DamageUtil.dealRawDamage(caster, ability.calcRealCost(caster, target), true);
        } else {
          caster.setEnergy(caster.getEnergy() - ability.calcRealCost(caster, target));
        }
      }
    }

    if (ability.getAbilityIconData() != null) {
      AbilityCastEvent abilityCastEvent = new AbilityCastEvent(caster, ability);
      Bukkit.getPluginManager().callEvent(abilityCastEvent);
    }

    if (!ability.getSneakEffects().isEmpty() && caster.getEntity().isSneaking()) {
      plugin.getEffectManager().processEffectList(caster, response, ability.getSneakEffects());
    } else {
      plugin.getEffectManager().processEffectList(caster, response, ability.getEffects());
      if (chargesRemaining > 1) {
        plugin.getEffectManager().processEffectList(caster, response, ability.getTwoChargeEffects());
      }
      if (chargesRemaining > 2) {
        plugin.getEffectManager().processEffectList(caster, response, ability.getThreeChargeEffects());
      }
      if (chargesRemaining > 3) {
        plugin.getEffectManager().processEffectList(caster, response, ability.getFourChargeEffects());
      }
    }

    if (caster.getEntity() instanceof Player) {
      if (ability.isCancelStealth()) {
        plugin.getStealthManager().unstealthPlayer((Player) caster.getEntity(), false);
      }
    }
    playChatMessages(caster, ability);
    return true;
  }

  public void reduceCooldownMillis(LivingEntity livingEntity, String abilityId, long msReduction) {
    if (!cdMap.containsKey(livingEntity)) {
      return;
    }
    CooldownTracker tracker = getCooldownTracker(livingEntity, abilityId);
    if (tracker == null || tracker.isToggleState()) {
      return;
    }
    tracker.reduce(msReduction);
  }

  public void reduceCooldownPercent(LivingEntity livingEntity, String abilityId, float percent) {
    if (!cdMap.containsKey(livingEntity)) {
      return;
    }
    CooldownTracker tracker = getCooldownTracker(livingEntity, abilityId);
    if (tracker == null || tracker.isToggleState()) {
      return;
    }
    tracker.reduce(percent);
  }

  public void unToggleAll(LivingEntity livingEntity) {
    if (!cdMap.containsKey(livingEntity)) {
      return;
    }
    for (CooldownTracker cooldownContainer : cdMap.get(livingEntity)) {
      doToggleOff(cooldownContainer, plugin.getStrifeMobManager().getStatMob(livingEntity),
          cooldownContainer.getSlot());
    }
  }

  public void unToggleAbility(StrifeMob mob, String abilityId) {
    CooldownTracker container = getCooldownTracker(mob.getEntity(), abilityId);
    if (container == null) {
      return;
    }
    doToggleOff(container, mob, container.getSlot());
  }

  private void doToggleOff(CooldownTracker tracker, StrifeMob mob, AbilitySlot slot) {
    if (!tracker.isToggleState()) {
      return;
    }
    tracker.setToggleState(false);
    Ability ability = tracker.getAbility();
    coolDownAbility(mob, mob, ability, slot);

    Set<LivingEntity> targets = new HashSet<>();
    targets.add(mob.getEntity());
    TargetResponse response = new TargetResponse(targets);

    plugin.getEffectManager().processEffectList(mob, response, ability.getToggleOffEffects());
  }

  public CooldownTracker getCooldownTracker(LivingEntity le, String abilityId) {
    if (!cdMap.containsKey(le)) {
      cdMap.put(le, new HashSet<>());
      return null;
    }
    Iterator<CooldownTracker> trackerIterator = cdMap.get(le).iterator();
    while (trackerIterator.hasNext()) {
      CooldownTracker tracker = trackerIterator.next();
      if (tracker.isCancelled()) {
        trackerIterator.remove();
        continue;
      }
      if (tracker.getAbility().getId().equals(abilityId)) {
        return tracker;
      }
    }
    return null;
  }

  public void savePlayerCooldowns(Player player) {
    if (cdMap.containsKey(player)) {
      savedCooldowns.put(player.getUniqueId(), new HashSet<>());
      for (CooldownTracker container : cdMap.get(player)) {
        if (container.isCancelled()) {
          continue;
        }
        container.setLogoutTime(System.currentTimeMillis());
        savedCooldowns.get(player.getUniqueId()).add(container);
      }
      cdMap.remove(player);
    }
  }

  public void loadPlayerCooldowns(Player player) {
    if (!savedCooldowns.containsKey(player.getUniqueId())) {
      return;
    }
    cdMap.putIfAbsent(player, new HashSet<>());
    for (CooldownTracker container : savedCooldowns.get(player.getUniqueId())) {
      container.setStartTime(System.currentTimeMillis());
      cdMap.get(player).add(container);
      container.updateIcon();
    }
    savedCooldowns.remove(player.getUniqueId());
  }

  private boolean hasEnoughResource(StrifeMob caster, StrifeMob target, Ability ability) {
    if (caster.getEntity() instanceof Player) {
      if (((Player) caster.getEntity()).getGameMode() == GameMode.CREATIVE) {
        return true;
      }
      float resource = ability.isHealthCost() ? (float) caster.getEntity().getHealth() : caster.getEnergy();
      return resource >= ability.calcRealCost(caster, target);
    }
    return true;
  }

  public Map<StrifeStat, Float> getApplicableAbilityPassiveStats(Player player, @NotNull Ability ability) {
    if (ability.getTargetType() == TargetType.TOGGLE) {
      CooldownTracker tracker = getCooldownTracker(player, ability.getId());
      if (tracker != null && tracker.isToggleState()) {
        return ability.getTogglePassiveStats();
      }
      if (ability.isPassiveStatsOnCooldown() || tracker == null || tracker.getChargesLeft() > 0) {
        return ability.getPassiveStats();
      }
    }
    CooldownTracker tracker = getCooldownTracker(player, ability.getId());
    if (tracker == null || tracker.getChargesLeft() > 0) {
      return ability.getPassiveStats();
    }
    return new HashMap<>();
  }

  public boolean canBeCast(StrifeMob caster, @NotNull Ability ability) {
    if (caster.getEntity() == null || !caster.getEntity().isValid()) {
      return false;
    }
    CooldownTracker tracker = getCooldownTracker(caster.getEntity(), ability.getId());
    return tracker == null || tracker.isToggleState()
        || (ability.getMaxCharges() > 1 && tracker.getChargesLeft() > 0);
  }

  private void playChatMessages(StrifeMob caster, Ability ability) {
    if (caster.getChampion() == null || ability.getAbilityIconData() == null) {
      return;
    }
    ChampionSaveData data = caster.getChampion().getSaveData();
    if (data.getCastMessages() == null || data.getCastMessages().isEmpty()) {
      return;
    }
    AbilitySlot slot = ability.getAbilityIconData().getAbilitySlot();
    List<String> messages = data.getCastMessages().get(slot);
    if (messages.isEmpty()) {
      return;
    }
    ((Player) caster.getEntity())
        .chat("==ability==" + messages.get(StrifePlugin.RNG.nextInt(messages.size())));
  }

  public void startAbilityTimerTask(StrifeMob mob) {
    EntityAbilitySet abilitySet = mob.getAbilitySet();
    if (abilitySet == null) {
      return;
    }
    if (abilitySet.getAbilities(TriggerAbilityType.TIMER) == null) {
      return;
    }
    for (Phase phase : abilitySet.getAbilities(TriggerAbilityType.TIMER).keySet()) {
      if (!abilitySet.getAbilities(TriggerAbilityType.TIMER).get(phase).isEmpty()) {
        new EntityAbilityTimer(mob);
        return;
      }
    }
  }

  public boolean abilityCast(StrifeMob caster, TriggerAbilityType type) {
    return abilityCast(caster, null, type);
  }

  public boolean abilityCast(StrifeMob caster, StrifeMob target, TriggerAbilityType type) {
    EntityAbilitySet abilitySet = caster.getAbilitySet();
    if (abilitySet == null) {
      return false;
    }
    determinePhase(caster);
    List<Ability> abilities = EntityAbilitySet.getAbilities(abilitySet, type);
    if (abilities == null || abilities.isEmpty()) {
      return false;
    }

    if (type == TriggerAbilityType.PHASE_SHIFT || type == TriggerAbilityType.DEATH) {
      for (Ability a : abilities) {
        execute(a, caster, caster, null);
      }
      return true;
    }

    if (!caster.isGlobalCooldownReady()) {
      return false;
    }

    if (target == null) {
      LivingEntity targetEntity = TargetingUtil.getMobTarget(caster);
      target = targetEntity == null ? null : plugin.getStrifeMobManager().getStatMob(targetEntity);
    }

    StrifeMob finalTarget = target;
    List<Ability> selectorList = abilities.stream()
        .filter(ability -> isAbilityCastReady(caster, finalTarget, ability)).toList();

    if (selectorList.isEmpty()) {
      return false;
    }
    Ability ability = selectorList.get(StrifePlugin.RNG.nextInt(selectorList.size()));
    if (caster.getEntity() instanceof Mob) {
      LivingEntity le = ((Mob) caster.getEntity()).getTarget();
      if (le != null && le.isValid()) {
        LookAt.forceLook(caster.getEntity(), le);
      }
    }
    return execute(ability, caster, target, null, true);
  }

  public boolean checkClickCast(StrifeMob mob) {
    if (plugin.getAttackSpeedManager().getRawMultiplier(mob.getEntity().getUniqueId(), false) < 0.05) {
      var ability = mob.getChampion().getAbility(AbilitySlot.SLOT_D);
      if (ability != null) {
        var tracker = plugin.getAbilityManager().getCooldownTracker(mob.getEntity(), ability.getId());
        if (tracker == null) {
          return plugin.getAbilityIconManager().triggerAbility((Player) mob.getEntity(), 3);
        }
      }
    }
    return false;
  }

  public void setGlobalCooldown(Player player, int ticks) {
    player.setCooldown(Material.GOLDEN_CHESTPLATE, Math.max(player.getCooldown(Material.GOLDEN_CHESTPLATE), ticks));
    player.setCooldown(Material.DIAMOND_CHESTPLATE, Math.max(player.getCooldown(Material.DIAMOND_CHESTPLATE), ticks));
  }

  private void coolDownAbility(StrifeMob caster, StrifeMob target, Ability ability, AbilitySlot slot) {
    if (ability.getGlobalCooldownTicks() > 0) {
      caster.bumpGlobalCooldown(ability.getGlobalCooldownTicks() * 50);
    }
    float cooldown = caster.getEntity().isSneaking() ? ability.getSneakCooldown() : ability.getCooldown();
    if (cooldown < 0.5) {
      return;
    }
    cdMap.putIfAbsent(caster.getEntity(), new HashSet<>());
    CooldownTracker tracker = getCooldownTracker(caster.getEntity(), ability.getId());
    float cooldownMult = 1f;
    for (var entry : ability.getCooldownModifiers().entrySet()) {
      if (entry.getKey().isMet(caster, target)) {
        cooldownMult *= entry.getValue();
      }
    }
    if (tracker == null) {
      tracker = new CooldownTracker(caster, ability, slot, cooldownMult);
      cdMap.get(caster.getEntity()).add(tracker);
      if (ability.getTargetType() == TargetType.TOGGLE) {
        tracker.setToggleState(true);
      }
    }
    if (ability.isConsumeAllCharges()) {
      tracker.setChargesLeft(0);
    } else {
      tracker.setChargesLeft(tracker.getChargesLeft() - 1);
    }
    if (ability.isFullRecharge()) {
      tracker.setStart(cooldownMult);
    }
  }

  /*
    Returns true with the toggle state of the ability afterward.
    Illegal state if it isn't a toggle ability at all...
   */
  private boolean toggleAbility(StrifeMob caster, Ability ability, AbilitySlot slot) {
    if (ability.getTargetType() != TargetType.TOGGLE) {
      throw new IllegalStateException("Attempted to toggle a non toggle ability!");
    }
    cdMap.putIfAbsent(caster.getEntity(), new HashSet<>());
    CooldownTracker tracker = getCooldownTracker(caster.getEntity(), ability.getId());
    float cooldownMult = 1f;
    for (var entry : ability.getCooldownModifiers().entrySet()) {
      if (entry.getKey().isMet(caster, caster)) {
        cooldownMult *= entry.getValue();
      }
    }
    if (tracker == null) {
      tracker = new CooldownTracker(caster, ability, slot, cooldownMult);
      tracker.setToggleState(true);
      cdMap.get(caster.getEntity()).add(tracker);
      return true;
    }
    if (tracker.isToggleState()) {
      tracker.setToggleState(false);
      coolDownAbility(caster, caster, ability, slot);

      Set<LivingEntity> entities = new HashSet<>();
      entities.add(caster.getEntity());
      TargetResponse response = new TargetResponse(entities);

      plugin.getEffectManager().processEffectList(caster, response, ability.getToggleOffEffects());
      return false;
    }
    tracker.setToggleState(true);
    return true;
  }

  private void determinePhase(StrifeMob strifeMob) {
    if (strifeMob.getAbilitySet() == null) {
      return;
    }
    LogUtil.printDebug(" - Checking phase switch");
    Phase currentPhase = strifeMob.getAbilitySet().getPhase();
    LogUtil.printDebug(" - Current Phase: " + currentPhase);
    Phase newPhase = EntityAbilitySet.phaseFromEntityHealth(strifeMob.getEntity());
    if (newPhase.ordinal() > currentPhase.ordinal()) {
      strifeMob.getAbilitySet().setPhase(newPhase);
      LogUtil.printDebug(" - New Phase: " + newPhase);
      abilityCast(strifeMob, TriggerAbilityType.PHASE_SHIFT);
    }
  }

  private TargetResponse getTargets(StrifeMob caster, StrifeMob target, Ability ability) {
    Set<LivingEntity> targets = new HashSet<>();
    switch (ability.getTargetType()) {
      case SELF, TOGGLE -> {
        targets.add(caster.getEntity());
        return new TargetResponse(targets, ability.isCancelOnDeath());
      }
      case PARTY -> {
        if (caster.getEntity() instanceof Player) {
          targets.addAll(plugin.getSnazzyPartiesHook().getNearbyPartyMembers((Player) caster.getEntity(),
              caster.getEntity().getLocation(), ability.getRange()));
        } else {
          targets.add(caster.getEntity());
        }
        return new TargetResponse(targets, ability.isCancelOnDeath());
      }
      case NEARBY_ALLIES -> {
        targets = HitDetectionUtil.getEntitiesInArea(caster.getEntity().getLocation(), ability.getRange());
        TargetingUtil.filterFriendlyEntities(targets, caster, true);
        TargetingUtil.filterByTargetPriority(targets, ability.getTargetingPriority(), caster, ability.getMaxTargets());
        return new TargetResponse(targets, ability.isCancelOnDeath());
      }
      case NEARBY_ENEMIES -> {
        float range = ability.getRange() * (1 + caster.getStat(StrifeStat.CAST_RANGE) / 120f);
        targets = HitDetectionUtil.getEntitiesInArea(caster.getEntity().getLocation(), range);
        TargetingUtil.filterFriendlyEntities(targets, caster, false);
        TargetingUtil.filterByTargetPriority(targets, ability.getTargetingPriority(), caster, ability.getMaxTargets());
        return new TargetResponse(targets, ability.isCancelOnDeath());
      }
      case MASTER -> {
        if (caster.getMaster() != null) {
          targets.add(caster.getMaster().getEntity());
        }
        return new TargetResponse(targets, ability.isCancelOnDeath());
      }
      case MINIONS -> {
        for (StrifeMob mob : caster.getMinions()) {
          targets.add(mob.getEntity());
        }
        return new TargetResponse(targets, ability.isCancelOnDeath());
      }
      case SINGLE_OTHER -> {
        if (target != null && target.getEntity() != null) {
          targets.add(target.getEntity());
          return new TargetResponse(targets);
        }
        var t = getSingleTarget(ability, caster);
        if (t != null) {
          targets.add(t.getEntity());
        }
        return new TargetResponse(targets, ability.isCancelOnDeath());
      }
      case TARGET_AREA -> {
        LivingEntity targetEntity = (target == null || target.getEntity() == null)
            ? null : target.getEntity();
        float range = ability.getRange() * (1 + caster.getStat(StrifeStat.CAST_RANGE) / 120f);
        Location loc = TargetingUtil.getTargetLocation(caster.getEntity(), targetEntity, range,
            ability.isRaycastsTargetEntities(), ability.isFriendly());
        return new TargetResponse(loc, ability.isCancelOnDeath());
      }
      case TARGET_GROUND -> {
        float range = ability.getRange() * (1 + caster.getStat(StrifeStat.CAST_RANGE) / 120f);
        LivingEntity targetEntity = (target == null || target.getEntity() == null) ? null : target.getEntity();
        Location loc2 = TargetingUtil.getTargetLocation(caster.getEntity(),
            targetEntity, range, ability.isRaycastsTargetEntities(), ability.isFriendly());
        loc2 = TargetingUtil.modifyLocation(loc2, ability.getRange() + 2);
        return new TargetResponse(loc2, ability.isCancelOnDeath());
      }
      case NEAREST_SOUL -> {
        float range = ability.getRange() * (1 + caster.getStat(StrifeStat.CAST_RANGE) / 120f);
        SoulTimer soul = plugin.getSoulManager().getNearestSoul(caster.getEntity(), range);
        if (soul != null) {
          targets.add(soul.getOwner());
        }
        TargetResponse response = new TargetResponse(targets, ability.isCancelOnDeath());
        response.setForce(true);
        return response;
      }
    }
    return new TargetResponse(new HashSet<>(), ability.isCancelOnDeath());
  }

  public StrifeMob getSingleTarget(Ability ability, StrifeMob caster) {
    if (ability.isSneakSelfTarget() && caster.getEntity().getType() == EntityType.PLAYER &&
        caster.getEntity().isSneaking()) {
      return caster;
    }
    float range = ability.getRange() * (1 + caster.getStat(StrifeStat.CAST_RANGE) / 120f);
    LivingEntity t = TargetingUtil.selectFirstEntityInSight(caster.getEntity(), range, ability.isFriendly());
    if (t != null) {
      return plugin.getStrifeMobManager().getStatMob(t);
    }
    return null;
  }

  private boolean isAbilityCastReady(StrifeMob caster, StrifeMob target, Ability ability) {
    return canBeCast(caster, ability) && PlayerDataUtil
        .areConditionsMet(caster, target, ability.getConditions());
  }

  private void doTargetNotFoundPrompt(StrifeMob caster, Ability ability) {
    LogUtil.printDebug("Failed. No target found for ability " + ability.getId());
    if (!(ability.isShowMessages() && caster.getEntity() instanceof Player)) {
      return;
    }
    plugin.getGuiManager().postNotice((Player) caster.getEntity(), GuiManager.NOTICE_INVALID_TARGET, 10);
    ((Player) caster.getEntity()).playSound(caster.getEntity().getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.6f);
  }

  private void doRequirementNotMetPrompt(StrifeMob caster, Ability ability) {
    LogUtil.printDebug("Failed. Requirement not met for ability " + ability.getId());
    if (!(ability.isShowMessages() && caster.getEntity() instanceof Player)) {
      return;
    }
    plugin.getGuiManager().postNotice((Player) caster.getEntity(), GuiManager.NOTICE_REQUIREMENT, 10);
    ((Player) caster.getEntity())
        .playSound(caster.getEntity().getLocation(), Sound.UI_BUTTON_CLICK, 1f, 0.6f);
  }

  private void doOnCooldownPrompt(StrifeMob caster, Ability ability) {
    LogUtil.printDebug("Failed. Ability " + ability.getId() + " is on cooldown");
    if (!(ability.isShowMessages() && caster.getEntity() instanceof Player)) {
      return;
    }
    plugin.getGuiManager().postNotice((Player) caster.getEntity(), GuiManager.NOTICE_COOLDOWN, 10);
    ((Player) caster.getEntity()).playSound(caster.getEntity().getLocation(),
        Sound.UI_BUTTON_CLICK, 1f, 1.6f);
    Bukkit.getScheduler().runTaskLater(plugin, () -> ((Player) caster.getEntity())
        .playSound(caster.getEntity().getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.6f), 2L);
  }

  private void doNoEnergyPrompt(StrifeMob caster, Ability ability) {
    LogUtil.printDebug("Failed. Not enough energy to cast Ability " + ability.getId());
    if (!(ability.isShowMessages() && caster.getEntity() instanceof Player)) {
      return;
    }
    plugin.getGuiManager().postNotice((Player) caster.getEntity(), GuiManager.NOTICE_ENERGY, 10);
    ((Player) caster.getEntity())
        .playSound(caster.getEntity().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 1.5f);
  }

  public void loadTooltips(ConfigurationSection cs) {
    for (String s : cs.getKeys(false)) {
      //iconFormats.put(s, PaletteUtil.color(cs.getString(s + ".format", "|white||i|")));
    }
  }

  public void loadAbility(String fileName, String key, ConfigurationSection cs) {
    if (cs == null) {
      return;
    }
    String name = PaletteUtil.loadString(cs, "name", key);
    TargetType targetType;
    try {
      targetType = TargetType.valueOf(cs.getString("target-type"));
    } catch (Exception e) {
      LogUtil.printWarning("Skipping load of ability " + key + " - Invalid target type.");
      return;
    }

    AbilityType abilityType;
    try {
      abilityType = AbilityType.valueOf(cs.getString("ability-type", "SPELL"));
    } catch (Exception e) {
      LogUtil.printWarning("Skipping load of ability " + key + " - Invalid ability type.");
      return;
    }

    boolean raycastsHitEntities = cs
        .getBoolean("raycasts-hit-entities", targetType == TargetType.TARGET_GROUND);

    List<String> effectStrings = cs.getStringList("effects");
    if (effectStrings.isEmpty()) {
      LogUtil.printWarning("Skipping ability " + key + " - No effects.");
      return;
    }
    List<String> effectStrings2 = cs.getStringList("effects-two-charges");
    List<String> effectStrings3 = cs.getStringList("effects-three-charges");
    List<String> effectStrings4 = cs.getStringList("effects-four-charges");
    List<String> killEffectStrings = cs.getStringList("passive-kill-effects");
    List<Effect> effects = plugin.getEffectManager().getEffects(effectStrings);
    List<Effect> effects2 = plugin.getEffectManager().getEffects(effectStrings2);
    List<Effect> effects3 = plugin.getEffectManager().getEffects(effectStrings3);
    List<Effect> effects4 = plugin.getEffectManager().getEffects(effectStrings4);
    List<Effect> killEffects = plugin.getEffectManager().getEffects(killEffectStrings);

    List<String> sneakEffectStrings = cs.getStringList("sneak-effects");
    List<Effect> sneakEffects = plugin.getEffectManager().getEffects(sneakEffectStrings);

    List<String> toggleStrings = cs.getStringList("toggle-off-effects");
    if (targetType == TargetType.TOGGLE && toggleStrings.isEmpty()) {
      LogUtil.printError("Skipping. Toggle abilities must have toggle-off-effects! Ability:" + key);
      return;
    }
    List<Effect> toggleOffEffects = plugin.getEffectManager().getEffects(toggleStrings);

    float cooldown = (float) cs.getDouble("cooldown", 0);
    float sneakCooldown = (float) cs.getDouble("sneak-cooldown", cooldown);
    float minCooldown = (float) cs.getDouble("min-cooldown", 0);
    int maxCharges = cs.getInt("max-charges", 1);
    int globalCooldownTicks = cs.getInt("global-cooldown-ticks", 5);
    int sneakGlobalCooldownTicks = cs.getInt("sneak-global-cooldown-ticks", globalCooldownTicks);
    float range = (float) cs.getDouble("range", 0);
    float cost = (float) cs.getDouble("cost", 0);
    float sneakCost = (float) cs.getDouble("sneak-cost", cost);
    float weightScaling = (float) cs.getDouble("weight-scaling", 0);
    boolean showMessages = cs.getBoolean("show-messages", false);
    boolean requireTarget = cs.getBoolean("require-target", false);
    List<String> conditionStrings = cs.getStringList("conditions");
    Set<Condition> conditions = new HashSet<>();
    for (String s : conditionStrings) {
      Condition condition = plugin.getEffectManager().getConditions().get(s);
      if (condition == null) {
        LogUtil.printWarning(" Invalid condition '" + s + "' for ability '" + key + "'. Skipping.");
        continue;
      }
      conditions.add(plugin.getEffectManager().getConditions().get(s));
    }
    List<String> sneakConditionStrings = cs.getStringList("conditions");
    Set<Condition> sneakConditions = new HashSet<>();
    for (String s : sneakConditionStrings) {
      Condition condition = plugin.getEffectManager().getConditions().get(s);
      if (condition == null) {
        LogUtil.printWarning(" Invalid condition '" + s + "' for ability '" + key + "'. Skipping.");
        continue;
      }
      sneakConditions.add(plugin.getEffectManager().getConditions().get(s));
    }
    AbilityIconData abilityIconData = buildIconData(fileName, key, abilityType, cs.getConfigurationSection("icon"), maxCharges);

    Ability ability = Ability.builder()
        .id(key)
        .name(name)
        .hidden(cs.getBoolean("hidden", true))
        .friendly(cs.getBoolean("friendly", false))
        .cooldown(cooldown)
        .sneakCooldown(sneakCooldown)
        .maxCharges(maxCharges)
        .globalCooldownTicks(globalCooldownTicks)
        .effects(effects)
        .twoChargeEffects(effects2)
        .threeChargeEffects(effects3)
        .fourChargeEffects(effects4)
        .onKillEffects(killEffects)
        .toggleOffEffects(toggleOffEffects)
        .tags(new HashSet<>(cs.getStringList("tags")))
        .targetType(targetType)
        .castType(abilityType)
        .sneakEffects(sneakEffects)
        .requireTarget(requireTarget)
        .sneakSelfTarget(cs.getBoolean("sneak-self-target", false))
        .raycastsTargetEntities(raycastsHitEntities)
        .range(range)
        .cost(cost)
        .sneakCost(sneakCost)
        .showMessages(showMessages)
        .conditions(conditions)
        .sneakConditions(sneakConditions)
        .passiveStatsOnCooldown(cs.getBoolean("passive-stats-on-cooldown", false))
        .abilityIconData(abilityIconData)
        .cancelStealth(cs.getBoolean("cancel-stealth", true))
        .cancelOnDeath(cs.getBoolean("cancel-on-death", true))
        .deathUntoggle(cs.getBoolean("untoggle-on-death", false))
        .minCooldown(minCooldown)
        .weightScaling(weightScaling)
        .maxTargets(cs.getInt("max-targets", 8))
        .healthCost(cs.getBoolean("health-cost", false))
        .fullRecharge(cs.getBoolean("full-recharge", false))
        .consumeAllCharges(cs.getBoolean("consume-all-charges", false))
        .build();

    ability.getPassiveStats().putAll(StatUtil.getStatMapFromSection(
        cs.getConfigurationSection("passive-stats")));
    ability.getTogglePassiveStats().putAll(StatUtil.getStatMapFromSection(
        cs.getConfigurationSection("passive-toggle-stats")));
    ability.setTargetingPriority(TargetingPriority.valueOf(
        cs.getString("target-priority", "RANDOM")));

    var section1 = cs.getStringList("cooldown-modifiers");
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> {
      for (String s : section1) {
        var strs = s.split("\\|");
        ability.getCooldownModifiers().put(
            plugin.getEffectManager().getConditions().get(strs[0]), Float.valueOf(strs[1]));
      }
    }, 5L);
    var section2 = cs.getStringList("cost-modifiers");
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> {
      for (String s : section2) {
        var strs = s.split("\\|");
        ability.getCostModifiers().put(
            plugin.getEffectManager().getConditions().get(strs[0]), Float.valueOf(strs[1]));
      }
    }, 5L);

    List<String> filterConditions = cs.getStringList("filter-conditions");
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> {
      for (String s : filterConditions) {
        ability.getFilterConditions().add(plugin.getEffectManager().getConditions().get(s));
      }
    }, 5L);

    loadedAbilities.put(key, ability);
    LogUtil.printDebug("Loaded ability " + key + " successfully.");
  }

  private AbilityIconData buildIconData(String fileName, String key, AbilityType castType, ConfigurationSection config, int charges) {
    if (config == null) {
      return null;
    }
    fileName = fileName.replace(".yml", "");
    LogUtil.printDebug("Ability " + key + " has icon!");
    String format = PaletteUtil.loadString(config, "format", "|white||b|");

    Material material = castType.getMaterial();
    var strs = PaletteUtil.loadStrings(config, "lore");
    var summary = UnicodeUtil.unicodePlacehold(PaletteUtil.loadStrings(config, "summary"));
    List<String> lore = new ArrayList<>();
    for (var s : strs) {
      if ("{summary}".equals(s)) {
        lore.addAll(summary);
      } else {
        lore.add(UnicodeUtil.unicodePlacehold(s));
      }
    }

    ItemStack icon = new ItemStack(material);

    // todo: tooltips here

    ItemStackExtensionsKt.setDisplayName(icon, format + AbilityIconManager.ABILITY_PREFIX + key);
    ItemStackExtensionsKt.setUnbreakable(icon, true);
    int modelData = config.getInt("custom-model-data", 7999);
    ItemRenderer.applyCommonFlags(icon);
    icon.editMeta(m -> {
      ((Damageable) m).setMaxDamage(1000);
      m.setMaxStackSize(charges);
      m.setItemModel(new NamespacedKey("faceland", "ability_icons/" + modelData));
    });

    ItemStack simpleIcon = icon.clone();

    TextUtils.setLore(icon, lore, false);
    TextUtils.setLore(simpleIcon, summary, false);

    AbilityIconData data = new AbilityIconData(icon, simpleIcon);

    data.setAbilitySlot(AbilitySlot.valueOf(config.getString("trigger-slot")));
    data.setLevelRequirement(config.getInt("level-requirement", -1));
    data.setTotalSkillRequirement(config.getInt("total-skill-requirement", -1));

    Map<StrifeAttribute, Integer> attrReqs = new HashMap<>();
    ConfigurationSection attrSection = config.getConfigurationSection("attribute-requirements");
    if (attrSection != null) {
      for (String s : attrSection.getKeys(false)) {
        StrifeAttribute attr = plugin.getAttributeManager().getAttribute(s);
        int value = attrSection.getInt(s);
        attrReqs.put(attr, value);
      }
    }
    Map<LifeSkillType, Integer> skillReqs = new HashMap<>();
    ConfigurationSection skillSecion = config.getConfigurationSection("skill-requirements");
    if (skillSecion != null) {
      for (String s : skillSecion.getKeys(false)) {
        LifeSkillType skill = LifeSkillType.valueOf(s);
        int value = skillSecion.getInt(s);
        skillReqs.put(skill, value);
      }
    }
    Map<LifeSkillType, Float> expWeight = new HashMap<>();
    ConfigurationSection weightSection = config.getConfigurationSection("exp-weights");
    if (weightSection != null) {
      for (String s : weightSection.getKeys(false)) {
        LifeSkillType skill = LifeSkillType.valueOf(s);
        double value = weightSection.getDouble(s);
        expWeight.put(skill, (float) value);
      }
    }
    data.getAttributeRequirement().clear();
    data.getAttributeRequirement().putAll(attrReqs);
    data.getLifeSkillRequirements().clear();
    data.getLifeSkillRequirements().putAll(skillReqs);
    data.getExpWeights().clear();
    data.getExpWeights().putAll(expWeight);
    return data;
  }

  @Getter
  public enum AbilityType {

    ATTACK(Material.DIAMOND_CHESTPLATE),
    SPELL(Material.GOLDEN_CHESTPLATE);

    private final Material material;

    AbilityType(Material material) {
      this.material = material;
    }
  }
}
