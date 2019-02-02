package info.faceland.strife.data;

import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.managers.BarrierManager;
import info.faceland.strife.managers.ChampionManager;
import info.faceland.strife.managers.MonsterManager;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class EntityStatCache {

  private final ChampionManager championManager;
  private final BarrierManager barrierManager;
  private final MonsterManager monsterManager;
  private Map<UUID, AttributedEntity> trackedEntities;

  public EntityStatCache(ChampionManager championManager, BarrierManager barrierManager,
      MonsterManager monsterManager) {
    this.championManager = championManager;
    this.barrierManager = barrierManager;
    this.monsterManager = monsterManager;
    this.trackedEntities = new HashMap<>();
  }

  public Map<UUID, AttributedEntity> getTrackedEntities() {
    return trackedEntities;
  }

  public AttributedEntity getAttributedEntity(LivingEntity entity) {
    if (!trackedEntities.containsKey(entity.getUniqueId())) {
      AttributedEntity attributedEntity;
      if (entity instanceof Player) {
        attributedEntity = new AttributedEntity(championManager.getChampion((Player) entity));
      } else {
        attributedEntity = new AttributedEntity(entity);
      }
      attributedEntity.setAttributes(monsterManager.getBaseStats(entity));
      trackedEntities.put(entity.getUniqueId(), attributedEntity);
    }
    AttributedEntity attributedEntity = trackedEntities.get(entity.getUniqueId());
    barrierManager.createBarrierEntry(attributedEntity);
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
