package land.face.strife.patch;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import land.face.strife.StrifePlugin;
import land.face.strife.data.UniqueEntity;
import land.face.strife.patch.FloatBehindGoalPatcher.FloatBehindGoal;
import land.face.strife.patch.ReturnHomePatcher.ReturnHomeGoal;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fish;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.jetbrains.annotations.NotNull;

public class AttackGoalPatcher {

  public static void addGoals(Mob mob, UniqueEntity uniqueEntity) {
    for (String goal : uniqueEntity.getAddGoals()) {
      switch (mob.getType()) {
        case BAT -> addGoal(mob, goal, uniqueEntity.getAttackSound(), 2.75,
            uniqueEntity.getFollowRange(), uniqueEntity.isAggressiveAi(), true);
        case TROPICAL_FISH, PUFFERFISH, COD, SALMON -> FishPatcher.patchFish((Fish) mob);
        default -> addGoal(mob, goal, uniqueEntity.getAttackSound(), 3.25,
            uniqueEntity.getFollowRange(), uniqueEntity.isAggressiveAi(), false);
      }
    }
  }

  public static void addGoal(Mob mob, String id, Sound sound, double attackRage, float followRange,
      boolean aggressive, boolean flying) {
    switch (id) {
      case "strife:generic_attack_goal" -> {
        AttackEntityGoal goalAttack = new AttackEntityGoal(mob, sound, attackRage, aggressive, flying);
        if (!Bukkit.getMobGoals().hasGoal(mob, goalAttack.getKey())) {
          Bukkit.getMobGoals().addGoal(mob, 3, goalAttack);
        }
      }
      case "strife:return_home" -> {
        ReturnHomeGoal returnHomeGoal = new ReturnHomeGoal(mob, Math.pow(followRange + 3, 2));
        if (!Bukkit.getMobGoals().hasGoal(mob, returnHomeGoal.getKey())) {
          Bukkit.getMobGoals().addGoal(mob, 1, returnHomeGoal);
        }
      }
      case "strife:follow_master" -> {
        AttackEntityGoal goalAttack = new AttackEntityGoal(mob, sound, attackRage, aggressive, flying);
        if (!Bukkit.getMobGoals().hasGoal(mob, goalAttack.getKey())) {
          Bukkit.getMobGoals().addGoal(mob, 3, goalAttack);
        }
      }
      case "strife:float_behind_master" -> {
        FloatBehindGoal goal = new FloatBehindGoal(mob);
        if (!Bukkit.getMobGoals().hasGoal(mob, goal.getKey())) {
          Bukkit.getMobGoals().addGoal(mob, 3, goal);
        }
      }
      default -> Bukkit.getLogger().info("Custom goal " + id + " not found! Not adding...");
    }
  }

  public static void removeGoals(Mob mob, List<String> removeKeys) {
    Bukkit.getMobGoals().getAllGoals(mob).forEach(goal -> {
      String goalName = goal.getKey().getNamespacedKey().toString();
      if (removeKeys.contains(goalName)) {
        Bukkit.getMobGoals().removeGoal(mob, goal);
      }
    });
  }

  public static class AttackEntityGoal implements Goal<Mob> {

    private static final NamespacedKey generic_hostile_key = new NamespacedKey(
        StrifePlugin.getInstance(), "generic_attack_goal");

    @Getter
    private static final GoalKey<Mob> goalKey = GoalKey.of(Mob.class, generic_hostile_key);
    private final Mob mob;
    private final boolean flying;
    private final boolean aggressive;
    private final double range;
    private long cooldownAttack = 0;
    private long cooldownTarget = 0;
    private final Sound attackSound;
    private final Random random = new Random(System.currentTimeMillis());

    public AttackEntityGoal(Mob mob, Sound attackSound, double range, boolean aggressive,
        boolean flying) {
      this.mob = mob;
      this.mob.getPathfinder().setCanPassDoors(true);
      this.mob.getPathfinder().setCanOpenDoors(true);
      this.mob.getPathfinder().setCanFloat(true);
      this.attackSound = attackSound;
      this.range = range;
      this.flying = flying;
      this.aggressive = aggressive;
    }

    @Override
    public boolean shouldActivate() {
      if (mob.getTarget() != null) {
        // TODO: actually use mob range
        if (mob.getTarget().getLocation().distanceSquared(mob.getLocation()) > 200) {
          mob.setTarget(null);
        }
      }
      if (aggressive && mob.getTarget() == null) {
        mob.setTarget(getClosestTarget());
      }
      return mob.getTarget() != null;
    }

    @Override
    public boolean shouldStayActive() {
      return shouldActivate();
    }

    @Override
    public void start() {
      this.cooldownAttack = 0;
    }

    @Override
    public void stop() {
      mob.getPathfinder().stopPathfinding();
      mob.setTarget(null);
      cooldownTarget = System.currentTimeMillis() + 300;
    }

    @Override
    public void tick() {
      if (mob.getTarget() == null) {
        return;
      }
      mob.lookAt(mob.getTarget());
      if (cooldownAttack < System.currentTimeMillis() && mob.getLocation()
          .distanceSquared(mob.getTarget().getLocation()) < range) {
        cooldownAttack = System.currentTimeMillis() + 300;
        attack(mob.getTarget());
        mob.getPathfinder().stopPathfinding();
      } else {
        if (flying) {
          mob.getPathfinder().moveTo(mob.getTarget().getEyeLocation(), 1.0D);
        } else {
          mob.getPathfinder().moveTo(mob.getTarget(), 1.0D);
        }
      }
    }

    private void attack(LivingEntity entity) {
      if (!mob.hasLineOfSight(entity)) {
        return;
      }
      mob.lookAt(entity);
      mob.getWorld().playSound(mob.getLocation(), attackSound, 1f, 0.75f + random.nextFloat() * 0.5f);
      mob.swingMainHand();
      mob.swingOffHand();
      double baseDamage = 8;
      entity.damage(baseDamage, mob);
    }

    @Override
    public GoalKey<Mob> getKey() {
      return goalKey;
    }

    @Override
    public @NotNull EnumSet<GoalType> getTypes() {
      return EnumSet.of(GoalType.LOOK, GoalType.MOVE);
    }

    private LivingEntity getClosestTarget() {
      if (cooldownTarget > System.currentTimeMillis()) {
        return null;
      }
      cooldownTarget = System.currentTimeMillis() + 800;
      double range = mob.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).getBaseValue();

      Collection<LivingEntity> nearbyTargets = mob.getWorld().getNearbyEntitiesByType(
          LivingEntity.class, mob.getLocation(), range, filterTargets(mob));

      double closestDistance = -1.0;
      LivingEntity closestTarget = null;
      for (LivingEntity target : nearbyTargets) {
        double distance = target.getLocation().distanceSquared(mob.getLocation());
        if (closestDistance != -1.0 && !(distance < closestDistance)) {
          continue;
        }
        closestDistance = distance;
        closestTarget = target;
      }

      if (closestTarget == null) {
        return null;
      }

      EntityTargetLivingEntityEvent ev = new EntityTargetLivingEntityEvent(mob,
          closestTarget, TargetReason.CLOSEST_PLAYER);

      Bukkit.getPluginManager().callEvent(ev);
      if (ev.isCancelled()) {
        return null;
      }
      return closestTarget;
    }

    private static Predicate<LivingEntity> filterTargets(Mob mob) {
      return entity -> entity != null && entity.isValid() &&
          mob != entity &&
          entity.getType() == EntityType.PLAYER &&
          ((Player) entity).getGameMode() != GameMode.SPECTATOR;
    }
  }
}
