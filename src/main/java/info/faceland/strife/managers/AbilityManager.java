package info.faceland.strife.managers;

import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.ability.Ability;
import info.faceland.strife.data.ability.Ability.TargetType;
import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.data.ability.EntityAbilitySet;
import info.faceland.strife.data.ability.EntityAbilitySet.AbilityType;
import info.faceland.strife.conditions.Condition;
import info.faceland.strife.effects.Effect;
import info.faceland.strife.effects.Wait;
import info.faceland.strife.util.LogUtil;
import info.faceland.strife.util.PlayerDataUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class AbilityManager {

  private final StrifePlugin plugin;
  private final Map<String, Ability> loadedAbilities = new HashMap<>();
  private final Set<Material> ignoredMaterials = new HashSet<>();

  private static final String ON_COOLDOWN = TextUtils.color("&f&lAbility On Cooldown!");
  private static final String NO_TARGET = TextUtils.color("&e&lNo Target Found!");
  private static final String TEST_CHICKEN = ChatColor.RED + "TEST CHICKEN PLS IGNORE";

  public AbilityManager(StrifePlugin plugin) {
    this.plugin = plugin;
    this.ignoredMaterials.add(Material.AIR);
    this.ignoredMaterials.add(Material.TALL_GRASS);
  }

  public Ability getAbility(String name) {
    if (loadedAbilities.containsKey(name)) {
      return loadedAbilities.get(name);
    }
    LogUtil.printWarning("Attempted to get unknown ability '" + name + "'.");
    return null;
  }

  public void execute(final Ability ability, final AttributedEntity caster,
      AttributedEntity target) {
    if (ability.getCooldown() != 0 && !caster.isCooledDown(ability)) {
      LogUtil.printDebug("Failed. Ability " + ability.getId() + " is on cooldown");
      if (ability.isDisplayCd() && caster.getEntity() instanceof Player) {
        MessageUtils.sendActionBar((Player) caster.getEntity(), ON_COOLDOWN);
      }
      return;
    }
    if (ability.getTargetType() == TargetType.SELF) {
      target = caster;
    }
    if (!PlayerDataUtil.areConditionsMet(caster, target, ability.getConditions())) {
      LogUtil.printDebug("Conditions not met for ability. Failed.");
      return;
    }
    LivingEntity targetEntity;
    if (target == null) {
      targetEntity = getTarget(caster, ability);
      if (targetEntity == null) {
        if (ability.isDisplayCd() && caster instanceof Player) {
          MessageUtils.sendActionBar((Player) caster.getEntity(), NO_TARGET);
        }
        LogUtil.printDebug("Failed. No target found for ability " + ability.getId());
        return;
      }
      target = plugin.getAttributedEntityManager().getAttributedEntity(targetEntity);
    } else {
      targetEntity = target.getEntity();
    }
    LogUtil.printDebug("Target: " + PlayerDataUtil.getName(targetEntity));
    if (ability.getCooldown() != 0) {
      caster.setCooldown(ability);
    }
    List<Effect> taskEffects = new ArrayList<>();
    int waitTicks = 0;
    for (Effect effect : ability.getEffects()) {
      if (effect instanceof Wait) {
        LogUtil.printDebug("Effects in this chunk: " + taskEffects.toString());
        runEffects(caster, target, taskEffects, waitTicks);
        waitTicks += ((Wait) effect).getTickDelay();
        taskEffects = new ArrayList<>();
        continue;
      }
      taskEffects.add(effect);
      LogUtil.printDebug("Added effect " + effect.getName() + " to task list");
    }
    runEffects(caster, target, taskEffects, waitTicks);
    if (TEST_CHICKEN.equals(targetEntity.getCustomName())) {
      targetEntity.remove();
    }
  }

  public void execute(Ability ability, final AttributedEntity caster) {
    execute(ability, caster, null);
  }

  public void uniqueAbilityCast(AttributedEntity caster, AbilityType type) {
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

  public void checkPhaseChange(AttributedEntity attributedEntity) {
    LogUtil.printDebug("Checking phase switch");
    LivingEntity livingEntity = attributedEntity.getEntity();
    int currentPhase = plugin.getUniqueEntityManager().getPhase(attributedEntity.getEntity());
    int newPhase =
        6 - (int) Math.ceil((livingEntity.getHealth() / livingEntity.getMaxHealth()) / 0.2);
    LogUtil.printDebug("currentPhase: " + currentPhase + " | newPhase: " + newPhase);
    if (newPhase > currentPhase) {
      plugin.getUniqueEntityManager().getLiveUniquesMap().get(livingEntity).setPhase(newPhase);
      uniqueAbilityCast(attributedEntity, AbilityType.PHASE_SHIFT);
    }
  }

  private void abilityPhaseCast(AttributedEntity caster, Map<Integer, List<Ability>> abilitySection,
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

  private void executeAbilityList(AttributedEntity caster, List<Ability> abilities) {
    for (Ability a : abilities) {
      execute(a, caster);
    }
  }

  private void runEffects(AttributedEntity caster, AttributedEntity target, List<Effect> effectList,
      int delay) {
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> {
      LogUtil.printDebug("Effect task started - " + effectList.toString());
      if (!caster.getEntity().isValid()) {
        LogUtil.printDebug("Task cancelled, caster is dead");
        return;
      }
      for (Effect effect : effectList) {
        LogUtil.printDebug("Executing effect " + effect.getName());
        plugin.getEffectManager().execute(effect, caster, target);
      }
      LogUtil.printDebug("Completed effect task.");
    }, delay);
  }

  private LivingEntity getTarget(AttributedEntity caster, Ability ability) {
    switch (ability.getTargetType()) {
      case SELF:
        return caster.getEntity();
      case OTHER:
        return selectFirstEntityInSight(caster.getEntity(), (int) ability.getRange());
      case RANGE:
        LivingEntity target = selectFirstEntityInSight(caster.getEntity(),
            (int) ability.getRange());
        if (target == null) {
          target = getBackupEntity(caster.getEntity(), ability.getRange());
        }
        return target;
    }
    return null;
  }

  private LivingEntity selectFirstEntityInSight(LivingEntity caster, int range) {
    if (caster instanceof Creature && ((Creature) caster).getTarget() != null) {
      LogUtil.printDebug("Creature target found. Using it instead of raycast");
      return ((Creature) caster).getTarget();
    }
    LogUtil.printDebug("No creature target found. Using raycast");
    ArrayList<Entity> entities = (ArrayList<Entity>) caster.getNearbyEntities(range, range, range);
    ArrayList<Block> sightBlock = (ArrayList<Block>) caster.getLineOfSight(ignoredMaterials, range);
    ArrayList<Location> sight = new ArrayList<>();
    for (Block b : sightBlock) {
      sight.add(b.getLocation());
    }
    for (Location loc : sight) {
      for (Entity entity : entities) {
        if (!(entity instanceof LivingEntity)) {
          continue;
        }
        if (Math.abs(entity.getLocation().getX() - loc.getX()) < 1.3) {
          if (Math.abs(entity.getLocation().getY() - loc.getY()) < 1.5) {
            if (Math.abs(entity.getLocation().getZ() - loc.getZ()) < 1.3) {
              return (LivingEntity) entity;
            }
          }
        }
      }
    }
    return null;
  }

  private LivingEntity getBackupEntity(LivingEntity caster, double range) {
    Block block = caster.getTargetBlock(null, (int) range);
    Location location;
    if (block != null) {
      location = block.getLocation().clone();
      location.subtract(caster.getLocation().getDirection().normalize().multiply(0.65));
    } else {
      location = caster.getLocation().clone();
      location.add(location.getDirection().normalize().multiply(range - 0.65));
    }
    LivingEntity chicken = (Chicken) location.getWorld().spawnEntity(location, EntityType.CHICKEN);
    chicken.setCustomNameVisible(true);
    chicken.setCustomName(TEST_CHICKEN);
    return chicken;
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
    List<String> effectStrings = cs.getStringList("effects");
    if (effectStrings.isEmpty()) {
      LogUtil.printWarning("Skipping ability " + key + " - No effects.");
      return;
    }
    List<Effect> effects = new ArrayList<>();
    for (String s : effectStrings) {
      Effect effect = plugin.getEffectManager().getEffect(s);
      if (effect == null) {
        LogUtil.printWarning(" Failed to add unknown effect '" + s + "' to ability '" + s + "'");
        continue;
      }
      effects.add(effect);
      LogUtil.printDebug(" Added effect '" + s + "' to ability '" + key + "'");
    }
    boolean displayCd = cs.getBoolean("show-cooldown-messages", false);
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
    loadedAbilities.put(key,
        new Ability(key, name, effects, targetType, range, cooldown, displayCd, conditions));
    LogUtil.printDebug("Loaded ability " + key + " successfully.");
  }
}
