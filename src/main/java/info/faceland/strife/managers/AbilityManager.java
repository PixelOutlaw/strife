package info.faceland.strife.managers;

import static info.faceland.strife.data.ability.Ability.TargetType.SINGLE_OTHER;
import static info.faceland.strife.data.ability.Ability.TargetType.TARGET_GROUND;
import static info.faceland.strife.data.ability.Ability.TargetType.TOGGLE;

import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.data.ability.Ability;
import info.faceland.strife.data.ability.Ability.TargetType;
import info.faceland.strife.data.ability.AbilityCooldownContainer;
import info.faceland.strife.data.ability.AbilityIconData;
import info.faceland.strife.data.ability.EntityAbilitySet;
import info.faceland.strife.data.ability.EntityAbilitySet.Phase;
import info.faceland.strife.data.ability.EntityAbilitySet.TriggerAbilityType;
import info.faceland.strife.data.champion.LifeSkillType;
import info.faceland.strife.data.champion.StrifeAttribute;
import info.faceland.strife.data.conditions.Condition;
import info.faceland.strife.data.effects.Effect;
import info.faceland.strife.data.effects.Wait;
import info.faceland.strife.stats.AbilitySlot;
import info.faceland.strife.timers.EntityAbilityTimer;
import info.faceland.strife.util.ItemUtil;
import info.faceland.strife.util.LogUtil;
import info.faceland.strife.util.PlayerDataUtil;
import info.faceland.strife.util.TargetingUtil;
import io.netty.util.internal.ConcurrentSet;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.apache.commons.lang.NullArgumentException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class AbilityManager {

  private final StrifePlugin plugin;
  private final Map<String, Ability> loadedAbilities = new HashMap<>();
  private final Map<LivingEntity, Set<AbilityCooldownContainer>> coolingDownAbilities = new ConcurrentHashMap<>();
  private final Map<UUID, Set<AbilityCooldownContainer>> savedPlayerCooldowns = new ConcurrentHashMap<>();
  private final Set<EntityAbilityTimer> abilityTimers = new HashSet<>();

  private final Random random = new Random();

  private static final String ON_COOLDOWN = TextUtils.color("&e&lAbility On Cooldown!");
  private static final String NO_TARGET = TextUtils.color("&e&lNo Ability Target Found!");
  private static final String NO_REQUIRE = TextUtils.color("&c&lAbility Requirements Not Met!");

  public AbilityManager(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  public Ability getAbility(String name) {
    if (loadedAbilities.containsKey(name)) {
      return loadedAbilities.get(name);
    }
    LogUtil.printWarning("Attempted to get unknown ability '" + name + "'.");
    return null;
  }

  public void cooldownReduce(LivingEntity livingEntity, String abilityId, int milliseconds) {
    if (!coolingDownAbilities.containsKey(livingEntity)) {
      return;
    }
    AbilityCooldownContainer container = getCooldownContainer(livingEntity, abilityId);
    if (container == null) {
      return;
    }
    container.setEndTime(container.getEndTime() - milliseconds);
    if (livingEntity instanceof Player) {
      plugin.getAbilityIconManager()
          .updateIconProgress((Player) livingEntity, getAbility(abilityId));
    }
  }

  public void createCooldownContainer(LivingEntity le) {
    coolingDownAbilities.put(le, new ConcurrentSet<>());
  }

  public AbilityCooldownContainer getCooldownContainer(LivingEntity le, String abilityId) {
    for (AbilityCooldownContainer cont : coolingDownAbilities.get(le)) {
      if (abilityId.equals(cont.getAbilityId())) {
        return cont;
      }
    }
    return null;
  }

  public double getCooldownPercent(AbilityCooldownContainer container) {
    if (container == null) {
      return 1.0D;
    }
    double progress = container.getEndTime() - System.currentTimeMillis();
    double maxTime = container.getEndTime() - container.getStartTime();
    return progress / maxTime;
  }

  public void tickAbilityCooldowns() {
    for (LivingEntity le : coolingDownAbilities.keySet()) {
      if (le == null || !le.isValid()) {
        coolingDownAbilities.remove(le);
        continue;
      }
      for (AbilityCooldownContainer container : coolingDownAbilities.get(le)) {
        Ability ability = getAbility(container.getAbilityId());
        if (container.isToggledOn()) {
          plugin.getAbilityIconManager().updateIconProgress((Player) le, ability);
          continue;
        }
        if (System.currentTimeMillis() >= container.getEndTime()) {
          if (container.getSpentCharges() <= 1) {
            coolingDownAbilities.get(le).remove(container);
            LogUtil.printDebug("Final cooldown for " + container.getAbilityId() + ", removing");
            if (le instanceof Player) {
              plugin.getAbilityIconManager().updateIconProgress((Player) le, ability);
            }
            continue;
          }
          container.setSpentCharges(container.getSpentCharges() - 1);
          container.setStartTime(System.currentTimeMillis());
          container.setEndTime(System.currentTimeMillis() + ability.getCooldown() * 1000);
          LogUtil.printDebug("Cooled one charge for " + container.getAbilityId());
          if (le instanceof Player) {
            plugin.getAbilityIconManager().updateIconProgress((Player) le, ability);
          }
        } else if (le instanceof Player && container.getSpentCharges() == ability.getMaxCharges()) {
          plugin.getAbilityIconManager().updateIconProgress((Player) le, ability);
        }
      }
    }
  }

  public void savePlayerCooldowns(Player player) {
    if (coolingDownAbilities.containsKey(player)) {
      savedPlayerCooldowns.put(player.getUniqueId(), new HashSet<>());
      for (AbilityCooldownContainer container : coolingDownAbilities.get(player)) {
        container.setLogoutTime(System.currentTimeMillis());
        savedPlayerCooldowns.get(player.getUniqueId()).add(container);
      }
      coolingDownAbilities.remove(player);
    }
  }

  public void loadPlayerCooldowns(Player player) {
    coolingDownAbilities.put(player, new ConcurrentSet<>());
    if (!savedPlayerCooldowns.containsKey(player.getUniqueId())) {
      return;
    }
    for (AbilityCooldownContainer container : savedPlayerCooldowns.get(player.getUniqueId())) {
      long timeDifference = System.currentTimeMillis() - container.getLogoutTime();
      container.setStartTime(container.getStartTime() + timeDifference);
      container.setEndTime(container.getEndTime() + timeDifference);
      container.setToggledOn(false);
      coolingDownAbilities.get(player).add(container);
      plugin.getAbilityIconManager()
          .updateIconProgress(player, getAbility(container.getAbilityId()));
    }
    savedPlayerCooldowns.remove(player.getUniqueId());
  }

  private boolean canBeCast(LivingEntity entity, Ability ability) {
    if (entity == null || !entity.isValid()) {
      return false;
    }
    AbilityCooldownContainer container = getCooldownContainer(entity, ability.getId());
    if (container == null || container.getSpentCharges() < ability.getMaxCharges()) {
      return true;
    }
    return System.currentTimeMillis() > container.getEndTime();
  }

  public boolean execute(final Ability ability, final StrifeMob caster, LivingEntity target) {
    return execute(ability, caster, target, false);
  }

  public boolean execute(final Ability ability, final StrifeMob caster, LivingEntity target,
      boolean ignoreReqs) {
    if (!ignoreReqs && ability.getCooldown() != 0 && !canBeCast(caster.getEntity(), ability)) {
      doOnCooldownPrompt(caster, ability);
      return false;
    }
    if (!ignoreReqs && !PlayerDataUtil.areConditionsMet(caster, caster, ability.getConditions())) {
      doRequirementNotMetPrompt(caster, ability);
      return false;
    }
    Set<LivingEntity> targets = getTargets(caster, target, ability);
    if (targets == null) {
      throw new NullArgumentException("Null target list on ability " + ability.getId());
    }
    if (ability.getTargetType() == TARGET_GROUND && targets.isEmpty()) {
      doTargetNotFoundPrompt(caster, ability);
      return false;
    }
    if (shouldSingleTargetFail(ability, caster, targets)) {
      return false;
    }
    if (ability.getTargetType() != TOGGLE) {
      coolDownAbility(caster.getEntity(), ability);
    } else {
      boolean isOnAfterToggle = toggleAbility(caster.getEntity(), ability);
      if (!isOnAfterToggle) {
        coolDownAbility(caster.getEntity(), ability);
        runEffects(caster, targets, ability.getToggleOffEffects(), 0);
        return true;
      }
    }
    if (caster.getChampion() != null && ability.getAbilityIconData() != null) {
      caster.getChampion().getDetailsContainer().addWeights(ability);
    }
    List<Effect> taskEffects = new ArrayList<>();
    int waitTicks = 0;
    for (Effect effect : ability.getEffects()) {
      if (effect instanceof Wait) {
        LogUtil.printDebug("Effects in this chunk: " + taskEffects.toString());
        runEffects(caster, targets, taskEffects, waitTicks);
        waitTicks += ((Wait) effect).getTickDelay();
        taskEffects = new ArrayList<>();
        continue;
      }
      taskEffects.add(effect);
      LogUtil.printDebug("Added effect " + effect.getId() + " to task list");
    }
    runEffects(caster, targets, taskEffects, waitTicks);
    return true;
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
        abilityTimers.add(new EntityAbilityTimer(mob));
        return;
      }
    }
  }

  public void cancelTimerTimers() {
    for (EntityAbilityTimer timer : abilityTimers) {
      timer.cancel();
    }
    abilityTimers.clear();
  }

  public boolean abilityCast(StrifeMob caster, TriggerAbilityType type) {
    EntityAbilitySet abilitySet = caster.getAbilitySet();
    if (abilitySet == null) {
      return false;
    }
    checkPhaseChange(caster);
    Phase phase = abilitySet.getPhase();
    Map<Phase, Set<Ability>> abilitySection = abilitySet.getAbilities(type);
    if (abilitySection == null) {
      return false;
    }
    Set<Ability> abilities = abilitySection.get(phase);
    if (abilities == null || abilities.isEmpty()) {
      return false;
    }
    if (type == TriggerAbilityType.PHASE_SHIFT) {
      for (Ability a : abilities) {
        execute(a, caster, null);
      }
      return true;
    }
    LivingEntity target = TargetingUtil.getMobTarget(caster);
    StrifeMob targetMob = target == null ? null : plugin.getStrifeMobManager().getStatMob(target);

    List<Ability> selectorList = abilities.stream()
        .filter(ability -> isAbilityCastReady(caster, targetMob, ability))
        .collect(Collectors.toList());

    if (selectorList.isEmpty()) {
      LogUtil.printDebug(PlayerDataUtil.getName(caster.getEntity()) + " failed to cast " +
          phase + " type " + type);
      return false;
    }
    Ability ability = selectorList.get(random.nextInt(selectorList.size()));
    return execute(ability, caster, target, true);
  }

  public void setGlobalCooldown(Player player, Ability ability) {
    player.setCooldown(Material.DIAMOND_CHESTPLATE, ability.getGlobalCooldownTicks());
  }

  public void setGlobalCooldown(Player player, int ticks) {
    player.setCooldown(Material.DIAMOND_CHESTPLATE, ticks);
  }

  private void coolDownAbility(LivingEntity livingEntity, Ability ability) {
    if (!coolingDownAbilities.containsKey(livingEntity)) {
      coolingDownAbilities.put(livingEntity, new ConcurrentSet<>());
    }
    AbilityCooldownContainer container = getCooldownContainer(livingEntity, ability.getId());
    if (container == null) {
      container = new AbilityCooldownContainer(ability.getId(),
          System.currentTimeMillis() + ability.getCooldown() * 1000);
      coolingDownAbilities.get(livingEntity).add(container);
    } else  {
      container.setStartTime(System.currentTimeMillis());
      container.setEndTime(System.currentTimeMillis() + ability.getCooldown() * 1000);
    }
    container.setSpentCharges(container.getSpentCharges() + 1);
    container.setToggledOn(false);
  }

  private boolean toggleAbility(LivingEntity livingEntity, Ability ability) {
    if (!coolingDownAbilities.containsKey(livingEntity)) {
      coolingDownAbilities.put(livingEntity, new ConcurrentSet<>());
    }
    AbilityCooldownContainer container = getCooldownContainer(livingEntity, ability.getId());
    if (container == null) {
      container = new AbilityCooldownContainer(ability.getId(), 0);
      container.setToggledOn(true);
      coolingDownAbilities.get(livingEntity).add(container);
    } else {
      container.setToggledOn(!container.isToggledOn());
    }
    return container.isToggledOn();
  }

  private boolean shouldSingleTargetFail(Ability ability, StrifeMob caster,
      Set<LivingEntity> targets) {
    if (ability.getTargetType() != SINGLE_OTHER) {
      return false;
    }
    TargetingUtil.filterFriendlyEntities(targets, caster, ability.isFriendly());
    if (targets.isEmpty()) {
      doTargetNotFoundPrompt(caster, ability);
      return true;
    }
    StrifeMob targetMob = plugin.getStrifeMobManager().getStatMob(targets.iterator().next());
    if (!PlayerDataUtil.areConditionsMet(caster, targetMob, ability.getConditions())) {
      doRequirementNotMetPrompt(caster, ability);
      return true;
    }
    return false;
  }

  private void checkPhaseChange(StrifeMob strifeMob) {
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

  private void runEffects(StrifeMob caster, Set<LivingEntity> targets, List<Effect> effectList,
      int delay) {
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> {
      LogUtil.printDebug("Effect task started - " + effectList.toString());
      if (!caster.getEntity().isValid()) {
        LogUtil.printDebug("- Task cancelled, caster is dead");
        return;
      }
      for (Effect effect : effectList) {
        LogUtil.printDebug("- Executing effect " + effect.getId());
        plugin.getEffectManager().execute(effect, caster, targets);
      }
      LogUtil.printDebug("- Completed effect task.");
    }, delay);
  }

  private Set<LivingEntity> getTargets(StrifeMob caster, LivingEntity target, Ability ability) {
    Set<LivingEntity> targets = new HashSet<>();
    switch (ability.getTargetType()) {
      case SELF:
      case TOGGLE:
      case PARTY:
        targets.add(caster.getEntity());
        return targets;
      case MASTER:
        if (caster.getMaster() != null) {
          targets.add(caster.getMaster());
        }
        return targets;
      case MINIONS:
        for (StrifeMob mob : caster.getMinions()) {
          targets.add(mob.getEntity());
        }
        return targets;
      case SINGLE_OTHER:
        if (target != null) {
          targets.add(target);
          return targets;
        }
        LivingEntity newTarget = TargetingUtil
            .selectFirstEntityInSight(caster.getEntity(), ability.getRange());
        if (newTarget != null) {
          targets.add(newTarget);
        }
        return targets;
      case TARGET_AREA:
        Location loc = TargetingUtil.getTargetLocation(
            caster.getEntity(), target, ability.getRange(), ability.isRaycastsTargetEntities());
        return TargetingUtil.getTempStandTargetList(loc, 0);
      case TARGET_GROUND:
        Location loc2 = TargetingUtil.getTargetLocation(
            caster.getEntity(), target, ability.getRange(), ability.isRaycastsTargetEntities());
        return TargetingUtil.getTempStandTargetList(loc2, ability.getRange() + 3);
    }
    return null;
  }

  private boolean isAbilityCastReady(StrifeMob caster, StrifeMob target, Ability ability) {
    return canBeCast(caster.getEntity(), ability) && PlayerDataUtil
        .areConditionsMet(caster, target, ability.getConditions());
  }

  private void doTargetNotFoundPrompt(StrifeMob caster, Ability ability) {
    LogUtil.printDebug("Failed. No target found for ability " + ability.getId());
    if (!(ability.isShowMessages() && caster.getEntity() instanceof Player)) {
      return;
    }
    MessageUtils.sendActionBar((Player) caster.getEntity(), NO_TARGET);
    ((Player) caster.getEntity()).playSound(
        caster.getEntity().getLocation(),
        Sound.ENTITY_GENERIC_EXTINGUISH_FIRE,
        1f,
        1f);
  }

  private void doRequirementNotMetPrompt(StrifeMob caster, Ability ability) {
    LogUtil.printDebug("Failed. Requirement not met for ability " + ability.getId());
    if (!(ability.isShowMessages() && caster.getEntity() instanceof Player)) {
      return;
    }
    MessageUtils.sendActionBar((Player) caster.getEntity(), NO_REQUIRE);
    ((Player) caster.getEntity()).playSound(
        caster.getEntity().getLocation(),
        Sound.BLOCK_LAVA_POP,
        1f,
        0.5f);
  }

  private void doOnCooldownPrompt(StrifeMob caster, Ability ability) {
    LogUtil.printDebug("Failed. Ability " + ability.getId() + " is on cooldown");
    if (!(ability.isShowMessages() && caster.getEntity() instanceof Player)) {
      return;
    }
    MessageUtils.sendActionBar((Player) caster.getEntity(), ON_COOLDOWN);
    ((Player) caster.getEntity()).playSound(
        caster.getEntity().getLocation(),
        Sound.ENTITY_GENERIC_EXTINGUISH_FIRE,
        1f,
        1.5f);
  }

  public void loadAbility(String key, ConfigurationSection cs) {
    String name = TextUtils.color(cs.getString("name", "ABILITY NOT NAMED"));
    TargetType targetType;
    try {
      targetType = TargetType.valueOf(cs.getString("target-type"));
    } catch (Exception e) {
      LogUtil.printWarning("Skipping load of ability " + key + " - Invalid target type.");
      return;
    }

    boolean raycastsHitEntities = cs
        .getBoolean("raycasts-hit-entities", targetType == TARGET_GROUND);

    List<String> effectStrings = cs.getStringList("effects");
    if (effectStrings.isEmpty()) {
      LogUtil.printWarning("Skipping ability " + key + " - No effects.");
      return;
    }
    List<Effect> effects = plugin.getEffectManager().getEffects(effectStrings);

    List<String> toggleStrings = cs.getStringList("toggle-off-effects");
    if (targetType == TOGGLE && toggleStrings.isEmpty()) {
      LogUtil.printError("Skipping. Toggle abilities must have toggle-off-effects! Ability:" + key);
      return;
    }
    List<Effect> toggleOffEffects = plugin.getEffectManager().getEffects(toggleStrings);

    int cooldown = cs.getInt("cooldown", 0);
    int maxCharges = cs.getInt("max-charges", 1);
    int globalCooldownTicks = cs.getInt("global-cooldown-ticks", 5);
    float range = (float) cs.getDouble("range", 0);
    boolean showMessages = cs.getBoolean("show-messages", false);
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
    AbilityIconData abilityIconData = buildIconData(key, cs.getConfigurationSection("icon"));
    boolean friendly = cs.getBoolean("friendly", false);
    loadedAbilities.put(key, new Ability(key, name, effects, toggleOffEffects, targetType, range,
        cooldown, maxCharges, globalCooldownTicks, showMessages, raycastsHitEntities, conditions,
        friendly, abilityIconData));
    LogUtil.printDebug("Loaded ability " + key + " successfully.");
  }

  private AbilityIconData buildIconData(String key, ConfigurationSection iconSection) {
    if (iconSection == null) {
      return null;
    }
    LogUtil.printDebug("Ability " + key + " has icon!");
    String format = TextUtils.color(iconSection.getString("format", "&f&l"));
    Material material = Material.valueOf(iconSection.getString("material"));
    List<String> lore = TextUtils.color(iconSection.getStringList("lore"));
    ItemStack icon = new ItemStack(material);
    ItemStackExtensionsKt.setDisplayName(icon, format + AbilityIconManager.ABILITY_PREFIX + key);
    ItemStackExtensionsKt.setLore(icon, lore);
    ItemStackExtensionsKt.addItemFlags(icon, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
    ItemUtil.removeAttributes(icon);

    AbilityIconData data = new AbilityIconData(icon);

    data.setAbilitySlot(AbilitySlot.valueOf(iconSection.getString("trigger-slot")));
    data.setLevelRequirement(iconSection.getInt("level-requirement", 0));
    data.setBonusLevelRequirement(iconSection.getInt("bonus-level-requirement", 0));

    Map<StrifeAttribute, Integer> attrReqs = new HashMap<>();
    ConfigurationSection attrSection = iconSection
        .getConfigurationSection("attribute-requirements");
    if (attrSection != null) {
      for (String s : attrSection.getKeys(false)) {
        StrifeAttribute attr = plugin.getAttributeManager().getAttribute(s);
        int value = attrSection.getInt(s);
        attrReqs.put(attr, value);
      }
    }
    Map<LifeSkillType, Integer> skillReqs = new HashMap<>();
    ConfigurationSection skillSecion = iconSection.getConfigurationSection("skill-requirements");
    if (skillSecion != null) {
      for (String s : skillSecion.getKeys(false)) {
        LifeSkillType skill = LifeSkillType.valueOf(s);
        int value = skillSecion.getInt(s);
        skillReqs.put(skill, value);
      }
    }
    Map<LifeSkillType, Float> expWeight = new HashMap<>();
    ConfigurationSection weightSection = iconSection.getConfigurationSection("exp-weights");
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
}
