package info.faceland.strife.managers;

import static info.faceland.strife.data.ability.Ability.TargetType.SINGLE_OTHER;

import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.conditions.Condition;
import info.faceland.strife.data.AbilityIconData;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.data.ability.Ability;
import info.faceland.strife.data.ability.Ability.TargetType;
import info.faceland.strife.data.ability.EntityAbilitySet;
import info.faceland.strife.data.ability.EntityAbilitySet.TriggerAbilityPhase;
import info.faceland.strife.data.ability.EntityAbilitySet.TriggerAbilityType;
import info.faceland.strife.data.champion.LifeSkillType;
import info.faceland.strife.data.champion.StrifeAttribute;
import info.faceland.strife.effects.Effect;
import info.faceland.strife.effects.PlaySound;
import info.faceland.strife.effects.SpawnParticle;
import info.faceland.strife.effects.Wait;
import info.faceland.strife.stats.AbilitySlot;
import info.faceland.strife.util.ItemUtil;
import info.faceland.strife.util.LogUtil;
import info.faceland.strife.util.PlayerDataUtil;
import info.faceland.strife.util.TargetingUtil;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class AbilityManager {

  private final StrifePlugin plugin;
  private final Map<String, Ability> loadedAbilities = new HashMap<>();
  private final Map<LivingEntity, Map<Ability, Integer>> coolingDownAbilities = new ConcurrentHashMap<>();
  private final Map<UUID, Map<Ability, Integer>> savedPlayerCooldowns = new ConcurrentHashMap<>();

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

  public void cooldownReduce(LivingEntity livingEntity, Ability ability, int ticks) {
    if (!coolingDownAbilities.containsKey(livingEntity)) {
      return;
    }
    int curTicks = coolingDownAbilities.get(livingEntity).getOrDefault(ability, 0);
    if (curTicks - ticks <= 0) {
      LogUtil.printDebug(" Cd Reduce - ability " + ability.getId() + " refreshed");
      coolingDownAbilities.get(livingEntity).remove(ability);
      updateIcons(livingEntity);
      return;
    }
    int newTicks = curTicks - ticks;
    LogUtil.printDebug(" Cd Reduce - ability " + ability.getId() + " reduced from " +
        curTicks + " to " + newTicks);
    coolingDownAbilities.get(livingEntity).put(ability, newTicks);
    updateIcons(livingEntity);
  }

  private void startAbilityCooldown(LivingEntity livingEntity, Ability ability) {
    if (!coolingDownAbilities.containsKey(livingEntity)) {
      coolingDownAbilities.put(livingEntity, new ConcurrentHashMap<>());
    }
    coolingDownAbilities.get(livingEntity).put(ability, ability.getCooldown() * 20);
  }

  public double getCooldownTicks(LivingEntity livingEntity, Ability ability) {
    if (!coolingDownAbilities.containsKey(livingEntity)) {
      return 0;
    }
    return coolingDownAbilities.get(livingEntity).getOrDefault(ability, 0);
  }

  public void tickAbilityCooldowns(int tickRate) {
    for (LivingEntity le : coolingDownAbilities.keySet()) {
      if (le == null || !le.isValid()) {
        coolingDownAbilities.remove(le);
        continue;
      }
      for (Ability ability : coolingDownAbilities.get(le).keySet()) {
        int ticks = coolingDownAbilities.get(le).get(ability);
        if (ticks <= tickRate) {
          coolingDownAbilities.get(le).remove(ability);
          continue;
        }
        coolingDownAbilities.get(le).put(ability, ticks - tickRate);
      }
    }
  }

  public void savePlayerCooldowns(Player player) {
    if (coolingDownAbilities.containsKey(player)) {
      savedPlayerCooldowns.put(player.getUniqueId(), coolingDownAbilities.get(player));
      coolingDownAbilities.remove(player);
    }
  }

  public void loadPlayerCooldowns(Player player) {
    coolingDownAbilities.put(player, new ConcurrentHashMap<>());
    if (savedPlayerCooldowns.containsKey(player.getUniqueId())) {
      coolingDownAbilities.put(player, savedPlayerCooldowns.get(player.getUniqueId()));
      savedPlayerCooldowns.remove(player.getUniqueId());
      updateIcons(player);
    }
  }

  public boolean isCooledDown(LivingEntity livingEntity, Ability ability) {
    if (!coolingDownAbilities.containsKey(livingEntity)) {
      coolingDownAbilities.put(livingEntity, new ConcurrentHashMap<>());
    }
    return !coolingDownAbilities.get(livingEntity).containsKey(ability);
  }

  public boolean execute(final Ability ability, final StrifeMob caster, LivingEntity target) {
    if (ability.getCooldown() != 0 && !isCooledDown(caster.getEntity(), ability)) {
      doOnCooldownPrompt(caster, ability);
      return false;
    }
    if (!PlayerDataUtil.areConditionsMet(caster, caster, ability.getConditions())) {
      doRequirementNotMetPrompt(caster, ability);
      return false;
    }
    if (caster.getChampion() != null && ability.getAbilityIconData() != null) {
      caster.getChampion().getDetailsContainer().addWeights(ability);
    }
    Set<LivingEntity> targets = getTargets(caster, target, ability);
    if (targets == null) {
      throw new NullArgumentException("Null target list on ability " + ability.getId());
    }
    if (ability.getTargetType() == SINGLE_OTHER) {
      TargetingUtil.filterFriendlyEntities(targets, caster, ability.isFriendly());
      if (targets.isEmpty()) {
        doTargetNotFoundPrompt(caster, ability);
        return false;
      }
    }
    if (ability.getCooldown() != 0) {
      startAbilityCooldown(caster.getEntity(), ability);
    }
    if (ability.getCastSound() != null) {
      ability.getCastSound().playAtLocation(caster.getEntity().getLocation());
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

  public boolean execute(Ability ability, final StrifeMob caster) {
    return execute(ability, caster, null);
  }

  public void abilityCast(StrifeMob caster, TriggerAbilityType type) {
    EntityAbilitySet abilitySet = caster.getAbilitySet();
    if (abilitySet == null) {
      return;
    }
    checkPhaseChange(caster);
    TriggerAbilityPhase phase = abilitySet.getPhase();
    Map<TriggerAbilityPhase, Set<Ability>> abilitySection = abilitySet.getAbilities(type);
    if (abilitySection == null) {
      return;
    }
    Set<Ability> abilities = abilitySection.get(phase);
    if (abilities == null) {
      return;
    }
    for (Ability a : abilities) {
      execute(a, caster);
    }
  }

  private void checkPhaseChange(StrifeMob strifeMob) {
    if (strifeMob.getAbilitySet() == null) {
      return;
    }
    LogUtil.printDebug(" - Checking phase switch");
    TriggerAbilityPhase currentPhase = strifeMob.getAbilitySet().getPhase();
    LogUtil.printDebug(" - Current Phase: " + currentPhase);
    TriggerAbilityPhase newPhase = EntityAbilitySet.phaseFromEntityHealth(strifeMob.getEntity());
    if (newPhase.ordinal() > currentPhase.ordinal()) {
      strifeMob.getAbilitySet().setPhase(newPhase);
      LogUtil.printDebug(" - New Phase: " + newPhase);
      abilityCast(strifeMob, TriggerAbilityType.PHASE_SHIFT);
    }
  }

  private void runEffects(StrifeMob caster, Set<LivingEntity> targets, List<Effect> effectList,
      int delay) {
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> {
      LogUtil.printDebug("Effect task (Location) started - " + effectList.toString());
      if (!caster.getEntity().isValid()) {
        LogUtil.printDebug(" - Task cancelled, caster is dead");
        return;
      }
      for (Effect effect : effectList) {
        LogUtil.printDebug(" - Executing effect " + effect.getId());
        plugin.getEffectManager().execute(effect, caster, targets);
      }
      LogUtil.printDebug(" - Completed effect task.");
    }, delay);
  }

  private Set<LivingEntity> getTargets(StrifeMob caster, LivingEntity target, Ability ability) {
    Set<LivingEntity> targets = new HashSet<>();
    switch (ability.getTargetType()) {
      case SELF:
      case PARTY:
        targets.add(caster.getEntity());
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
      case AREA_LINE:
        return getEntitiesInLine(caster.getEntity(), ability, ability.getRange());
      case AREA_RADIUS:
        return getEntitiesInRadius(caster.getEntity(), ability, ability.getRange());
      case TARGET_AREA:
        Location loc = TargetingUtil
            .getTargetArea(caster.getEntity(), target, ability.getRange(), false);
        return getAreaTargets(targets, ability, loc);
      case TARGET_GROUND:
        Location loc2 = TargetingUtil
            .getTargetArea(caster.getEntity(), target, ability.getRange(), true);
        return getGroundedAreaTargets(targets, ability, loc2);
    }
    return null;
  }

  private Set<LivingEntity> getGroundedAreaTargets(Set<LivingEntity> targets, Ability ability,
      Location location) {
    for (int i = 0; i < 24; i++) {
      if (location.getBlock().getType().isSolid()) {
        location.setY(location.getBlockY() + 1.5);
        if (ability.getRadius() == 0) {
          targets.add(TargetingUtil.buildAndRemoveDetectionStand(location));
          return targets;
        }
        return getEntitiesInRadius(location, ability, ability.getRadius());
      }
      location.add(0, -1, 0);
    }
    return targets;
  }

  private Set<LivingEntity> getAreaTargets(Set<LivingEntity> targets, Ability ability,
      Location location) {
    if (ability.getRadius() == 0) {
      targets.add(TargetingUtil.buildAndRemoveDetectionStand(location));
      return targets;
    }
    return getEntitiesInRadius(location, ability, ability.getRadius());
  }

  private Set<LivingEntity> getEntitiesInLine(LivingEntity caster, Ability ability, double range) {
    Set<LivingEntity> targets = new HashSet<>();
    Location eyeLoc = caster.getEyeLocation();
    Vector direction = caster.getEyeLocation().getDirection();
    ArrayList<Entity> entities = (ArrayList<Entity>) caster.getNearbyEntities(range, range, range);
    for (double incRange = 0; incRange <= range; incRange += 1) {
      Location loc = eyeLoc.clone().add(direction.clone().multiply(incRange));
      if (loc.getBlock() != null && loc.getBlock().getType() != Material.AIR) {
        if (!loc.getBlock().getType().isTransparent()) {
          break;
        }
      }
      for (Entity entity : entities) {
        if (!(entity instanceof LivingEntity)) {
          continue;
        }
        if (Math.abs(entity.getLocation().getX() - loc.getX()) < 1) {
          if (Math.abs(entity.getLocation().getY() - loc.getY()) < 2.5) {
            if (Math.abs(entity.getLocation().getZ() - loc.getZ()) < 1) {
              targets.add((LivingEntity) entity);
            }
          }
        }
      }
    }
    SpawnParticle particle = ability.getAbilityParticle();
    if (particle != null) {
      ability.getAbilityParticle().playAtLocation(caster);
    }
    return targets;
  }

  private Set<LivingEntity> getEntitiesInRadius(LivingEntity le, Ability ability, double range) {
    Set<LivingEntity> targets = new HashSet<>();
    if (le == null) {
      LogUtil.printWarning("Null center for getEntitiesInRadius... some ability is borked...");
      return targets;
    }
    LogUtil.printDebug(" - Using TARGET ENTITY target location calculation");
    ArrayList<Entity> entities = (ArrayList<Entity>) le.getNearbyEntities(range, range, range);
    for (Entity entity : entities) {
      if (!(entity instanceof LivingEntity) || entity instanceof ArmorStand) {
        continue;
      }
      if (le.hasLineOfSight(entity)) {
        targets.add((LivingEntity) entity);
      }
    }
    SpawnParticle particle = ability.getAbilityParticle();
    if (particle != null) {
      particle.playAtLocation(TargetingUtil.getOriginLocation(le, particle.getOrigin()));
    }
    targets.add(le);
    return targets;
  }

  private Set<LivingEntity> getEntitiesInRadius(Location location, Ability ability, double range) {
    SpawnParticle particle = ability.getAbilityParticle();
    if (particle != null) {
      ability.getAbilityParticle().playAtLocation(location);
    }
    return TargetingUtil.getLOSEntitiesAroundLocation(location, range);
  }

  private void updateIcons(LivingEntity livingEntity) {
    if (livingEntity instanceof Player) {
      plugin.getAbilityIconManager().updateAbilityIconDamageMeters((Player) livingEntity, true);
    }
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
    int cooldown = cs.getInt("cooldown", 0);
    int range = cs.getInt("range", 0);
    double radius = cs.getDouble("radius", 0);
    List<String> effectStrings = cs.getStringList("effects");
    if (effectStrings.isEmpty()) {
      LogUtil.printWarning("Skipping ability " + key + " - No effects.");
      return;
    }
    List<Effect> effects = new ArrayList<>();
    for (String s : effectStrings) {
      Effect effect = plugin.getEffectManager().getEffect(s);
      if (effect == null) {
        LogUtil.printWarning(" Failed to add unknown effect '" + s + "' to ability '" + key + "'");
        continue;
      }
      effects.add(effect);
      LogUtil.printDebug(" Added effect '" + s + "' to ability '" + key + "'");
    }
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
    String particle = cs.getString("particle");
    boolean friendly = cs.getBoolean("friendly", false);
    SpawnParticle abilityParticle = null;
    if (StringUtils.isNotBlank(particle)) {
      abilityParticle = (SpawnParticle) plugin.getEffectManager().getEffect(particle);
    }
    String sound = cs.getString("cast-sound");
    PlaySound playSound = null;
    if (StringUtils.isNotBlank(sound)) {
      playSound = (PlaySound) plugin.getEffectManager().getEffect(sound);
    }
    loadedAbilities.put(key, new Ability(key, name, effects, targetType, range, radius, cooldown,
        showMessages, conditions, friendly, abilityIconData, abilityParticle, playSound));
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
