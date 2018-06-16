package info.faceland.strife.managers;

import com.tealcube.minecraft.bukkit.TextUtils;
import gyurix.spigotlib.ChatAPI;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.Ability;
import info.faceland.strife.data.Ability.TargetType;
import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.data.EntityAbilitySet;
import info.faceland.strife.data.EntityAbilitySet.AbilityType;
import info.faceland.strife.effects.Effect;
import info.faceland.strife.effects.Wait;
import info.faceland.strife.util.LogUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class AbilityManager {

  private final EffectManager effectManager;
  private final UniqueEntityManager uniqueEntityManager;
  private final Map<String, Ability> loadedAbilities;

  private static final String ON_COOLDOWN = TextUtils.color("&f&lAbility On Cooldown!");
  private static final String NO_TARGET = TextUtils.color("&e&lNo Target Found!");
  private static final String TEST_CHICKEN = ChatColor.RED + "TEST CHICKEN PLS IGNORE";

  public AbilityManager(EffectManager effectManager, UniqueEntityManager uniqueEntityManager) {
    this.effectManager = effectManager;
    this.uniqueEntityManager = uniqueEntityManager;
    this.loadedAbilities = new HashMap<>();
  }

  public Ability getAbility(String name) {
    if (loadedAbilities.containsKey(name)) {
      return loadedAbilities.get(name);
    }
    LogUtil.printWarning("Attempted to get unknown ability '" + name + "'.");
    return null;
  }

  public Map<String, Ability> getLoadedAbilities() {
    return loadedAbilities;
  }

  private void execute(Ability ability, final AttributedEntity caster) {
    LogUtil.printDebug(caster.getEntity().getCustomName() + " is casting ability: " + ability.getId());
    if (System.currentTimeMillis() < ability.getReadyTime()) {
      LogUtil.printDebug("Failed. Ability " + ability.getId() + " is on cooldown");
      if (caster instanceof Player) {
        ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, ON_COOLDOWN, (Player) caster);
      }
      return;
    }
    LivingEntity target = getTarget(caster, ability);
    if (target == null) {
      if (caster instanceof Player) {
        ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR, NO_TARGET, (Player) caster);
      }
      LogUtil.printDebug("Failed. No target found for ability " + ability.getId());
      return;
    }
    ability.setReadyTime(System.currentTimeMillis() + ability.getCooldown() * 1000);
    List<Effect> taskEffects = new ArrayList<>();
    int waitTicks = 0;
    for (Effect effect : ability.getEffects()) {
      if (effect instanceof Wait) {
        LogUtil.printDebug("ree " + taskEffects.toString());
        runEffects(caster, target, taskEffects, waitTicks);
        waitTicks += ((Wait) effect).getTickDelay();
        taskEffects = new ArrayList<>();
        continue;
      }
      taskEffects.add(effect);
      LogUtil.printDebug("Added effect " + effect.getName() + " to task list");
    }
    runEffects(caster, target, taskEffects, waitTicks);
    if (TEST_CHICKEN.equals(target.getCustomName())) {
      target.remove();
    }
  }

  public void uniqueAbilityCast(AttributedEntity caster, AbilityType type) {
    int phase = fetchAndUpdatePhase(caster);
    EntityAbilitySet entityAbilitySet = uniqueEntityManager.getAbilitySet(caster.getEntity());
    switch (type) {
      case ON_HIT:
        abilityPhaseCast(caster, entityAbilitySet.getOnHitAbilities(), phase);
        break;
      case WHEN_HIT:
        abilityPhaseCast(caster, entityAbilitySet.getWhenHitAbilities(), phase);
        break;
      case TIMER:
        abilityPhaseCast(caster, entityAbilitySet.getTimerAbilities(), phase);
        break;
      case PHASE_SHIFT:
        abilityPhaseCast(caster, entityAbilitySet.getPhaseShiftAbilities(), phase);
        break;
    }
  }

  private int fetchAndUpdatePhase(AttributedEntity attributedEntity) {
    LivingEntity livingEntity = attributedEntity.getEntity();
    int currentPhase = uniqueEntityManager.getPhase(attributedEntity.getEntity());
    int newPhase = 6 - (int)Math.ceil((livingEntity.getHealth()/livingEntity.getMaxHealth()) / 0.2);
    if (newPhase > currentPhase) {
      uniqueEntityManager.getLiveUniquesMap().get(livingEntity).getAbilitySet().setPhase(newPhase);
      uniqueAbilityCast(attributedEntity, AbilityType.PHASE_SHIFT);
    }
    return newPhase;
  }

  private void abilityPhaseCast(AttributedEntity caster, Map<Integer, List<Ability>> abilitySection,
      int phase) {
    if (phase > 5) {
      LogUtil.printError("Attempted to use ability phase higher than 5? Likely a code bug...");
      return;
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

  private void runEffects(AttributedEntity caster, LivingEntity target, List<Effect> effectList, int delay) {
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), new Runnable() {
      @Override
      public void run() {
        LogUtil.printDebug("Effect task started - " + effectList.toString());
        if (!caster.getEntity().isValid()) {
          LogUtil.printDebug("Task cancelled, caster is dead");
          return;
        }
        for (Effect effect : effectList) {
          LogUtil.printDebug("Executing effect " + effect.getName());
          effect.execute(caster, target);
        }
        LogUtil.printDebug("Completed effect task.");
      }
    }, delay);
  }

  private LivingEntity selectFirstEntityInSight(LivingEntity caster, int range) {
    if (caster instanceof Creature && ((Creature) caster).getTarget() != null) {
      return ((Creature) caster).getTarget();
    }
    ArrayList<Entity> entities = (ArrayList<Entity>) caster.getNearbyEntities(range, range, range);
    ArrayList<Block> sightBlock = (ArrayList<Block>) caster.getLineOfSight(null, range);
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

  private LivingEntity getTarget(AttributedEntity caster, Ability ability) {
    switch (ability.getTargetType()) {
      case SELF:
        return caster.getEntity();
      case OTHER:
        return selectFirstEntityInSight(caster.getEntity(), (int) ability.getRange());
      case RANGE:
        LivingEntity target = selectFirstEntityInSight(caster.getEntity(), (int) ability.getRange());
        if (target == null) {
          target = getBackupEntity(caster.getEntity(), ability.getRange());
        }
        return target;
    }
    return null;
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
    int cooldown = cs.getInt("cooldown", 10);
    int range = cs.getInt("range", 0);
    List<String> effectStrings = cs.getStringList("effects");
    if (effectStrings.isEmpty()) {
      LogUtil.printWarning("Skipping ability " + key + " - No effects.");
      return;
    }
    List<Effect> effects = new ArrayList<>();
    for (String s : effectStrings) {
      Effect effect = effectManager.getEffect(s);
      if (effect == null) {
        LogUtil.printWarning("Ability " + key + " tried to add unknown effect" + s);
        continue;
      }
      effects.add(effect);
      LogUtil.printDebug("Added effect " + effect.getName() + " (" + s + ") to ability " + key);
    }
    loadedAbilities.put(key, new Ability(key, name, effects, targetType, range, cooldown));
    LogUtil.printDebug("Loaded ability " + key + " successfully.");
  }
}
