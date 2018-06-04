package info.faceland.strife.data;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class EntityStatCache {

  private final StrifePlugin plugin;
  private Map<UUID, AttributedEntity> trackedEntities;

  public EntityStatCache(StrifePlugin plugin) {
    this.plugin = plugin;
    this.trackedEntities = new HashMap<>();
  }

  public Map<UUID, AttributedEntity> getTrackedEntities() {
    return trackedEntities;
  }

  public AttributedEntity getAttributedEntity(LivingEntity entity) {
    if (!trackedEntities.containsKey(entity.getUniqueId())) {
      buildEntityStats(entity);
    }
    AttributedEntity attributedEntity = trackedEntities.get(entity.getUniqueId());
    plugin.getBarrierManager().createBarrierEntry(attributedEntity);
    return attributedEntity;
  }

  public void buildEntityStats(LivingEntity entity) {
    if (!trackedEntities.containsKey(entity.getUniqueId())) {
      AttributedEntity attributedEntity = new AttributedEntity(entity);
      attributedEntity.setAttributes(plugin.getMonsterManager().getBaseStats(entity));
      trackedEntities.put(entity.getUniqueId(), attributedEntity);
    }
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
