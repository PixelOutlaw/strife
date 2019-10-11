package info.faceland.strife.managers;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.data.buff.Buff;
import info.faceland.strife.data.buff.LoadedBuff;
import info.faceland.strife.stats.StrifeStat;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class StrifeMobManager {

  private final StrifePlugin plugin;
  private final Map<UUID, StrifeMob> trackedEntities = new ConcurrentHashMap<>();

  public StrifeMobManager(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  public StrifeMob getStatMob(LivingEntity entity) {
    if (!trackedEntities.containsKey(entity.getUniqueId())) {
      StrifeMob strifeMob;
      if (entity instanceof Player) {
        strifeMob = new StrifeMob(plugin.getChampionManager().getChampion((Player) entity));
      } else {
        strifeMob = new StrifeMob(entity);
      }
      strifeMob.setStats(plugin.getMonsterManager().getBaseStats(entity));
      trackedEntities.put(entity.getUniqueId(), strifeMob);
    }
    StrifeMob strifeMob = trackedEntities.get(entity.getUniqueId());
    strifeMob.setLivingEntity(entity);
    plugin.getBarrierManager().createBarrierEntry(strifeMob);
    return strifeMob;
  }

  public void addBuff(UUID uuid, String buffId, double durationMultiplier) {
    addBuff(uuid, plugin.getBuffManager().getBuffFromId(buffId), durationMultiplier);
  }

  public void addBuff(UUID uuid, LoadedBuff loadedBuff, double durationMultiplier) {
    StrifeMob strifeMob = trackedEntities.get(uuid);
    Buff buff = plugin.getBuffManager().buildFromLoadedBuff(loadedBuff);
    strifeMob.addBuff(loadedBuff.getName(), buff, loadedBuff.getSeconds() * durationMultiplier);
  }

  public int removeInvalidEntities() {
    int initialSize = trackedEntities.size();
    for (UUID uuid : trackedEntities.keySet()) {
      LivingEntity le = trackedEntities.get(uuid).getEntity();
      if (le != null && le.isValid()) {
        continue;
      }
      trackedEntities.remove(uuid);
    }
    return initialSize - trackedEntities.size();
  }

  public StrifeMob setEntityStats(LivingEntity entity, Map<StrifeStat, Float> statMap) {
    StrifeMob strifeMob = getStatMob(entity);
    strifeMob.setStats(statMap);
    trackedEntities.put(entity.getUniqueId(), strifeMob);
    return strifeMob;
  }

  public void despawnAllTempEntities() {
    for (StrifeMob strifeMob : trackedEntities.values()) {
      if (strifeMob.getEntity().isValid() && strifeMob.isDespawnOnUnload()) {
        strifeMob.getEntity().remove();
      }
    }
  }

  public void doChunkDespawn(LivingEntity entity) {
    if (!isTrackedEntity(entity)) {
      return;
    }
    if (trackedEntities.get(entity.getUniqueId()).isDespawnOnUnload()) {
      entity.remove();
    }
  }

  public void removeEntity(LivingEntity entity) {
    trackedEntities.remove(entity.getUniqueId());
  }

  public void removeEntity(UUID uuid) {
    trackedEntities.remove(uuid);
  }

  public void doSpawnerDeath(UUID uuid) {
    if (trackedEntities.containsKey(uuid)) {
      trackedEntities.get(uuid).doSpawnerDeath();
    }
  }

  public boolean isTrackedEntity(Entity entity) {
    return trackedEntities.containsKey(entity.getUniqueId());
  }
}
