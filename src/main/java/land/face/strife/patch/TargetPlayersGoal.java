package land.face.strife.patch;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import java.util.Collection;
import java.util.EnumSet;
import java.util.function.Predicate;
import land.face.strife.StrifePlugin;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.jetbrains.annotations.NotNull;

public class TargetPlayersGoal implements Goal<Mob> {

  private static final NamespacedKey key = new NamespacedKey(
      StrifePlugin.getInstance(), "target_players");

  @Getter
  private static final GoalKey<Mob> goalKey = GoalKey.of(Mob.class, key);
  private final Mob mob;
  private long retargetTimestamp = 0;

  public TargetPlayersGoal(Mob mob) {
    this.mob = mob;
    this.mob.getPathfinder().setCanPassDoors(true);
    this.mob.getPathfinder().setCanOpenDoors(true);
    this.mob.getPathfinder().setCanFloat(true);
  }

  @Override
  public boolean shouldActivate() {
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
    retargetTimestamp = System.currentTimeMillis() + 800;
    double range = mob.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).getValue();

    Collection<LivingEntity> nearbyTargets = mob.getWorld().getNearbyEntitiesByType(
        LivingEntity.class, mob.getLocation(), range, filterTargets(mob));

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

    EntityTargetLivingEntityEvent ev = new EntityTargetLivingEntityEvent(mob,
        closestTarget, TargetReason.CLOSEST_PLAYER);

    Bukkit.getPluginManager().callEvent(ev);
    if (ev.isCancelled()) {
      return null;
    }
    return closestTarget;
  }

  private static Predicate<LivingEntity> filterTargets(Mob mob) {
    return targetEntity -> targetEntity != null && targetEntity.isValid() &&
        mob != targetEntity && targetEntity.getType() == EntityType.PLAYER &&
        ((Player) targetEntity).getGameMode() != GameMode.SPECTATOR;
  }
}
