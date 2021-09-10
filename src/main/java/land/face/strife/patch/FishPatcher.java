package land.face.strife.patch;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import land.face.strife.StrifePlugin;
import land.face.strife.events.UniqueKillEvent;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

public class RabbitPatcher {

  // in EntityAddToWorldEvent and EntitySpawnEvent i call this for change the rabbit Goals
  public static void patchRabbit(Rabbit rabbit) {
    rabbit.setSilent(true);
    AttackEntityGoal goalAttack = new AttackEntityGoal(rabbit);
    // Remove vanilla goals in weaks rabbits
    Bukkit.getMobGoals().getAllGoals(rabbit).forEach(rabbitGoal -> {
      String nameGoalRabbit = rabbitGoal.getKey().getNamespacedKey().toString();
      if (nameGoalRabbit.contains("look_at_player") || nameGoalRabbit.contains(
          "rabbit_avoid_target") || nameGoalRabbit.contains("rabbit_panic")
          || nameGoalRabbit.contains("tempt") || nameGoalRabbit.contains("bred")) {
        Bukkit.getMobGoals().removeGoal(rabbit, rabbitGoal);
      }
    });
    if (!Bukkit.getMobGoals().hasGoal(rabbit, goalAttack.getKey())) {
      Bukkit.getMobGoals().addGoal(rabbit, 3, goalAttack);
    } else {
      // If i use PlugMan for reload the Goal still alive but cant cast because "AttackEntityGoal != AttackEntityGoal" (Better restart the server)
      Goal<Rabbit> goalRabbit = Bukkit.getMobGoals().getGoal(rabbit, goalAttack.getKey());
      try {
        AttackEntityGoal goalRabbitLocal = (AttackEntityGoal) goalRabbit;
      } catch (ClassCastException ignored) {
        Bukkit.getMobGoals().addGoal(rabbit, 3, goalAttack);
      }
    }
  }

  public static class AttackEntityGoal implements Goal<Rabbit> {

    private static final NamespacedKey key_hostile_rabbits = new NamespacedKey(StrifePlugin.getInstance(), "hostile_rabbit");

    @Getter
    private static final GoalKey<Rabbit> goalKey = GoalKey.of(Rabbit.class, key_hostile_rabbits);
    private final Predicate<LivingEntity> targetPredicate;
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
      this.mob.getPathfinder().setCanFloat(true);
      this.targetPredicate = generatePredicateForTarget();
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
        if (currentAttacker.getLocation().distanceSquared(mob.getLocation())
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
        mob.getPathfinder().moveTo(currentTarget, 1.0D);
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
        mob.getWorld().playSound(mob.getLocation(), org.bukkit.Sound.ENTITY_RABBIT_ATTACK, 5.0F,
            (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
        mob.swingMainHand();
        mob.swingOffHand();
        getRabbit().setJumping(true);
        double baseDamage = 8;
        entity.damage(baseDamage, mob);
      }
    }

    private Rabbit getRabbit() {
      return ((Rabbit) this.mob);
    }

    @Override
    public GoalKey<Rabbit> getKey() {
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
          LivingEntity.class, mob.getLocation(), range, this.targetPredicate);
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

    private Predicate<LivingEntity> generatePredicateForTarget() {
      Predicate<Player> playerPredicate = player -> (player.getGameMode().equals(GameMode.SURVIVAL)
          || player.getGameMode().equals(GameMode.ADVENTURE));
      Predicate<EntityType> blackListTargets = entityType ->
          entityType.equals(EntityType.ARMOR_STAND) || entityType.equals(EntityType.BAT)
              || entityType.equals(EntityType.RABBIT) || entityType.equals(EntityType.ENDER_DRAGON);
      return entity -> !entity.getUniqueId().equals(this.mob.getUniqueId())
          && !entity.hasPotionEffect(PotionEffectType.INVISIBILITY) && !entity.isInvisible()
          && !entity.isInvulnerable() && !entity.isDead() && !blackListTargets.test(
          entity.getType()) && (!entity.getType().equals(EntityType.PLAYER) || playerPredicate.test(
          ((Player) entity))) && entity.isValid();
    }
  }
}
