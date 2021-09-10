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
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fish;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.jetbrains.annotations.NotNull;

public class FishPatcher {

  // in EntityAddToWorldEvent and EntitySpawnEvent i call this for change the rabbit Goals
  public static void patchFish(Fish fish) {
    fish.setSilent(true);
    AttackEntityGoal goalAttack = new AttackEntityGoal(fish);
    Bukkit.getMobGoals().getAllGoals(fish).forEach(goal -> {
      String goalName = goal.getKey().getNamespacedKey().toString();
      if (goalName.equals("minecraft:avoid_target") ||
          goalName.equals("minecraft:panic")) {
        Bukkit.getMobGoals().removeGoal(fish, goal);
      }
    });
    if (!Bukkit.getMobGoals().hasGoal(fish, goalAttack.getKey())) {
      Bukkit.getMobGoals().addGoal(fish, 3, goalAttack);
    } else {
      // If i use PlugMan for reload the Goal still alive but cant cast because "AttackEntityGoal != AttackEntityGoal" (Better restart the server)
      Goal<Fish> goal = Bukkit.getMobGoals().getGoal(fish, goalAttack.getKey());
      try {
        AttackEntityGoal goalRabbitLocal = (AttackEntityGoal) goal;
      } catch (ClassCastException ignored) {
        Bukkit.getMobGoals().addGoal(fish, 3, goalAttack);
      }
    }
  }

  public static class AttackEntityGoal implements Goal<Fish> {

    private static final NamespacedKey key_hostile_fish = new NamespacedKey(StrifePlugin.getInstance(), "hostile_fish");

    @Getter
    private static final GoalKey<Fish> goalKey = GoalKey.of(Fish.class, key_hostile_fish);
    private final Mob mob;
    private LivingEntity currentTarget;
    private LivingEntity currentAttacker = null;
    private int cooldownMove;
    private int cooldownAttack;
    private final Random random = new Random(System.currentTimeMillis());

    public AttackEntityGoal(Mob mob) {
      this.mob = mob;
      this.mob.getPathfinder().setCanPassDoors(true);
      this.mob.getPathfinder().setCanOpenDoors(true);
      this.mob.getPathfinder().setCanFloat(false);
    }

    @Override
    public boolean shouldActivate() {
      if (cooldownMove > 0) {
        --cooldownMove;
        return false;
      }

      // Check if currentAttacker is still on range
      if (currentAttacker != null) {
        double distanceFollowAttacker = 200;
        if (currentAttacker.getLocation().getBlock().getType() != Material.WATER) {
          currentAttacker = null;
        } else if (currentAttacker.getLocation().distanceSquared(mob.getLocation())
            > distanceFollowAttacker) {
          currentAttacker = null;
        }
      }

      // Current target is the attacker if on range, else is the closest entity
      this.currentTarget = this.currentAttacker != null ? currentAttacker : getClosestTarget();
      return currentTarget != null;
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
      cooldownMove = 100;
    }

    @Override
    public void tick() {
      mob.setTarget(currentTarget);
      if (mob.getLocation().distanceSquared(currentTarget.getLocation()) < 3.25) {
        this.cooldownAttack = Math.max(this.cooldownAttack - 1, 0);
        double distanceSquared = mob.getLocation().distanceSquared(currentTarget.getLocation());
        attack(currentTarget, distanceSquared);
        mob.getPathfinder().stopPathfinding();
      } else {
        mob.getPathfinder().moveTo(currentTarget, 2.0D);
      }
      mob.lookAt(currentTarget);
    }

    private void attack(LivingEntity entity, double squaredDistance) {
      double squaredMaxAttackDistance = this.getSquaredMaxAttackDistance(entity);
      if (squaredDistance <= squaredMaxAttackDistance && this.cooldownAttack <= 0) {
        List<Block> blocks = mob.getLineOfSight(null, 1);
        blocks.removeIf(block -> block.isEmpty() || block.isLiquid());
        if (!blocks.isEmpty()) {
          return;
        }
        mob.lookAt(entity);
        this.resetCooldownAttack();
        mob.getWorld().playSound(mob.getLocation(), Sound.BLOCK_BUBBLE_COLUMN_BUBBLE_POP, 5.0F,
            (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
        mob.swingMainHand();
        mob.swingOffHand();
        double baseDamage = 8;
        entity.damage(baseDamage, mob);
      }
    }

    @Override
    public GoalKey<Fish> getKey() {
      return goalKey;
    }

    @Override
    public @NotNull EnumSet<GoalType> getTypes() {
      return EnumSet.of(GoalType.LOOK, GoalType.MOVE);
    }

    protected void resetCooldownAttack() {
      this.cooldownAttack = 40;
    }

    private double getSquaredMaxAttackDistance(LivingEntity entity) {
      return 4 + entity.getWidth();
    }

    private LivingEntity getClosestTarget() {
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
          entity.getType() == EntityType.PLAYER &&
          entity.getLocation().getBlock().getType() == Material.WATER;
    }
  }
}
