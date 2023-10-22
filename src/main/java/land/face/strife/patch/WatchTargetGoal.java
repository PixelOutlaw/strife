package land.face.strife.patch;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import java.lang.ref.WeakReference;
import java.util.EnumSet;
import land.face.strife.StrifePlugin;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

public class WatchTargetGoal implements Goal<Mob> {

  private static final NamespacedKey key = new NamespacedKey(
      StrifePlugin.getInstance(), "watch_target");

  @Getter
  private static final GoalKey<Mob> goalKey = GoalKey.of(Mob.class, key);
  private final WeakReference<Mob> mob;

  public WatchTargetGoal(Mob mob) {
    this.mob = new WeakReference<>(mob);
  }

  @Override
  public boolean shouldActivate() {
    Mob mob = this.mob.get();
    if (mob == null) {
      return false;
    }
    return mob.getTarget() != null;
  }

  @Override
  public boolean shouldStayActive() {
    return shouldActivate();
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
    Mob mob = this.mob.get();
    if (mob == null) {
      return;
    }
    if (mob.getTarget() == null || !mob.getTarget().isValid() || mob.getTarget().getWorld() != mob.getWorld()) {
      mob.setTarget(null);
      stop();
      return;
    }
    mob.lookAt(mob.getTarget());
  }

  @Override
  public @NotNull GoalKey<Mob> getKey() {
    return goalKey;
  }

  @Override
  public @NotNull EnumSet<GoalType> getTypes() {
    return EnumSet.of(GoalType.LOOK);
  }
}
