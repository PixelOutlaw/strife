package land.face.strife.patch;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import java.lang.ref.WeakReference;
import java.util.EnumSet;
import land.face.strife.StrifePlugin;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

public class ChaseTargetGoal implements Goal<Mob> {

  private static final NamespacedKey key = new NamespacedKey(
      StrifePlugin.getInstance(), "chase_target");

  @Getter
  private static final GoalKey<Mob> goalKey = GoalKey.of(Mob.class, key);
  private final WeakReference<Mob> mob;
  private float followRange;
  private long retryStamp = System.currentTimeMillis();

  public ChaseTargetGoal(Mob mob) {
    this.mob = new WeakReference<>(mob);
    mob.getPathfinder().setCanPassDoors(true);
    mob.getPathfinder().setCanOpenDoors(true);
    mob.getPathfinder().setCanFloat(true);
  }

  @Override
  public boolean shouldActivate() {
    return mob.get() != null && mob.get().getTarget() != null;
  }

  @Override
  public boolean shouldStayActive() {
    return shouldActivate();
  }

  @Override
  public void start() {
    Mob mob = this.mob.get();
    if (mob == null) {
      return;
    }
    followRange = (float) mob.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).getValue();
    followRange *= followRange;
  }

  @Override
  public void stop() {
    Mob mob = this.mob.get();
    if (mob == null) {
      return;
    }
    mob.getPathfinder().stopPathfinding();
  }

  @Override
  public void tick() {
    Mob mob = this.mob.get();
    if (mob == null) {
      return;
    }
    if (retryStamp > System.currentTimeMillis()) {
      if (mob.getTarget().getWorld() == mob.getWorld()) {
        mob.lookAt(mob.getTarget());
      }
      return;
    }
    if (!mob.getTarget().isValid()) {
      mob.setTarget(null);
      return;
    }
    if (mob.getTarget().getWorld() != mob.getWorld()) {
      mob.setTarget(null);
      return;
    }
    if (mob.getTarget().getLocation().distanceSquared(mob.getLocation()) > followRange) {
      mob.setTarget(null);
      return;
    }
    retryStamp = System.currentTimeMillis() + 400;
    mob.lookAt(mob.getTarget());
    mob.getPathfinder().moveTo(mob.getTarget(), 1.0D);
  }

  @Override
  public @NotNull GoalKey<Mob> getKey() {
    return goalKey;
  }

  @Override
  public @NotNull EnumSet<GoalType> getTypes() {
    return EnumSet.of(GoalType.LOOK, GoalType.MOVE);
  }
}
