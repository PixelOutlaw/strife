package info.faceland.strife.managers;

import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.conditions.Condition;
import info.faceland.strife.data.AbilityIconData;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.data.ability.Ability;
import info.faceland.strife.data.ability.Ability.TargetType;
import info.faceland.strife.data.ability.EntityAbilitySet;
import info.faceland.strife.data.ability.EntityAbilitySet.AbilityType;
import info.faceland.strife.data.champion.LifeSkillType;
import info.faceland.strife.data.champion.StrifeAttribute;
import info.faceland.strife.effects.Effect;
import info.faceland.strife.effects.SpawnParticle;
import info.faceland.strife.effects.Wait;
import info.faceland.strife.stats.AbilitySlot;
import info.faceland.strife.util.DamageUtil;
import info.faceland.strife.util.ItemUtil;
import info.faceland.strife.util.LogUtil;
import info.faceland.strife.util.PlayerDataUtil;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

public class AbilityManager {

  private final StrifePlugin plugin;
  private final Map<String, Ability> loadedAbilities = new HashMap<>();
  private final Map<LivingEntity, Map<Ability, Integer>> coolingDownAbilities = new ConcurrentHashMap<>();
  private final Map<UUID, Map<Ability, Integer>> savedPlayerCooldowns = new ConcurrentHashMap<>();

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

  public void startAbilityCooldown(LivingEntity livingEntity, Ability ability) {
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
    }
  }

  public boolean isCooledDown(LivingEntity livingEntity, Ability ability) {
    if (!coolingDownAbilities.containsKey(livingEntity)) {
      coolingDownAbilities.put(livingEntity, new ConcurrentHashMap<>());
    }
    return !coolingDownAbilities.get(livingEntity).containsKey(ability);
  }

  public void execute(final Ability ability, final StrifeMob caster, LivingEntity target) {
    if (ability.getCooldown() != 0 && !isCooledDown(caster.getEntity(), ability)) {
      LogUtil.printDebug("Failed. Ability " + ability.getId() + " is on cooldown");
      return;
    }
    if (caster.getChampion() != null && ability.getAbilityIconData() != null) {
      caster.getChampion().getDetailsContainer().addWeights(ability);
    }
    Set<LivingEntity> targets = getTargets(caster, target, ability);
    if (targets == null) {
      LogUtil.printError("Null ability target list for " + ability.getName() + "! Somethin bork");
      return;
    }
    if (targets.isEmpty() && ability.getTargetType() == TargetType.SINGLE_OTHER) {
      doTargetNotFoundPrompt(caster, ability);
      return;
    }
    if (ability.getCooldown() != 0) {
      startAbilityCooldown(caster.getEntity(), ability);
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
      LogUtil.printDebug("Added effect " + effect.getName() + " to task list");
    }
    runEffects(caster, targets, taskEffects, waitTicks);
  }

  public void execute(Ability ability, final StrifeMob caster) {
    execute(ability, caster, null);
  }

  public void uniqueAbilityCast(StrifeMob caster, AbilityType type) {
    EntityAbilitySet abilitySet = plugin.getUniqueEntityManager().getAbilitySet(caster.getEntity());
    if (abilitySet == null) {
      return;
    }
    int phase = plugin.getUniqueEntityManager().getPhase(caster.getEntity());
    switch (type) {
      case ON_HIT:
        abilityPhaseCast(caster, abilitySet.getOnHitAbilities(), phase);
        break;
      case WHEN_HIT:
        abilityPhaseCast(caster, abilitySet.getWhenHitAbilities(), phase);
        break;
      case TIMER:
        abilityPhaseCast(caster, abilitySet.getTimerAbilities(), phase);
        break;
      case PHASE_SHIFT:
        abilityPhaseCast(caster, abilitySet.getPhaseShiftAbilities(), phase);
        break;
    }
  }

  public void checkPhaseChange(LivingEntity entity) {
    if (!plugin.getUniqueEntityManager().isUniqueEntity(entity)) {
      LogUtil.printDebug("Trying to check phase on non-unique: " + PlayerDataUtil.getName(entity));
      return;
    }
    LogUtil.printDebug(" - Checking phase switch");
    StrifeMob strifeMob = plugin.getStrifeMobManager().getStatMob(entity);
    int currentPhase = plugin.getUniqueEntityManager().getPhase(strifeMob.getEntity());
    LogUtil.printDebug(" - Current Phase: " + currentPhase);
    int newPhase = 6 - (int) Math.ceil((entity.getHealth() / entity.getMaxHealth()) / 0.2);
    LogUtil.printDebug(" - New Phase: " + newPhase);
    if (newPhase > currentPhase) {
      plugin.getUniqueEntityManager().getLiveUniquesMap().get(entity).setPhase(newPhase);
      uniqueAbilityCast(strifeMob, AbilityType.PHASE_SHIFT);
    }
  }

  private void abilityPhaseCast(StrifeMob caster, Map<Integer, List<Ability>> abilitySection,
      int phase) {
    if (phase > 5) {
      throw new IllegalArgumentException("Phase cannot be higher than 5");
    }
    if (abilitySection.containsKey(phase)) {
      executeAbilityList(caster, abilitySection.get(phase));
      return;
    }
    if (phase > 1) {
      abilityPhaseCast(caster, abilitySection, phase - 1);
    }
  }

  private void executeAbilityList(StrifeMob caster, List<Ability> abilities) {
    for (Ability a : abilities) {
      execute(a, caster);
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
        LogUtil.printDebug(" - Executing effect " + effect.getName());
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
      case SINGLE_OTHER:
        if (target != null) {
          targets.add(target);
        } else {
          targets.add(selectFirstEntityInSight(caster.getEntity(), ability.getRange()));
        }
        return targets;
      case AREA_LINE:
        return getEntitiesInLine(caster.getEntity(), ability, ability.getRange());
      case AREA_RADIUS:
        return getEntitiesInRadius(caster.getEntity(), ability, ability.getRange());
      case TARGET_AREA:
        if (target == null) {
          target = selectFirstEntityInSight(caster.getEntity(), ability.getRange());
        }
        if (target == null) {
          Location location = getTargetLocation(caster.getEntity(), (int) ability.getRange());
          return getEntitiesInRadius(location, ability, ability.getRadius());
        }
        return getEntitiesInRadius(target, ability, ability.getRadius());
    }
    return null;
  }

  private LivingEntity selectFirstEntityInSight(LivingEntity caster, double range) {
    if (caster instanceof Mob && ((Mob) caster).getTarget() != null) {
      return ((Mob) caster).getTarget();
    }
    return DamageUtil.getFirstEntityInLOS(caster, (int) range);
  }

  private Set<LivingEntity> getEntitiesInLine(LivingEntity caster, Ability ability, double range) {
    Set<LivingEntity> targets = new HashSet<>();
    Location eyeLoc = caster.getEyeLocation();
    Vector direction = caster.getEyeLocation().getDirection();
    ArrayList<Entity> entities = (ArrayList<Entity>) caster.getNearbyEntities(range, range, range);
    for (double incRange = 0; incRange >= range; incRange += 1) {
      Location loc = eyeLoc.clone().add(direction.multiply(incRange));
      for (Entity entity : entities) {
        if (!(entity instanceof LivingEntity)) {
          continue;
        }
        if (Math.abs(entity.getLocation().getX() - loc.getX()) < 1) {
          if (Math.abs(entity.getLocation().getY() - loc.getY()) < 1) {
            if (Math.abs(entity.getLocation().getZ() - loc.getZ()) < 1) {
              targets.add((LivingEntity) entity);
            }
          }
        }
      }
    }
    SpawnParticle particle = ability.getAbilityParticle();
    if (particle != null) {
      ability.getAbilityParticle()
          .playAtLocation(caster.getEyeLocation(), caster.getEyeLocation().getDirection());
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
      particle.playAtLocation(SpawnParticle.getLoc(particle.getOrigin(), le),
          le.getEyeLocation().getDirection());
    }
    targets.add(le);
    return targets;
  }

  private Set<LivingEntity> getEntitiesInRadius(Location location, Ability ability, double range) {
    SpawnParticle particle = ability.getAbilityParticle();
    if (particle != null) {
      ability.getAbilityParticle().playAtLocation(location, location.getDirection());
    }
    return DamageUtil.getLOSEntitiesAroundLocation(location, range);
  }

  private Location getTargetLocation(LivingEntity caster, double range) {
    BlockIterator bi = new BlockIterator(caster.getEyeLocation(), 0, (int) range+1);
    Block sightBlock = null;
    while (bi.hasNext()) {
      Block b = bi.next();
      if (b.getType().isSolid()) {
        sightBlock = b;
        break;
      }
    }
    if (sightBlock == null) {
      LogUtil.printDebug(" - Using MAX DISTANCE target location calculation");
      return caster.getEyeLocation().clone().add(
          caster.getEyeLocation().getDirection().multiply(range));
    }
    LogUtil.printDebug(" - Using TARGET BLOCK target location calculation");
    double dist = sightBlock.getLocation().add(0.5, 0.5, 0.5).distance(caster.getEyeLocation());
    return caster.getEyeLocation().add(
        caster.getEyeLocation().getDirection().multiply(Math.max(0, dist - 1)));
  }

  private void doTargetNotFoundPrompt(StrifeMob caster, Ability ability) {
    if (!(ability.isShowMessages() && caster instanceof Player)) {
      return;
    }
    MessageUtils.sendActionBar((Player) caster.getEntity(), NO_TARGET);
    LogUtil.printDebug("Failed. No target found for ability " + ability.getId());
    ((Player) caster.getEntity()).playSound(
        caster.getEntity().getLocation(),
        Sound.ENTITY_GENERIC_EXTINGUISH_FIRE,
        1f,
        1f);
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
    if (targetType == TargetType.TARGET_AREA && radius == 0) {
      LogUtil.printWarning("Skipping ability " + key + ". TARGET_AREA requires 'radius' > 0.");
      return;
    }
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
    SpawnParticle abilityParticle = null;
    if (StringUtils.isNotBlank(particle)) {
      abilityParticle = (SpawnParticle) plugin.getEffectManager().getEffect(particle);
    }
    loadedAbilities.put(key, new Ability(key, name, effects, targetType, range, radius, cooldown,
        showMessages, conditions, abilityIconData, abilityParticle));
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
