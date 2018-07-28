package info.faceland.strife.data;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public class Spawner {

  private final UniqueEntity uniqueEntity;
  private final Location location;
  private final double leashRange;
  private final long respawnSeconds;
  private final Chunk chunk;

  private LivingEntity trackedEntity;
  private long respawnTime;
  private int resetCount = 0;

  public Spawner(UniqueEntity uniqueEntity, Location location, int respawnSeconds,
      double leashRange) {
    this.uniqueEntity = uniqueEntity;
    this.location = location;
    this.respawnSeconds = respawnSeconds;
    this.leashRange = leashRange;

    this.chunk = location.getChunk();
    this.respawnTime = System.currentTimeMillis();
  }

  public UniqueEntity getUniqueEntity() {
    return uniqueEntity;
  }

  public Location getLocation() {
    return location;
  }

  public double getLeashRange() {
    return leashRange;
  }

  public long getRespawnMillis() {
    return respawnSeconds * 1000L;
  }

  public long getRespawnSeconds() {
    return respawnSeconds;
  }

  public Chunk getChunk() {
    return chunk;
  }

  public LivingEntity getTrackedEntity() {
    return trackedEntity;
  }

  public void setTrackedEntity(LivingEntity trackedEntity) {
    this.trackedEntity = trackedEntity;
  }

  public long getRespawnTime() {
    return respawnTime;
  }

  public void setRespawnTime(long respawnTime) {
    this.respawnTime = respawnTime;
  }

  public int getResetCount() {
    return resetCount;
  }

  public void setResetCount(int resetCount) {
    this.resetCount = resetCount;
  }
}
