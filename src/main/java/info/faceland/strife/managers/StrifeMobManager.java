package info.faceland.strife.managers;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.StrifeMob;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class StrifeMobManager {

  private final StrifePlugin plugin;
  private Map<UUID, StrifeMob> trackedEntities;

  public StrifeMobManager(StrifePlugin plugin) {
    this.plugin = plugin;
    this.trackedEntities = new ConcurrentHashMap<>();
  }

  public Map<UUID, StrifeMob> getTrackedEntities() {
    return trackedEntities;
  }

  public StrifeMob getAttributedEntity(LivingEntity entity) {
    if (!trackedEntities.containsKey(entity.getUniqueId())) {
      StrifeMob strifeMob;
      if (entity instanceof Player) {
        strifeMob = new StrifeMob(
            plugin.getChampionManager().getChampion((Player) entity));
      } else {
        strifeMob = new StrifeMob(entity);
      }
      strifeMob.setAttributes(plugin.getMonsterManager().getBaseStats(entity));
      trackedEntities.put(entity.getUniqueId(), strifeMob);
    }
    StrifeMob strifeMob = trackedEntities.get(entity.getUniqueId());
    strifeMob.setLivingEntity(entity);
    plugin.getBarrierManager().createBarrierEntry(strifeMob);
    return strifeMob;
  }

  public void setEntityStats(LivingEntity entity, Map<StrifeAttribute, Double> statMap) {
    StrifeMob strifeMob = getAttributedEntity(entity);
    strifeMob.setAttributes(statMap);
    trackedEntities.put(entity.getUniqueId(), strifeMob);
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

  public boolean isTrackedEntity(Entity entity) {
    return isTrackedEntity(entity.getUniqueId());
  }

  public boolean isTrackedEntity(UUID uuid) {
    return trackedEntities.containsKey(uuid);
  }

  public LivingEntity getLivingEntity(UUID uuid) {
    if (!isTrackedEntity(uuid)) {
      return null;
    }
    return trackedEntities.get(uuid).getEntity();
  }
}
