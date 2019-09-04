package info.faceland.strife.managers;

import info.faceland.strife.data.Spawner;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.timers.SpawnerTimer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SpawnerManager {

  private final UniqueEntityManager uniqueManager;
  private final Map<String, Spawner> spawnerMap = new HashMap<>();
  private final Set<SpawnerTimer> spawnerTimers = new HashSet<>();

  private static final long RESPAWN_RETRY_DELAY = 5000;

  public SpawnerManager(UniqueEntityManager uniqueManager) {
    this.uniqueManager = uniqueManager;
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
      StrifeMob mob = uniqueManager.spawnUnique(s.getUniqueEntity(), s.getLocation());
      if (mob == null || mob.getEntity() == null || !mob.getEntity().isValid()) {
        return;
      }
      mob.setSpawner(s);
      mob.setDespawnOnUnload(true);
      s.setTrackedEntity(mob.getEntity());
      spawnerTimers.add(new SpawnerTimer(s));
    }
  }

  public void cancelAll() {
    for (SpawnerTimer timer : spawnerTimers) {
      timer.cancel();
    }
  }

  private boolean isChuckLoaded(Spawner spawner) {
    return spawner.getLocation().getWorld().isChunkInUse(spawner.getChunkX(), spawner.getChunkZ());
  }
}
