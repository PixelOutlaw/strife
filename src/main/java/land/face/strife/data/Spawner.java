package land.face.strife.data;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import land.face.strife.StrifePlugin;
import land.face.strife.util.LogUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class Spawner extends BukkitRunnable {

  public static int SPAWNER_OFFSET = 0;

  private final List<LivingEntity> entities = new CopyOnWriteArrayList<>();
  private final List<Long> respawnTimes = new CopyOnWriteArrayList<>();
  private final String id;
  private final String uniqueId;
  private final UniqueEntity uniqueEntity;
  private int amount;
  private Location location;
  private double leashRange;
  private long respawnSeconds;
  private long chunkKey;

  public Spawner(String id, UniqueEntity uniqueEntity, String uniqueId, int amount,
      Location location, int respawnSeconds, double leashRange) {
    this.id = id;
    this.uniqueId = uniqueId;
    this.uniqueEntity = uniqueEntity;
    this.amount = amount;
    this.location = location;
    this.respawnSeconds = respawnSeconds;
    this.leashRange = leashRange;
    chunkKey = location.getChunk().getChunkKey();

    runTaskTimer(StrifePlugin.getInstance(), SPAWNER_OFFSET, 20L);
    SPAWNER_OFFSET++;
    LogUtil.printDebug("Created Spawner with taskId " + getTaskId());
  }

  @Override
  public void run() {
    for (LivingEntity le : entities) {
      if (le == null || !le.isValid()) {
        entities.remove(le);
        continue;
      }
      double xDist = Math.abs(location.getX() - le.getLocation().getX());
      double zDist = Math.abs(location.getZ() - le.getLocation().getZ());
      if (Math.abs(xDist) + Math.abs(zDist) > leashRange) {
        despawnParticles(le);
        if (StrifePlugin.getInstance().getStrifeMobManager().isTrackedEntity(le)) {
          StrifePlugin.getInstance().getStrifeMobManager().removeStrifeMob(le);
        }
        le.remove();
        LogUtil.printDebug("Cancelled SpawnerTimer with id " + getTaskId() + " due to leash range");
      }
    }
  }

  public void addRespawnTimeIfApplicable(LivingEntity livingEntity) {
    if (entities.contains(livingEntity)) {
      entities.remove(livingEntity);
      respawnTimes.add(System.currentTimeMillis() + getRespawnMillis());
    }
  }

  private static void despawnParticles(LivingEntity livingEntity) {
    double height = livingEntity.getHeight() / 2;
    double width = livingEntity.getWidth() / 2;
    Location loc = livingEntity.getLocation().clone().add(0, height, 0);
    livingEntity.getWorld().spawnParticle(Particle.CLOUD, loc, (int) (20 * (height + width)), width,
        height, width, 0);
  }

  public String getId() {
    return id;
  }

  public String getUniqueId() {
    return uniqueId;
  }

  public UniqueEntity getUniqueEntity() {
    return uniqueEntity;
  }

  public int getAmount() {
    return amount;
  }

  public void setAmount(int amount) {
    this.amount = amount;
  }

  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
    chunkKey = location.getChunk().getChunkKey();
  }

  public double getLeashRange() {
    return leashRange;
  }

  public void setLeashRange(double leashRange) {
    this.leashRange = leashRange;
  }

  public long getRespawnMillis() {
    return respawnSeconds * 1000L;
  }

  public long getRespawnSeconds() {
    return respawnSeconds;
  }

  public void setRespawnSeconds(long respawnSeconds) {
    this.respawnSeconds = respawnSeconds;
  }

  public List<LivingEntity> getEntities() {
    return entities;
  }

  public void addEntity(LivingEntity trackedEntity) {
    entities.add(trackedEntity);
  }

  public List<Long> getRespawnTimes() {
    return respawnTimes;
  }

  public long getChunkKey() {
    return chunkKey;
  }
}
