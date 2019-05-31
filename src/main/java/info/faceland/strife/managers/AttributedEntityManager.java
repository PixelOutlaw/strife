package info.faceland.strife.managers;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.AttributedEntity;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class AttributedEntityManager {

  private final StrifePlugin plugin;
  private Map<UUID, AttributedEntity> trackedEntities;

  public AttributedEntityManager(StrifePlugin plugin) {
    this.plugin = plugin;
    this.trackedEntities = new ConcurrentHashMap<>();
  }

  public Map<UUID, AttributedEntity> getTrackedEntities() {
    return trackedEntities;
  }

  public AttributedEntity getAttributedEntity(LivingEntity entity) {
    if (!trackedEntities.containsKey(entity.getUniqueId())) {
      AttributedEntity attributedEntity;
      if (entity instanceof Player) {
        attributedEntity = new AttributedEntity(
            plugin.getChampionManager().getChampion((Player) entity));
      } else {
        attributedEntity = new AttributedEntity(entity);
      }
      attributedEntity.setAttributes(plugin.getMonsterManager().getBaseStats(entity));
      trackedEntities.put(entity.getUniqueId(), attributedEntity);
    }
    AttributedEntity attributedEntity = trackedEntities.get(entity.getUniqueId());
    attributedEntity.setLivingEntity(entity);
    plugin.getBarrierManager().createBarrierEntry(attributedEntity);
    return attributedEntity;
  }

  public void setEntityStats(LivingEntity entity, Map<StrifeAttribute, Double> statMap) {
    AttributedEntity attributedEntity = getAttributedEntity(entity);
    attributedEntity.setAttributes(statMap);
    trackedEntities.put(entity.getUniqueId(), attributedEntity);
  }

  public void removeEntity(LivingEntity entity) {
    trackedEntities.remove(entity.getUniqueId());
  }

  public void removeEntity(UUID uuid) {
    trackedEntities.remove(uuid);
  }

  public boolean isValid(UUID uuid) {
    if (trackedEntities.containsKey(uuid)) {
      Entity entity = Bukkit.getEntity(uuid);
      if (entity == null || !entity.isValid()) {
        return false;
      }
    }
    return true;
  }
}
