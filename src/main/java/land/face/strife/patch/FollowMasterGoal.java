package land.face.strife.patch;

import com.destroystokyo.paper.entity.ai.Goal;
import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import java.lang.ref.WeakReference;
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
  private final WeakReference<Mob> mob;
  private WeakReference<LivingEntity> master = new WeakReference<>(null);
  private long masterCheck = 0;
  private int teleportTicks = 0;

  public FollowMasterGoal(Mob mob) {
    this.mob = new WeakReference<>(mob);
    mob.getPathfinder().setCanPassDoors(true);
    mob.getPathfinder().setCanOpenDoors(true);
    mob.getPathfinder().setCanFloat(true);
  }

  @Override
  public boolean shouldActivate() {
    Mob selfMob = this.mob.get();
    LivingEntity masterEntity = this.master.get();
    if (selfMob == null) {
      //Bukkit.getLogger().info("[Strife][AI][fllow_master] Existed due to null mob");
      return false;
    }
    if (masterCheck < System.currentTimeMillis()) {
      masterCheck = System.currentTimeMillis() + 1000;
      StrifeMob self = StrifePlugin.getInstance().getStrifeMobManager().getStatMob(selfMob);
      if (self.getMaster() == null) {
        selfMob.remove();
        //Bukkit.getLogger().info("[Strife][AI][follow_master] Existed due to not strifemob master");
        return false;
      }
      if (masterEntity != self.getMaster().getEntity()) {
        this.master = new WeakReference<>(self.getMaster().getEntity());
        masterEntity = self.getMaster().getEntity();
      }
    }
    if (masterEntity == null || !masterEntity.isValid()) {
      //Bukkit.getLogger().info("[Strife][AI][fllow_master] Existed due to invalid master entity");
      return false;
    }
    if (masterEntity.getWorld() != selfMob.getWorld()) {
      if (!"Graveyard".equals(selfMob.getWorld().getName())) {
        selfMob.remove();
      }
      //Bukkit.getLogger().info("[Strife][AI][fllow_master] Existed due to differing world");
      return false;
    }
    if (selfMob.getTarget() == null) {
      return masterEntity.getLocation().distanceSquared(selfMob.getLocation()) > 64;
    }
    if (masterEntity.getLocation().distanceSquared(selfMob.getLocation()) > 324) {
      selfMob.setTarget(null);
      return true;
    }
    return false;
  }

  @Override
  public boolean shouldStayActive() {
    if (masterCheck > System.currentTimeMillis()) {
      return true;
    }
    Mob selfMob = this.mob.get();
    if (selfMob == null) {
      //Bukkit.getLogger().info("[Strife][AI][fllow_master] Existed due to missing selfmob");
      return false;
    }
    LivingEntity master = this.master.get();
    if (master == null) {
      //Bukkit.getLogger().info("[Strife][AI][fllow_master] Existed due to null master");
      return false;
    }
    masterCheck = System.currentTimeMillis() + 500;
    if (master.getWorld() != selfMob.getWorld()) {
      //Bukkit.getLogger().info("[Strife][AI][fllow_master] Existed due to differing worls2");
      return false;
    }
    if (selfMob.getTarget() != null && selfMob.getTarget().isValid()) {
      teleportTicks = 0;
      //Bukkit.getLogger().info("[Strife][AI][fllow_master] Teleport ticks due to target found");
      return false;
    }
    teleportTicks++;
    if (teleportTicks > 12) {
      selfMob.teleport(master.getLocation());
      teleportTicks = 0;
      //Bukkit.getLogger().info("[Strife][AI][fllow_master] Teleport ticks due to teleport success");
      return false;
    }
    return master.getLocation().distanceSquared(selfMob.getLocation()) > 28;
  }

  @Override
  public void start() {
    // Nothing
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
      //Bukkit.getLogger().info("[Strife][AI][fllow_master] Mob tick, do nothing, null mob");
      return;
    }
    LivingEntity master = this.master.get();
    if (master == null || !master.isValid() || master.getWorld() != mob.getWorld()) {
      //Bukkit.getLogger().info("[Strife][AI][fllow_master] Mob tick, null invalid or wrong world master");
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
