package land.face.strife.patch;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import java.util.EnumSet;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;

public class FloatBehindGoalPatcher {

  public static class FloatBehindGoal implements Goal<Mob> {

    private static final NamespacedKey generic_hostile_key = new NamespacedKey(
        StrifePlugin.getInstance(), "float_behind_master");

    @Getter
    private static final GoalKey<Mob> goalKey = GoalKey.of(Mob.class, generic_hostile_key);
    private final Mob mob;

    private LivingEntity master;
    private long cooldown = 0;
    private long masterDetectCooldown = 0;

    public FloatBehindGoal(Mob mob) {
      this.mob = mob;
      this.mob.getPathfinder().setCanPassDoors(true);
      this.mob.getPathfinder().setCanOpenDoors(true);
      this.mob.getPathfinder().setCanFloat(true);
    }

    @Override
    public boolean shouldActivate() {
      if (master != null && master.isValid() && master.getWorld() == mob.getWorld()) {
        return true;
      }
      if (masterDetectCooldown > System.currentTimeMillis()) {
        return false;
      }
      masterDetectCooldown = System.currentTimeMillis() + 3000;
      StrifeMob masterMob = StrifePlugin.getInstance().getStrifeMobManager().getStatMob(mob);
      if (masterMob != null) {
        master = masterMob.getEntity();
        return true;
      }
      return false;
    }

    @Override
    public boolean shouldStayActive() {
      return shouldActivate();
    }

    @Override
    public void start() {
      this.cooldown = 0;
    }

    @Override
    public void stop() {}

    @Override
    public void tick() {
      if (cooldown > System.currentTimeMillis()) {
        return;
      }
      Location baseLocation = master.getEyeLocation().clone()
          .add(master.getEyeLocation().getDirection().multiply(-2));
      baseLocation.add(
          4 * (0.5 - StrifePlugin.RNG.nextDouble()),
          2 * (0.5 - StrifePlugin.RNG.nextDouble()),
          4 * (0.5 - StrifePlugin.RNG.nextDouble())
      );
      mob.getPathfinder().moveTo(baseLocation, 1.0D);
      cooldown = System.currentTimeMillis() + 1000;
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
