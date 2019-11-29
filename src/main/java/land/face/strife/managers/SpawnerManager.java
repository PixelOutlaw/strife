package land.face.strife.managers;

import io.netty.util.internal.ConcurrentSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import land.face.strife.data.Spawner;
import land.face.strife.data.StrifeMob;
import land.face.strife.timers.SpawnerLeashTimer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class SpawnerManager {

  private final UniqueEntityManager uniqueManager;
  private final Map<String, Spawner> spawnerMap;
  private final Set<SpawnerLeashTimer> spawnerLeashTimers;

  public SpawnerManager(UniqueEntityManager uniqueManager) {
    this.uniqueManager = uniqueManager;
    spawnerMap = new HashMap<>();
    spawnerLeashTimers = new ConcurrentSet<>();
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

  public void removeLeashTimer(SpawnerLeashTimer timer) {
    spawnerLeashTimers.remove(timer);
  }

  public void addRespawnTime(LivingEntity livingEntity) {
    for (SpawnerLeashTimer s : spawnerLeashTimers) {
      if (s.getEntity() == livingEntity) {
        Spawner spawner = s.getSpawner();
        spawner.getRespawnTimes().add(System.currentTimeMillis() + spawner.getRespawnMillis());
        spawner.getEntities().remove(livingEntity);
        s.cancelAndClear();
      }
    }
  }

  public void spawnSpawners() {
    for (Spawner s : spawnerMap.values()) {
      if (s.getUniqueEntity() == null || s.getLocation() == null) {
        continue;
      }
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
      if (s.getRespawnTimes().size() + s.getEntities().size() >= maxMobs) {
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
      // Random displacement to prevent clumping
      mob.getEntity().setVelocity(new Vector(Math.random() * 0.2, 0.05, Math.random() * 0.2));
      spawnerLeashTimers.add(new SpawnerLeashTimer(s, mob.getEntity()));
    }
  }

  public void cancelAll() {
    for (SpawnerLeashTimer timer : spawnerLeashTimers) {
      timer.cancel();
    }
  }

  private boolean isChuckLoaded(Spawner spawner) {
    return spawner.getLocation().getWorld().isChunkInUse(spawner.getChunkX(), spawner.getChunkZ());
  }
}
