package info.faceland.strife.data;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.math.NumberUtils;
import com.tealcube.minecraft.bukkit.shade.google.common.base.CharMatcher;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class EntityStatCache {

  private final StrifePlugin plugin;
  private Map<UUID, AttributedEntity> trackedEntities = new HashMap<>();

  public EntityStatCache(StrifePlugin plugin) {
    this.plugin = plugin;
    this.trackedEntities = new HashMap<>();
  }

  public Map<StrifeAttribute, Double> getEntityStats(LivingEntity entity) {
    if (!trackedEntities.containsKey(entity.getUniqueId())) {
      buildEntityStats(entity);
    }
    return trackedEntities.get(entity.getUniqueId()).getAttributes();
  }

  public AttributedEntity getAttributedEntity(LivingEntity entity) {
    if (!trackedEntities.containsKey(entity.getUniqueId())) {
      buildEntityStats(entity);
    }
    return trackedEntities.get(entity.getUniqueId());
  }

  public void buildEntityStats(LivingEntity entity) {
    AttributedEntity attributedEntity;
    if (!trackedEntities.containsKey(entity.getUniqueId())) {
      attributedEntity = new AttributedEntity(entity);
    } else {
      attributedEntity = trackedEntities.get(entity.getUniqueId());
    }
    attributedEntity.setAttributes(plugin.getMonsterManager().getBaseStats(entity.getType(), getEntityLevel(entity)));
    trackedEntities.put(entity.getUniqueId(), attributedEntity);
  }

  public void setEntityStats(LivingEntity entity, Map<StrifeAttribute, Double> statMap) {
    AttributedEntity attributedEntity = getAttributedEntity(entity);
    attributedEntity.setAttributes(statMap);
    trackedEntities.put(entity.getUniqueId(), attributedEntity);
  }

  public void removeEntity(LivingEntity entity) {
    if (trackedEntities.get(entity.getUniqueId()) != null) {
      trackedEntities.remove(entity.getUniqueId());
    }
  }

  private int getEntityLevel(LivingEntity entity) {
    if (entity instanceof Player) {
      return ((Player) entity).getLevel();
    }
    if (entity.getCustomName() != null) {
      System.out.println("LEVEL: " + CharMatcher.DIGIT.retainFrom(ChatColor.stripColor(entity.getCustomName())));
      return NumberUtils.toInt(CharMatcher.DIGIT.retainFrom(ChatColor.stripColor(entity.getCustomName())), 0);
    }
    return 0;
  }
}
