package land.face.strife.patch;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Predicate;
import land.face.strife.StrifePlugin;
import land.face.strife.util.SpecialStatusUtil;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

public class TargetEnemyFaction implements Goal<Mob> {

  private static final NamespacedKey key = new NamespacedKey(StrifePlugin.getInstance(), "target_factions");

  @Getter
  private static final GoalKey<Mob> goalKey = GoalKey.of(Mob.class, key);
  private final WeakReference<Mob> mob;
  private final Set<String> uniques;
  private long retargetTimestamp = 0;

  public TargetEnemyFaction(Mob mob, Set<String> uniques) {
    this.mob = new WeakReference<>(mob);
    mob.getPathfinder().setCanPassDoors(true);
    mob.getPathfinder().setCanOpenDoors(true);
    mob.getPathfinder().setCanFloat(true);
    this.uniques = uniques;
  }

  @Override
  public boolean shouldActivate() {
    Mob mob = this.mob.get();
    if (mob == null) {
      return false;
    }
    if (mob.getTarget() == null && retargetTimestamp < System.currentTimeMillis()) {
      LivingEntity target = getClosestTarget();
      if (target == null) {
        return false;
      }
      mob.setTarget(target);
      return true;
    }
    return false;
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
  public @NotNull GoalKey<Mob> getKey() {
    return goalKey;
  }

  @Override
  public @NotNull EnumSet<GoalType> getTypes() {
    return EnumSet.of(GoalType.LOOK, GoalType.MOVE);
  }

  private LivingEntity getClosestTarget() {
    Mob mob = this.mob.get();
    if (mob == null) {
      return null;
    }
    retargetTimestamp = System.currentTimeMillis() + 850;
    double range = mob.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).getValue();

    Collection<LivingEntity> nearbyTargets = mob.getWorld().getNearbyEntitiesByType(
        LivingEntity.class, mob.getLocation(), range, filterTargets(mob, uniques));

    if (nearbyTargets.isEmpty()) {
      return null;
    }

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

    return closestTarget;
  }

  private static Predicate<LivingEntity> filterTargets(Mob ownerMob, Set<String> uniques) {
    return targetEntity -> targetEntity != null && targetEntity.isValid() &&
        ownerMob != targetEntity && SpecialStatusUtil.getUniqueId(targetEntity) != null &&
        uniques.contains(SpecialStatusUtil.getUniqueId(targetEntity));
  }
}
