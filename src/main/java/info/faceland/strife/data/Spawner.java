package info.faceland.strife.data;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public class Spawner {

  private final UniqueEntity uniqueEntity;

  private LivingEntity trackedEntity;
  private Location location;
  private double leashRange;
  private long respawnSeconds;
  private long respawnTime;
  private int resetCount = 0;
  private int chunkX;
  private int chunkZ;

  public Spawner(UniqueEntity uniqueEntity, Location location, int respawnSeconds,
      double leashRange) {
    this.uniqueEntity = uniqueEntity;
    this.location = location;
    this.respawnSeconds = respawnSeconds;
    this.leashRange = leashRange;
    this.respawnTime = System.currentTimeMillis();

    Chunk chunk = location.getChunk();
    this.chunkX = chunk.getX();
    this.chunkZ = chunk.getZ();
  }

  public UniqueEntity getUniqueEntity() {
    return uniqueEntity;
  }

  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
    Chunk chunk = location.getChunk();
    this.chunkX = chunk.getX();
    this.chunkZ = chunk.getZ();
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

  public int getChunkX() {
    return chunkX;
  }

  public int getChunkZ() {
    return chunkZ;
  }
}
