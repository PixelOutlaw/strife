package land.face.strife.patch;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import java.util.EnumSet;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import lombok.Getter;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

public class FollowMasterGoal implements Goal<Mob> {

  private static final NamespacedKey key = new NamespacedKey(
      StrifePlugin.getInstance(), "follow_master");

  @Getter
  private static final GoalKey<Mob> goalKey = GoalKey.of(Mob.class, key);
  private final Mob mob;
  private LivingEntity master;
  private long masterCheck = 0;
  private int teleportTicks = 0;

  public FollowMasterGoal(Mob mob) {
    this.mob = mob;
    this.mob.getPathfinder().setCanPassDoors(true);
    this.mob.getPathfinder().setCanOpenDoors(true);
    this.mob.getPathfinder().setCanFloat(true);
  }

  @Override
  public boolean shouldActivate() {
    if (masterCheck < System.currentTimeMillis()) {
      masterCheck = System.currentTimeMillis() + 1000;
      StrifeMob self = StrifePlugin.getInstance().getStrifeMobManager().getStatMob(mob);
      if (self.getMaster() == null) {
        return false;
      }
      master = self.getMaster().getEntity();
      if (!master.isValid() || master.getWorld() != mob.getWorld()) {
        return false;
      }
      return master.getLocation().distanceSquared(mob.getLocation()) > 64;
    }
    return false;
  }

  @Override
  public boolean shouldStayActive() {
    if (masterCheck > System.currentTimeMillis()) {
      return true;
    }
    masterCheck = System.currentTimeMillis() + 500;
    if (master.getWorld() != mob.getWorld()) {
      return false;
    }
    if (mob.getTarget() != null && mob.getTarget().isValid()) {
      teleportTicks = 0;
      return false;
    }
    teleportTicks++;
    if (teleportTicks > 8) {
      mob.teleport(master.getLocation());
      teleportTicks = 0;
      return false;
    }
    return master.getLocation().distanceSquared(mob.getLocation()) > 28;
  }

  @Override
  public void start() {
    // Nothing
  }

  @Override
  public void stop() {
    mob.getPathfinder().stopPathfinding();
  }

  @Override
  public void tick() {
    if (master == null || !master.isValid() || master.getWorld() != mob.getWorld()) {
      return;
    }
    mob.getPathfinder().moveTo(master.getLocation(), 1.35D);
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
