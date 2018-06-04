package info.faceland.strife.managers;

import info.faceland.strife.data.Spawner;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.LivingEntity;

public class SpawnerManager {

  private final UniqueEntityManager uniqueManager;
  private final Map<String, Spawner> spawnerMap;

  private static final long RESPAWN_RETRY_DELAY = 15;

  public SpawnerManager(UniqueEntityManager uniqueManager) {
    this.uniqueManager = uniqueManager;
    this.spawnerMap = new HashMap<>();
  }

  public Map<String, Spawner> getSpawnerMap() {
    return spawnerMap;
  }

  public void setSpawnerMap(Map<String, Spawner> spawnerMap) {
    this.spawnerMap.clear();
    this.spawnerMap.putAll(spawnerMap);
  }

  public void addSpawner(String id, Spawner spawner) {
    this.spawnerMap.put(id, spawner);
  }

  public void leashSpawners() {
    for (Spawner spawner : spawnerMap.values()) {
      if (spawner.getTrackedEntity() == null || !spawner.getTrackedEntity().isValid()) {
        continue;
      }
      double distance = spawner.getLocation().distance(spawner.getTrackedEntity().getLocation());
      if (distance > spawner.getLeashRange()) {
        uniqueManager.removeEntity(spawner.getTrackedEntity(), true, false);
      }
    }
  }

  public void spawnSpawners() {
    for (Spawner s : spawnerMap.values()) {
      attemptSpawn(s);
    }
  }

  public void triggerRespawnCooldown(LivingEntity livingEntity) {
    for (Spawner spawner : spawnerMap.values()) {
      if (livingEntity == spawner.getTrackedEntity()) {
        spawner.setRespawnTime(System.currentTimeMillis() + spawner.getRespawnMillis());
        return;
      }
    }
  }

  private void attemptSpawn(Spawner spawner) {
    if (!spawner.getChunk().isLoaded()) {
      spawner.setRespawnTime(System.currentTimeMillis() + RESPAWN_RETRY_DELAY);
      return;
    }
    if (System.currentTimeMillis() < spawner.getRespawnTime()) {
      return;
    }
    if (spawner.getTrackedEntity() != null && spawner.getTrackedEntity().isValid()) {
      spawner.setRespawnTime(System.currentTimeMillis() + RESPAWN_RETRY_DELAY);
      return;
    }
    LivingEntity le = uniqueManager.spawnUnique(spawner.getUniqueEntity(), spawner.getLocation());
    spawner.setTrackedEntity(le);
  }
}
