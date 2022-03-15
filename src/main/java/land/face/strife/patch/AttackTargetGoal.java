package land.face.strife.patch;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import java.util.EnumSet;
import java.util.Random;
import land.face.strife.StrifePlugin;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

public class AttackTargetGoal implements Goal<Mob> {

  private static final NamespacedKey key = new NamespacedKey(
      StrifePlugin.getInstance(), "attack_target");

  @Getter
  private static final GoalKey<Mob> goalKey = GoalKey.of(Mob.class, key);
  private final Mob mob;
  private final float attackRange;
  private long attackTimestamp = 0;

  public AttackTargetGoal(Mob mob, float attackRange) {
    this.mob = mob;
    this.mob.getPathfinder().setCanPassDoors(true);
    this.mob.getPathfinder().setCanOpenDoors(true);
    this.mob.getPathfinder().setCanFloat(true);
    this.attackRange = (float) Math.pow(attackRange, 2f);
  }

  @Override
  public boolean shouldActivate() {
    if (mob.getTarget() == null || attackTimestamp > System.currentTimeMillis()) {
      return false;
    }
    if (mob.getTarget().getLocation().distanceSquared(mob.getLocation()) > attackRange) {
      return false;
    }
    if (!mob.hasLineOfSight(mob.getTarget())) {
      return false;
    }
    attack(mob.getTarget());
    return true;
  }

  private void attack(LivingEntity entity) {
    attackTimestamp = System.currentTimeMillis() + 900;
    mob.lookAt(entity);
    mob.swingMainHand();
    mob.swingOffHand();
    double baseDamage = 8;
    entity.damage(baseDamage, mob);
  }

  @Override
  public boolean shouldStayActive() {
    return false;
  }

  @Override
  public void start() {
    // Nothing
  }

  @Override
  public void stop() {
    // Nothing
  }

  @Override
  public void tick() {
    // Nothing
  }

  @Override
  public GoalKey<Mob> getKey() {
    return goalKey;
  }

  @Override
  public @NotNull EnumSet<GoalType> getTypes() {
    return EnumSet.of(GoalType.LOOK, GoalType.TARGET);
  }
}
