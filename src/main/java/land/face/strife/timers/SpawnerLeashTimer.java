package land.face.strife.timers;

import land.face.strife.StrifePlugin;
import land.face.strife.data.Spawner;
import land.face.strife.util.LogUtil;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class SpawnerLeashTimer extends BukkitRunnable {

  private final Spawner spawner;
  private final LivingEntity entity;

  public SpawnerLeashTimer(Spawner spawner, LivingEntity livingEntity) {
    this.spawner = spawner;
    this.entity = livingEntity;
    runTaskTimer(StrifePlugin.getInstance(), 0L, 20L);
    LogUtil.printDebug("Created SpawnerTimer with id " + getTaskId());
  }

  @Override
  public void run() {
    if (entity == null || !entity.isValid()) {
      LogUtil.printDebug("Cancelled SpawnerTimer with id " + getTaskId() + " due to null entity");
      cancelAndClear();
      return;
    }
    // Lazy fuzzy distance checking, leash distance has no reason
    // to be truly exact
    double xDist = Math.abs(spawner.getLocation().getX() - entity.getLocation().getX());
    double zDist = Math.abs(spawner.getLocation().getZ() - entity.getLocation().getZ());
    if (Math.abs(xDist) + Math.abs(zDist) > spawner.getLeashRange()) {
      double offset = entity.getEyeHeight() * 0.5;
      entity.getWorld().spawnParticle(Particle.CLOUD,
          entity.getLocation().clone().add(0, offset, 0), 20, 0.5, offset, 0.5, 0);
      LogUtil.printDebug("Cancelled SpawnerTimer with id " + getTaskId() + " due to leash range");
      cancelAndClear();
    }
  }

  public Spawner getSpawner() {
    return spawner;
  }

  public LivingEntity getEntity() {
    return entity;
  }

  public void cancelAndClear() {
    if (entity != null && entity.isValid()) {
      entity.remove();
    }
    StrifePlugin.getInstance().getSpawnerManager().removeLeashTimer(this);
    cancel();
  }
}
