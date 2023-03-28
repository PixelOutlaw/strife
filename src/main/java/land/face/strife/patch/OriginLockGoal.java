package land.face.strife.patch;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import java.util.EnumSet;
import land.face.strife.StrifePlugin;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Mob;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class OriginLockGoal implements Goal<Mob> {

  private static final NamespacedKey key = new NamespacedKey(
      StrifePlugin.getInstance(), "origin_lock");
  private static final Vector zero = new Vector(0, -0.05, 0);

  @Getter
  private static final GoalKey<Mob> goalKey = GoalKey.of(Mob.class, key);
  private final Mob mob;

  public OriginLockGoal(Mob mob) {
    this.mob = mob;
    this.mob.getPathfinder().setCanPassDoors(true);
    this.mob.getPathfinder().setCanOpenDoors(true);
    this.mob.getPathfinder().setCanFloat(true);
  }

  @Override
  public boolean shouldActivate() {
    return true;
  }

  @Override
  public boolean shouldStayActive() {
    return true;
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
    Location origin = mob.getOrigin();
    if (origin == null) {
      return;
    }
    Vector direction = mob.getLocation().getDirection();
    origin.setDirection(direction);
    mob.teleport(origin);
    mob.setVelocity(zero.clone());
  }

  @Override
  public @NotNull GoalKey<Mob> getKey() {
    return goalKey;
  }

  @Override
  public @NotNull EnumSet<GoalType> getTypes() {
    return EnumSet.of(GoalType.MOVE);
  }
}
