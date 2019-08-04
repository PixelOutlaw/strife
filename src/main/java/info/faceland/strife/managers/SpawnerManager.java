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

  public void removeSpawner(String id) {
    this.spawnerMap.remove(id);
  }

  public void leashSpawners() {
    for (Spawner spawner : spawnerMap.values()) {
      if (spawner.getTrackedEntity() == null || !spawner.getTrackedEntity().isValid()) {
        continue;
      }
      double distance = spawner.getLocation().distance(spawner.getTrackedEntity().getLocation());
      if (distance > spawner.getLeashRange()) {
        spawner.getTrackedEntity().remove();
      }
    }
  }

  public void spawnSpawners() {
    for (Spawner s : spawnerMap.values()) {
      if (System.currentTimeMillis() < s.getRespawnTime()) {
        continue;
      }
      if (!isChuckLoaded(s)) {
        s.setRespawnTime(System.currentTimeMillis() + RESPAWN_RETRY_DELAY);
        continue;
      }
      if (s.getTrackedEntity() != null && s.getTrackedEntity().isValid()) {
        s.setRespawnTime(System.currentTimeMillis() + RESPAWN_RETRY_DELAY);
        continue;
      }
      LivingEntity le = uniqueManager.spawnUnique(s.getUniqueEntity(), s.getLocation());
      if (le == null) {
        return;
      }
      s.setTrackedEntity(le);
    }
  }

  private boolean isChuckLoaded(Spawner spawner) {
    return spawner.getLocation().getWorld().isChunkInUse(spawner.getChunkX(), spawner.getChunkZ());
  }
}
