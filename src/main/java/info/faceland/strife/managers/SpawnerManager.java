package info.faceland.strife.managers;

import info.faceland.strife.data.Spawner;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.timers.SpawnerTimer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.entity.LivingEntity;

public class SpawnerManager {

  private final UniqueEntityManager uniqueManager;
  private final Map<String, Spawner> spawnerMap = new HashMap<>();
  private final Set<SpawnerTimer> spawnerTimers = new HashSet<>();

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
      int maxMobs = s.getAmount();
      for (long stamp : s.getRespawnTimes()) {
        if (System.currentTimeMillis() > stamp) {
          s.getRespawnTimes().remove(stamp);
        }
      }
      for (LivingEntity livingEntity : s.getEntities()) {
        if (livingEntity == null || !livingEntity.isValid()) {
          s.getEntities().remove(livingEntity);
        }
      }
      if (s.getRespawnTimes().size() >= maxMobs || s.getEntities().size() >= maxMobs) {
        continue;
      }
      if (!isChuckLoaded(s)) {
        continue;
      }
      StrifeMob mob = uniqueManager.spawnUnique(s.getUniqueEntity(), s.getLocation());
      if (mob == null || mob.getEntity() == null || !mob.getEntity().isValid()) {
        continue;
      }
      mob.setSpawner(s);
      mob.setDespawnOnUnload(true);
      s.addEntity(mob.getEntity());
      spawnerTimers.add(new SpawnerTimer(s, mob.getEntity()));
      s.getRespawnTimes().add(System.currentTimeMillis() + s.getRespawnMillis());
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
