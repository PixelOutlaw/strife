package land.face.strife.managers;

import com.tealcube.minecraft.bukkit.facecore.utilities.AdvancedActionBarUtil;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import io.pixeloutlaw.minecraft.spigot.garbage.ListExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import land.face.strife.StrifePlugin;
import land.face.strife.data.NoticeData;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.TargetResponse;
import land.face.strife.data.ability.Ability;
import land.face.strife.data.ability.Ability.TargetType;
import land.face.strife.data.ability.CooldownTracker;
import land.face.strife.data.ability.AbilityIconData;
import land.face.strife.data.ability.EntityAbilitySet;
import land.face.strife.data.ability.EntityAbilitySet.Phase;
import land.face.strife.data.ability.EntityAbilitySet.TriggerAbilityType;
import land.face.strife.data.champion.ChampionSaveData;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.champion.StrifeAttribute;
import land.face.strife.data.conditions.Condition;
import land.face.strife.data.effects.Effect;
import land.face.strife.events.AbilityCastEvent;
import land.face.strife.stats.AbilitySlot;
import land.face.strife.stats.StrifeStat;
import land.face.strife.timers.EntityAbilityTimer;
import land.face.strife.timers.SoulTimer;
import land.face.strife.util.LogUtil;
import land.face.strife.util.PlayerDataUtil;
import land.face.strife.util.StatUtil;
import land.face.strife.util.TargetingUtil;
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
import org.jetbrains.annotations.NotNull;

public class AbilityManager {

  private final StrifePlugin plugin;

  private final Map<String, Ability> loadedAbilities = new HashMap<>();
  private final Map<LivingEntity, Set<CooldownTracker>> cdMap = new ConcurrentHashMap<>();
  private final Map<UUID, Set<CooldownTracker>> savedCooldowns = new ConcurrentHashMap<>();

  private final Random random = new Random();

  /*
  private static final String ON_COOLDOWN = StringExtensionsKt
      .chatColorize("&6&lAbility On Cooldown!");
  private static final String NO_ENERGY = StringExtensionsKt
      .chatColorize("&e&lNot enough energy! (&7&l{n1}&e&l/{n2})");
  private static final String NO_TARGET = StringExtensionsKt
      .chatColorize("&7&lNo Target Found!");
  private static final String NO_REQUIRE = StringExtensionsKt
      .chatColorize("&c&lAbility Requirements Not Met!");
   */
  private static final String CAST = StringExtensionsKt
      .chatColorize("&a&l&oCast &f&l&o{n}&a&l&o!");

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

  public boolean execute(final Ability ability, final StrifeMob caster, StrifeMob target) {
    return execute(ability, caster, target, false);
  }

  public boolean execute(final Ability ability, final StrifeMob caster,
      final StrifeMob target, boolean ignoreReqs) {
    if (!ignoreReqs) {
      if (ability.getCooldown() != 0 && !canBeCast(caster, ability)) {
        doOnCooldownPrompt(caster, ability);
        return false;
      }
      if (!hasEnergy(caster, ability)) {
        doNoEnergyPrompt(caster, ability);
        return false;
      }
      if (!PlayerDataUtil.areConditionsMet(caster, target, ability.getConditions())) {
        doRequirementNotMetPrompt(caster, ability);
        return false;
      }
    }
    TargetResponse response = getTargets(caster, target, ability);
    if (response.getLocation() == null && (response.getEntities() == null ||
        response.getEntities().isEmpty())) {
      doTargetNotFoundPrompt(caster, ability);
      return false;
    }
    if (ability.getTargetType() == TargetType.TOGGLE) {
      boolean toggledOn = toggleAbility(caster, ability);
      if (!toggledOn) {
        return true;
      }
    } else {
      coolDownAbility(caster, ability);
    }
    if (caster.getChampion() != null && ability.getAbilityIconData() != null) {
      caster.getChampion().getDetailsContainer().addWeights(ability);
    }
    if (caster.getEntity() instanceof Player) {
      if (((Player) caster.getEntity()).getGameMode() != GameMode.CREATIVE) {
        caster.setEnergy(caster.getEnergy() - ability.getCost());
      }
    }

    if (ability.getAbilityIconData() != null) {
      AbilityCastEvent abilityCastEvent = new AbilityCastEvent(caster, ability);
      Bukkit.getPluginManager().callEvent(abilityCastEvent);
    }

    plugin.getEffectManager().processEffectList(caster, response, ability.getEffects());

    if (caster.getEntity() instanceof Player) {
      if (ability.isCancelStealth()) {
        plugin.getStealthManager().unstealthPlayer((Player) caster.getEntity());
      }
      if (ability.isShowMessages()) {
        AdvancedActionBarUtil.addMessage((Player) caster.getEntity(), "ability-status",
            CAST.replace("{n}", ability.getId()), 20, 11);
      }
    }
    playChatMessages(caster, ability);
    return true;
  }

  public void reduceCooldowns(LivingEntity livingEntity, String abilityId, long msReduction) {
    if (!cdMap.containsKey(livingEntity)) {
      return;
    }
    CooldownTracker tracker = getCooldownTracker(livingEntity, abilityId);
    if (tracker == null) {
      return;
    }
    tracker.reduce(msReduction);
  }

  public void unToggleAll(LivingEntity livingEntity) {
    if (!cdMap.containsKey(livingEntity)) {
      return;
    }
    for (CooldownTracker cooldownContainer : cdMap.get(livingEntity)) {
      doToggleOff(cooldownContainer, plugin.getStrifeMobManager().getStatMob(livingEntity));
    }
  }

  public void unToggleAbility(StrifeMob mob, String abilityId) {
    CooldownTracker container = getCooldownTracker(mob.getEntity(), abilityId);
    if (container == null) {
      return;
    }
    doToggleOff(container, mob);
  }

  private void doToggleOff(CooldownTracker tracker, StrifeMob mob) {
    if (!tracker.isToggleState()) {
      return;
    }
    tracker.setToggleState(false);
    Ability ability = tracker.getAbility();
    coolDownAbility(mob, ability);

    Set<LivingEntity> targets = new HashSet<>();
    targets.add(mob.getEntity());
    TargetResponse response = new TargetResponse(targets);

    plugin.getEffectManager().processEffectList(mob, response, ability.getToggleOffEffects());
  }

  public CooldownTracker getCooldownTracker(LivingEntity le, String abilityId) {
    if (cdMap.get(le) == null) {
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
    cdMap.put(player, new HashSet<>());
    if (!savedCooldowns.containsKey(player.getUniqueId())) {
      return;
    }
    for (CooldownTracker container : savedCooldowns.get(player.getUniqueId())) {
      container.setStartTime(System.currentTimeMillis());
      cdMap.get(player).add(container);
      container.updateIcon();
    }
    savedCooldowns.remove(player.getUniqueId());
  }

  private boolean hasEnergy(StrifeMob caster, Ability ability) {
    if (caster.getEntity() instanceof Player) {
      if (((Player) caster.getEntity()).getGameMode() == GameMode.CREATIVE) {
        return true;
      }
      return caster.getEnergy() >= ability.getCost();
    }
    return true;
  }

  public Map<StrifeStat, Float> getApplicableAbilityPassiveStats(Player player,
      @NotNull Ability ability) {
    if (ability.isPassiveStatsOnCooldown()) {
      if (ability.getTargetType() == TargetType.TOGGLE) {
        CooldownTracker tracker = getCooldownTracker(player, ability.getId());
        if (tracker != null && tracker.isToggleState()) {
          return StatUpdateManager.combineMaps(ability.getPassiveStats(), ability.getTogglePassiveStats());
        }
        return ability.getPassiveStats();
      }
      return ability.getPassiveStats();
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
    if (type == TriggerAbilityType.WHEN_HIT) {
    }
    if (abilitySet == null) {
      return false;
    }
    determinePhase(caster);
    Set<Ability> abilities = EntityAbilitySet.getAbilities(abilitySet, type);
    if (abilities == null || abilities.isEmpty()) {
      return false;
    }

    if (type == TriggerAbilityType.PHASE_SHIFT) {
      for (Ability a : abilities) {
        execute(a, caster, caster);
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
        .filter(ability -> isAbilityCastReady(caster, finalTarget, ability))
        .collect(Collectors.toList());

    if (selectorList.isEmpty()) {
      return false;
    }
    Ability ability = selectorList.get(random.nextInt(selectorList.size()));
    return execute(ability, caster, target, true);
  }

  public void setGlobalCooldown(Player player, Ability ability) {
    setGlobalCooldown(player, ability.getGlobalCooldownTicks());
  }

  public void setGlobalCooldown(Player player, int ticks) {
    player.setCooldown(Material.DIAMOND_CHESTPLATE,
        Math.max(player.getCooldown(Material.DIAMOND_CHESTPLATE), ticks));
  }

  private void coolDownAbility(StrifeMob caster, Ability ability) {
    if (ability.getGlobalCooldownTicks() > 0) {
      caster.bumpGlobalCooldown(ability.getGlobalCooldownTicks() * 50);
    }
    if (ability.getCooldown() < 1) {
      return;
    }
    if (!cdMap.containsKey(caster.getEntity())) {
      cdMap.put(caster.getEntity(), new HashSet<>());
    }
    CooldownTracker tracker = getCooldownTracker(caster.getEntity(), ability.getId());
    if (tracker == null) {
      tracker = new CooldownTracker(caster, ability);
      cdMap.get(caster.getEntity()).add(tracker);
      if (ability.getTargetType() == TargetType.TOGGLE) {
        tracker.setToggleState(true);
      }
    }
    tracker.setChargesLeft(tracker.getChargesLeft() - 1);
  }

  /*
    Returns true with the toggle state of the ability afterwards.
    Illegal state if it isn't a toggle ability at all...
   */
  private boolean toggleAbility(StrifeMob caster, Ability ability) {
    if (ability.getTargetType() != TargetType.TOGGLE) {
      throw new IllegalStateException("Attempted to toggle a non toggle ability!");
    }
    if (!cdMap.containsKey(caster.getEntity())) {
      cdMap.put(caster.getEntity(), new HashSet<>());
    }
    CooldownTracker tracker = getCooldownTracker(caster.getEntity(), ability.getId());
    if (tracker == null) {
      tracker = new CooldownTracker(caster, ability);
      tracker.setToggleState(true);
      cdMap.get(caster.getEntity()).add(tracker);
      return true;
    }
    if (tracker.isToggleState()) {
      tracker.setToggleState(false);
      coolDownAbility(caster, ability);

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
        return new TargetResponse(targets, true);
      }
      case PARTY -> {
        if (caster.getEntity() instanceof Player) {
          targets.addAll(
              plugin.getSnazzyPartiesHook().getNearbyPartyMembers((Player) caster.getEntity(),
                  caster.getEntity().getLocation(), 30));
        } else {
          targets.add(caster.getEntity());
        }
        return new TargetResponse(targets, true);
      }
      case MASTER -> {
        if (caster.getMaster() != null) {
          targets.add(caster.getMaster().getEntity());
        }
        return new TargetResponse(targets, true);
      }
      case MINIONS -> {
        for (StrifeMob mob : caster.getMinions()) {
          targets.add(mob.getEntity());
        }
        return new TargetResponse(targets, true);
      }
      case SINGLE_OTHER -> {
        if (target != null && target.getEntity() != null) {
          targets.add(target.getEntity());
          return new TargetResponse(targets);
        }
        LivingEntity newTarget = TargetingUtil.selectFirstEntityInSight(caster.getEntity(),
            ability.getRange(), ability.isFriendly());
        if (newTarget != null) {
          targets.add(newTarget);
        }
        return new TargetResponse(targets, true);
      }
      case TARGET_AREA -> {
        LivingEntity targetEntity = (target == null || target.getEntity() == null)
            ? null : target.getEntity();
        Location loc = TargetingUtil.getTargetLocation(caster.getEntity(), targetEntity,
            ability.getRange(), ability.isRaycastsTargetEntities());
        return new TargetResponse(loc);
      }
      case TARGET_GROUND -> {
        LivingEntity targetEntity = (target == null || target.getEntity() == null)
            ? null : target.getEntity();
        Location loc2 = TargetingUtil.getTargetLocation(caster.getEntity(), targetEntity,
            ability.getRange(), ability.isRaycastsTargetEntities());
        loc2 = TargetingUtil.modifyLocation(loc2, ability.getRange() + 2);
        return new TargetResponse(loc2);
      }
      case NEAREST_SOUL -> {
        SoulTimer soul = plugin.getSoulManager().getNearestSoul(caster.getEntity(),
            ability.getRange());
        if (soul != null) {
          targets.add(soul.getOwner());
        }
        TargetResponse response = new TargetResponse(targets, true);
        response.setForce(true);
        return response;
      }
    }
    return new TargetResponse(new HashSet<>());
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
    plugin.getGuiManager().postNotice((Player) caster.getEntity(), new NoticeData(GuiManager.NOTICE_INVALID_TARGET, 60,10));
    ((Player) caster.getEntity()).playSound(caster.getEntity().getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.6f);
  }

  private void doRequirementNotMetPrompt(StrifeMob caster, Ability ability) {
    LogUtil.printDebug("Failed. Requirement not met for ability " + ability.getId());
    if (!(ability.isShowMessages() && caster.getEntity() instanceof Player)) {
      return;
    }
    plugin.getGuiManager().postNotice((Player) caster.getEntity(), new NoticeData(GuiManager.NOTICE_REQUIREMENT, 90,10));
    ((Player) caster.getEntity())
        .playSound(caster.getEntity().getLocation(), Sound.UI_BUTTON_CLICK, 1f, 0.6f);
  }

  private void doOnCooldownPrompt(StrifeMob caster, Ability ability) {
    LogUtil.printDebug("Failed. Ability " + ability.getId() + " is on cooldown");
    if (!(ability.isShowMessages() && caster.getEntity() instanceof Player)) {
      return;
    }
    plugin.getGuiManager().postNotice((Player) caster.getEntity(), new NoticeData(GuiManager.NOTICE_COOLDOWN, 54,10));
    ((Player) caster.getEntity())
        .playSound(caster.getEntity().getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.6f);
    Bukkit.getScheduler().runTaskLater(plugin, () -> ((Player) caster.getEntity())
        .playSound(caster.getEntity().getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.6f), 2L);
  }

  private void doNoEnergyPrompt(StrifeMob caster, Ability ability) {
    LogUtil.printDebug("Failed. Not enough energy to cast Ability " + ability.getId());
    if (!(ability.isShowMessages() && caster.getEntity() instanceof Player)) {
      return;
    }
    plugin.getGuiManager().postNotice((Player) caster.getEntity(), new NoticeData(GuiManager.NOTICE_ENERGY, 49,10));
    ((Player) caster.getEntity())
        .playSound(caster.getEntity().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 1.5f);
  }

  public void loadAbility(String key, ConfigurationSection cs) {
    if (cs == null) {
      return;
    }
    String name = StringExtensionsKt
        .chatColorize(Objects.requireNonNull(cs.getString("name", key)));
    TargetType targetType;
    try {
      targetType = TargetType.valueOf(cs.getString("target-type"));
    } catch (Exception e) {
      LogUtil.printWarning("Skipping load of ability " + key + " - Invalid target type.");
      return;
    }

    boolean raycastsHitEntities = cs
        .getBoolean("raycasts-hit-entities", targetType == TargetType.TARGET_GROUND);

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
    boolean passivesOnCooldown = cs.getBoolean("passive-stats-on-cooldown", false);
    boolean cancelStealth = cs.getBoolean("cancel-stealth", true);

    Ability ability = new Ability(key, name, effects, toggleOffEffects, targetType, range,
        cost, cooldown, maxCharges, globalCooldownTicks, showMessages, requireTarget,
        raycastsHitEntities, conditions, passivesOnCooldown, friendly, abilityIconData,
        cancelStealth);

    ability.getPassiveStats().putAll(StatUtil.getStatMapFromSection(
        cs.getConfigurationSection("passive-stats")));
    ability.getPassiveStats().putAll(StatUtil.getStatMapFromSection(
        cs.getConfigurationSection("passive-toggle-stats")));

    loadedAbilities.put(key, ability);
    LogUtil.printDebug("Loaded ability " + key + " successfully.");
  }

  private AbilityIconData buildIconData(String key, ConfigurationSection iconSection) {
    if (iconSection == null) {
      return null;
    }
    LogUtil.printDebug("Ability " + key + " has icon!");
    String format = StringExtensionsKt.chatColorize(
        Objects.requireNonNull(iconSection.getString("format", "&f&l")));
    Material material = Material.valueOf(iconSection.getString("material"));
    List<String> lore = ListExtensionsKt.chatColorize(iconSection.getStringList("lore"));
    ItemStack icon = new ItemStack(material);
    ItemStackExtensionsKt.setDisplayName(icon, format + AbilityIconManager.ABILITY_PREFIX + key);
    TextUtils.setLore(icon, lore);
    ItemStackExtensionsKt.setUnbreakable(icon, true);
    ItemStackExtensionsKt.setCustomModelData(icon, iconSection.getInt("custom-model-data", 7999));
    icon.addItemFlags(ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);

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
