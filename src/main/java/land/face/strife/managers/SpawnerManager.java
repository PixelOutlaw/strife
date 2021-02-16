package land.face.strife.managers;

import java.util.HashMap;
import java.util.Map;
import land.face.strife.StrifePlugin;
import land.face.strife.data.Spawner;
import org.bukkit.entity.LivingEntity;

public class SpawnerManager {

  private final StrifePlugin plugin;
  private final Map<String, Spawner> spawnerMap = new HashMap<>();

  public SpawnerManager(StrifePlugin plugin) {
    this.plugin = plugin;
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

  public void addRespawnTime(LivingEntity livingEntity) {
    for (Spawner spawner : spawnerMap.values()) {
      spawner.addRespawnTimeIfApplicable(livingEntity);
    }
  }

  public void cancelAll() {
    for (Spawner spawner : spawnerMap.values()) {
      spawner.cancel();
    }
  }
}
