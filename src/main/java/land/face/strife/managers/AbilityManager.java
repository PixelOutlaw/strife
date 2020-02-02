package land.face.strife.managers;

import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import io.netty.util.internal.ConcurrentSet;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.ability.Ability;
import land.face.strife.data.ability.Ability.TargetType;
import land.face.strife.data.ability.AbilityCooldownContainer;
import land.face.strife.data.ability.AbilityIconData;
import land.face.strife.data.ability.EntityAbilitySet;
import land.face.strife.data.ability.EntityAbilitySet.Phase;
import land.face.strife.data.ability.EntityAbilitySet.TriggerAbilityType;
import land.face.strife.data.champion.ChampionSaveData;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.champion.StrifeAttribute;
import land.face.strife.data.conditions.Condition;
import land.face.strife.data.effects.Effect;
import land.face.strife.stats.AbilitySlot;
import land.face.strife.timers.EntityAbilityTimer;
import land.face.strife.timers.SoulTimer;
import land.face.strife.util.LogUtil;
import land.face.strife.util.PlayerDataUtil;
import land.face.strife.util.TargetingUtil;
import org.apache.commons.lang.NullArgumentException;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

public class AbilityManager {

  private StrifePlugin plugin;
  private Map<String, Ability> loadedAbilities = new HashMap<>();
  private Map<LivingEntity, Set<AbilityCooldownContainer>> coolingDownAbilities = new ConcurrentHashMap<>();
  private Map<UUID, Set<AbilityCooldownContainer>> savedPlayerCooldowns = new ConcurrentHashMap<>();

  private final Random random = new Random();

  private static final String ON_COOLDOWN = TextUtils.color("&6&lAbility On Cooldown!");
  private static final String NO_ENERGY = TextUtils.color("&e&lNot enough energy!");
  private static final String NO_TARGET = TextUtils.color("&7&lNo Target Found!");
  private static final String NO_REQUIRE = TextUtils.color("&c&lAbility Requirements Not Met!");

  public AbilityManager(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  public Map<String, Ability> getLoadedAbilities() {
    return loadedAbilities;
  }

  public Ability getAbility(String name) {
    if (loadedAbilities.containsKey(name)) {
      return loadedAbilities.get(name);
    }
    LogUtil.printWarning("Attempted to get unknown ability '" + name + "'.");
    return null;
  }

  public void cooldownReduce(LivingEntity livingEntity, String abilityId, int msReduction) {
    if (!coolingDownAbilities.containsKey(livingEntity)) {
      return;
    }
    AbilityCooldownContainer container = getCooldownContainer(livingEntity, abilityId);
    if (container == null) {
      return;
    }
    int abilityCooldown = getAbility(container.getAbilityId()).getCooldown() * 1000;
    while (msReduction >= abilityCooldown && container.getSpentCharges() >= 1) {
      container.setSpentCharges(container.getSpentCharges() - 1);
      msReduction -= abilityCooldown;
    }
    container.setStartTime(container.getStartTime() - msReduction);
    container.setEndTime(container.getEndTime() - msReduction);
    if (livingEntity instanceof Player) {
      plugin.getAbilityIconManager()
          .updateIconProgress((Player) livingEntity, getAbility(abilityId));
    }
  }

  public void unToggleAbility(StrifeMob mob, String abilityId) {
    if (!coolingDownAbilities.containsKey(mob.getEntity())) {
      return;
    }
    AbilityCooldownContainer container = getCooldownContainer(mob.getEntity(), abilityId);
    if (container == null || !container.isToggledOn()) {
      return;
    }
    Ability ability = getAbility(abilityId);
    Set<LivingEntity> targets = new HashSet<>();
    targets.add(mob.getEntity());
    toggleAbility(mob, targets, ability);
  }

  public AbilityCooldownContainer getCooldownContainer(LivingEntity le, String abilityId) {
    if (coolingDownAbilities.get(le) == null) {
      coolingDownAbilities.put(le, new ConcurrentSet<>());
      return null;
    }
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
      if (le == null || !le.isValid() || coolingDownAbilities.get(le).isEmpty()) {
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

  private boolean hasEnergy(StrifeMob caster, Ability ability) {
    if (caster.getEntity() instanceof Player) {
      if (((Player) caster.getEntity()).getGameMode() == GameMode.CREATIVE) {
        return true;
      }
      return plugin.getEnergyManager().getEnergy(caster) >= ability.getCost();
    }
    return true;
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
    if (!ignoreReqs && !hasEnergy(caster, ability)) {
      doNoEnergyPrompt(caster, ability);
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
    if (targets.isEmpty() && ability.isRequireTarget()) {
      doTargetNotFoundPrompt(caster, ability);
      return false;
    }
    if (ability.getTargetType() != TargetType.TOGGLE) {
      coolDownAbility(caster.getEntity(), ability);
    } else {
      boolean isOnAfterToggle = toggleAbility(caster, targets, ability);
      if (!isOnAfterToggle) {
        return true;
      }
    }
    if (caster.getChampion() != null && ability.getAbilityIconData() != null) {
      caster.getChampion().getDetailsContainer().addWeights(ability);
    }
    if (caster.getEntity() instanceof Player
        && ((Player) caster.getEntity()).getGameMode() != GameMode.CREATIVE) {
      plugin.getEnergyManager().changeEnergy(caster, -ability.getCost(), true);
    }
    plugin.getEffectManager().execute(caster, targets, ability.getEffects());
    playChatMessages(caster, ability);
    return true;
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
        .chat("==ability==" + messages.get(random.nextInt(messages.size())));
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
        mob.setAbilityTimer(new EntityAbilityTimer(mob));
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

    LivingEntity targetEntity;
    if (target == null) {
      targetEntity = TargetingUtil.getMobTarget(caster);
      target = targetEntity == null ? null : plugin.getStrifeMobManager().getStatMob(targetEntity);
    } else {
      targetEntity = target.getEntity();
    }

    StrifeMob finalTarget = target;
    List<Ability> selectorList = abilities.stream()
        .filter(ability -> isAbilityCastReady(caster, finalTarget, ability))
        .collect(Collectors.toList());

    if (selectorList.isEmpty()) {
      LogUtil.printDebug(PlayerDataUtil.getName(caster.getEntity()) + " failed to cast " +
          phase + " type " + type);
      return false;
    }
    Ability ability = selectorList.get(random.nextInt(selectorList.size()));
    return execute(ability, caster, targetEntity, true);
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
    }
    if (container.getSpentCharges() == 0) {
      container.setStartTime(System.currentTimeMillis());
      container.setEndTime(System.currentTimeMillis() + ability.getCooldown() * 1000);
    }
    container.setSpentCharges(container.getSpentCharges() + 1);
    container.setToggledOn(false);
  }

  /*
  Returns true with the toggle state of the ability afterwards.
  Illegal state if it isn't a toggle ability at all...
   */
  private boolean toggleAbility(StrifeMob caster, Set<LivingEntity> targets, Ability ability) {
    if (ability.getTargetType() != TargetType.TOGGLE) {
      throw new IllegalStateException("Attempted to toggle a non toggle ability!");
    }
    if (!coolingDownAbilities.containsKey(caster.getEntity())) {
      coolingDownAbilities.put(caster.getEntity(), new ConcurrentSet<>());
    }
    AbilityCooldownContainer container = getCooldownContainer(caster.getEntity(), ability.getId());
    if (container == null) {
      container = new AbilityCooldownContainer(ability.getId(), 0);
      container.setToggledOn(true);
      coolingDownAbilities.get(caster.getEntity()).add(container);
      return true;
    }
    if (container.isToggledOn()) {
      coolDownAbility(caster.getEntity(), ability);
      plugin.getEffectManager().execute(caster, targets, ability.getToggleOffEffects());
      return false;
    }
    container.setToggledOn(true);
    return true;
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

  private Set<LivingEntity> getTargets(StrifeMob caster, LivingEntity target, Ability ability) {
    Set<LivingEntity> targets = new HashSet<>();
    switch (ability.getTargetType()) {
      case SELF:
      case TOGGLE:
        targets.add(caster.getEntity());
        return targets;
      case PARTY:
        if (caster.getEntity() instanceof Player) {
          targets.addAll(plugin.getSnazzyPartiesHook().getNearbyPartyMembers(
              (Player) caster.getEntity(), caster.getEntity().getLocation(), 30));
        } else {
          targets.add(caster.getEntity());
        }
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
        LivingEntity stando = TargetingUtil.getTempStand(loc, 0);
        if (stando != null) {
          targets.add(stando);
        }
        return targets;
      case TARGET_GROUND:
        Location loc2 = TargetingUtil.getTargetLocation(
            caster.getEntity(), target, ability.getRange(), ability.isRaycastsTargetEntities());
        LivingEntity stando2 = TargetingUtil.getTempStand(loc2, ability.getRange() + 3);
        if (stando2 != null) {
          targets.add(stando2);
        }
        return targets;
      case NEAREST_SOUL:
        SoulTimer soul = plugin.getSoulManager()
            .getNearestSoul(caster.getEntity(), ability.getRange());
        if (soul != null) {
          targets.add(Bukkit.getPlayer(soul.getOwner()));
        }
        return targets;
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
    ((Player) caster.getEntity())
        .playSound(caster.getEntity().getLocation(), Sound.UI_BUTTON_CLICK, 1f, 0.6f);
  }

  private void doRequirementNotMetPrompt(StrifeMob caster, Ability ability) {
    LogUtil.printDebug("Failed. Requirement not met for ability " + ability.getId());
    if (!(ability.isShowMessages() && caster.getEntity() instanceof Player)) {
      return;
    }
    MessageUtils.sendActionBar((Player) caster.getEntity(), NO_REQUIRE);
    ((Player) caster.getEntity())
        .playSound(caster.getEntity().getLocation(), Sound.UI_BUTTON_CLICK, 1f, 0.6f);
  }

  private void doOnCooldownPrompt(StrifeMob caster, Ability ability) {
    LogUtil.printDebug("Failed. Ability " + ability.getId() + " is on cooldown");
    if (!(ability.isShowMessages() && caster.getEntity() instanceof Player)) {
      return;
    }
    MessageUtils.sendActionBar((Player) caster.getEntity(), ON_COOLDOWN);
    ((Player) caster.getEntity())
        .playSound(caster.getEntity().getLocation(), Sound.UI_BUTTON_CLICK, 1f, 0.6f);
  }

  private void doNoEnergyPrompt(StrifeMob caster, Ability ability) {
    LogUtil.printDebug("Failed. Not enough energy to cast Ability " + ability.getId());
    if (!(ability.isShowMessages() && caster.getEntity() instanceof Player)) {
      return;
    }
    MessageUtils.sendActionBar((Player) caster.getEntity(), NO_ENERGY);
    ((Player) caster.getEntity())
        .playSound(caster.getEntity().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.2f, 1.3f);
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

    boolean raycastsHitEntities = cs.getBoolean("raycasts-hit-entities",
        targetType == TargetType.TARGET_GROUND);

    List<String> effectStrings = cs.getStringList("effects");
    if (effectStrings.isEmpty()) {
      LogUtil.printWarning("Skipping ability " + key + " - No effects.");
      return;
    }
    List<Effect> effects = plugin.getEffectManager().getEffects(effectStrings);

    List<String> toggleStrings = cs.getStringList("toggle-off-effects");
    if (targetType == TargetType.TOGGLE && toggleStrings.isEmpty()) {
      LogUtil.printError("Skipping. Toggle abilities must have toggle-off-effects! Ability:" + key);
      return;
    }
    List<Effect> toggleOffEffects = plugin.getEffectManager().getEffects(toggleStrings);

    int cooldown = cs.getInt("cooldown", 0);
    int maxCharges = cs.getInt("max-charges", 1);
    int globalCooldownTicks = cs.getInt("global-cooldown-ticks", 5);
    float range = (float) cs.getDouble("range", 0);
    float cost = (float) cs.getDouble("cost", 0);
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
    AbilityIconData abilityIconData = buildIconData(key, cs.getConfigurationSection("icon"));
    boolean friendly = cs.getBoolean("friendly", false);
    loadedAbilities.put(key, new Ability(key, name, effects, toggleOffEffects, targetType, range,
        cost, cooldown, maxCharges, globalCooldownTicks, showMessages, requireTarget,
        raycastsHitEntities, conditions, friendly, abilityIconData));
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
    ItemStackExtensionsKt.setUnbreakable(icon, true);
    ItemStackExtensionsKt.setCustomModelData(icon, iconSection.getInt("custom-model-data", 7999));
    ItemStackExtensionsKt.addItemFlags(icon, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS,
        ItemFlag.HIDE_ATTRIBUTES);

    AbilityIconData data = new AbilityIconData(icon);

    data.setAbilitySlot(AbilitySlot.valueOf(iconSection.getString("trigger-slot")));
    data.setLevelRequirement(iconSection.getInt("level-requirement", -1));
    data.setTotalSkillRequirement(iconSection.getInt("total-skill-requirement", -1));
    data.setBonusLevelRequirement(iconSection.getInt("bonus-level-requirement", -1));

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
