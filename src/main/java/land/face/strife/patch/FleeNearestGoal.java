package land.face.strife.patch;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import com.tealcube.minecraft.bukkit.facecore.utilities.MoveUtil;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import land.face.strife.StrifePlugin;
import land.face.strife.util.SpecialStatusUtil;
import land.face.strife.util.TargetingUtil;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class FleeNearestGoal implements Goal<Mob> {

  private static final NamespacedKey key = new NamespacedKey(StrifePlugin.getInstance(), "flee_nearest");

  @Getter
  private static final GoalKey<Mob> goalKey = GoalKey.of(Mob.class, key);
  private final WeakReference<Mob> mob;
  private WeakReference<LivingEntity> flightMob = new WeakReference<>(null);
  private long retryStamp = System.currentTimeMillis();

  public FleeNearestGoal(Mob mob) {
    this.mob = new WeakReference<>(mob);
    mob.getPathfinder().setCanPassDoors(true);
    mob.getPathfinder().setCanOpenDoors(true);
    mob.getPathfinder().setCanFloat(true);
  }

  @Override
  public boolean shouldActivate() {
    if (mob.get() == null) {
      return false;
    }
    if (retryStamp > System.currentTimeMillis()) {
      return false;
    }
    List<Player> nearby = new ArrayList<>(mob.get().getWorld()
        .getNearbyEntitiesByType(Player.class, mob.get().getLocation(), 5));
    nearby.removeIf(p -> !MoveUtil.hasMoved(p, 10000));
    if (nearby.isEmpty()) {
      retryStamp = System.currentTimeMillis() + 500;
      return false;
    }
    retryStamp = System.currentTimeMillis() + 100;
    TargetingUtil.DISTANCE_COMPARATOR.setLoc(mob.get().getLocation());
    nearby.sort(TargetingUtil.DISTANCE_COMPARATOR);
    Player player = nearby.get(0);
    SpecialStatusUtil.setHerdedBy(mob.get(), player);
    flightMob = new WeakReference<>(nearby.get(0));
    return true;
  }

  @Override
  public boolean shouldStayActive() {
    return shouldActivate();
  }

  @Override
  public void start() {
    // AAAAAAAAA
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
    if (flightMob.get() == null || !flightMob.get().getLocation().getWorld().equals(mob.getWorld())) {
      return;
    }
    Vector diff = mob.getLocation().toVector().subtract(flightMob.get().getLocation().toVector()).normalize().multiply(4);
    Location newLoc = mob.getLocation().clone().add(diff);
    mob.getPathfinder().moveTo(newLoc, 2.0);
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
