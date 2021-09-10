package land.face.strife.patch;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Random;
import java.util.function.Predicate;
import land.face.strife.StrifePlugin;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Cow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.jetbrains.annotations.NotNull;

public class CowPatcher {

  public static void patchCow(Cow cow) {
    AttackEntityGoal goalAttack = new AttackEntityGoal(cow, Sound.ENTITY_COW_STEP);
    Bukkit.getMobGoals().getAllGoals(cow).forEach(goal -> {
      String goalName = goal.getKey().getNamespacedKey().toString();
      if (goalName.equals("minecraft:look_at_player") ||
          goalName.equals("minecraft:panic") ||
          goalName.equals("minecraft:tempt") ||
          goalName.equals("minecraft:breed")) {
        Bukkit.getMobGoals().removeGoal(cow, goal);
      }
    });
    if (!Bukkit.getMobGoals().hasGoal(cow, goalAttack.getKey())) {
      Bukkit.getMobGoals().addGoal(cow, 3, goalAttack);
    }
  }

  public static class AttackEntityGoal implements Goal<Mob> {

    private static final NamespacedKey generic_hostile_key = new NamespacedKey(
        StrifePlugin.getInstance(), "hostile_cow");

    @Getter
    private static final GoalKey<Mob> goalKey = GoalKey.of(Mob.class, generic_hostile_key);
    private final Mob mob;
    private long cooldownAttack = 0;
    private long cooldownTarget = 0;
    private final Sound attackSound;
    private final Random random = new Random(System.currentTimeMillis());

    public AttackEntityGoal(Mob mob, Sound attackSound) {
      this.mob = mob;
      this.mob.getPathfinder().setCanPassDoors(true);
      this.mob.getPathfinder().setCanOpenDoors(true);
      this.mob.getPathfinder().setCanFloat(true);
      this.attackSound = attackSound;
    }

    @Override
    public boolean shouldActivate() {
      if (mob.getTarget() != null) {
        // TODO: actually use mob range
        if (mob.getTarget().getLocation().distanceSquared(mob.getLocation()) > 200) {
          mob.setTarget(null);
        }
      }
      if (mob.getTarget() == null) {
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
      if (cooldownAttack < System.currentTimeMillis() && mob.getLocation()
          .distanceSquared(mob.getTarget().getLocation()) < 3.25) {
        cooldownAttack = System.currentTimeMillis() + 300;
        attack(mob.getTarget());
        mob.getPathfinder().stopPathfinding();
      } else {
        mob.getPathfinder().moveTo(mob.getTarget(), 1.0D);
      }
    }

    private void attack(LivingEntity entity) {
      if (!mob.hasLineOfSight(entity)) {
        return;
      }
      mob.lookAt(entity);
      mob.getWorld().playSound(mob.getLocation(), attackSound, 3.0f, 0.75f + random.nextFloat() * 0.5f);
      mob.swingMainHand();
      mob.swingOffHand();
      mob.setJumping(true);
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
      cooldownTarget = System.currentTimeMillis() + 300;
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
      return entity -> entity != null && entity.isValid() && mob != entity &&
          entity.getType() == EntityType.PLAYER;
    }
  }
}
