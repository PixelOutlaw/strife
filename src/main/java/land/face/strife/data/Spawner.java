package land.face.strife.data;

import io.netty.util.internal.ConcurrentSet;
import java.util.Set;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public class Spawner {

  private Set<LivingEntity> entities = new ConcurrentSet<>();
  private Set<Long> respawnTimes = new ConcurrentSet<>();

  private final String uniqueId;
  private final UniqueEntity uniqueEntity;
  private int amount;
  private Location location;
  private double leashRange;
  private long respawnSeconds;
  private int chunkX;
  private int chunkZ;

  public Spawner(UniqueEntity uniqueEntity, String uniqueId, int amount, Location location,
      int respawnSeconds, double leashRange) {
    this.uniqueId = uniqueId;
    this.uniqueEntity = uniqueEntity;
    this.amount = amount;
    this.location = location;
    this.respawnSeconds = respawnSeconds;
    this.leashRange = leashRange;

    Chunk chunk = location.getChunk();
    this.chunkX = chunk.getX();
    this.chunkZ = chunk.getZ();
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

  public Set<LivingEntity> getEntities() {
    return entities;
  }

  public void addEntity(LivingEntity trackedEntity) {
    entities.add(trackedEntity);
  }

  public Set<Long> getRespawnTimes() {
    return respawnTimes;
  }

  public int getChunkX() {
    return chunkX;
  }

  public int getChunkZ() {
    return chunkZ;
  }

  public void doDeath(LivingEntity livingEntity) {
    if (livingEntity != null && livingEntity.isValid()) {
      entities.remove(livingEntity);
      livingEntity.remove();
    }
    respawnTimes.add(System.currentTimeMillis() + respawnSeconds * 1000);
  }
}
