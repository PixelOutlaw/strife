package land.face.strife.patch;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import java.util.EnumSet;
import land.face.strife.StrifePlugin;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

public class ReturnHomePatcher {

  public static class ReturnHomeGoal implements Goal<Mob> {

    private static final NamespacedKey RETURN_HOME = new NamespacedKey(
        StrifePlugin.getInstance(), "return_home");

    @Getter
    private static final GoalKey<Mob> goalKey = GoalKey.of(Mob.class, RETURN_HOME);
    private final Mob mob;
    private final double range;
    private boolean returning;
    private long checkCooldown = 0;

    public ReturnHomeGoal(Mob mob, double dist) {
      this.mob = mob;
      this.mob.getPathfinder().setCanPassDoors(true);
      this.mob.getPathfinder().setCanOpenDoors(true);
      this.mob.getPathfinder().setCanFloat(true);
      this.range = dist;
    }

    @Override
    public boolean shouldActivate() {
      if (mob.getTarget() != null) {
        return false;
      }
      if (returning && checkCooldown > System.currentTimeMillis()) {
        return true;
      }
      boolean outOfRange = mob.getLocation().distanceSquared(mob.getOrigin()) > range;
      if (outOfRange) {
        checkCooldown = System.currentTimeMillis() + 2000;
      } else {
        checkCooldown = System.currentTimeMillis() + 600;
      }
      returning = outOfRange;
      return outOfRange;
    }

    @Override
    public boolean shouldStayActive() {
      return shouldActivate();
    }

    @Override
    public void start() {
      this.checkCooldown = 0;
    }

    @Override
    public void stop() {
      mob.getPathfinder().stopPathfinding();
    }

    @Override
    public void tick() {
      if (mob.getTarget() != null) {
        return;
      }
      mob.getPathfinder().moveTo(mob.getOrigin(), 1.0D);
    }

    @Override
    public GoalKey<Mob> getKey() {
      return goalKey;
    }

    @Override
    public @NotNull EnumSet<GoalType> getTypes() {
      return EnumSet.of(GoalType.LOOK, GoalType.MOVE);
    }
  }
}
