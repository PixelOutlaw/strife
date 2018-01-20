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

  public void updateEntity(LivingEntity entity) {
    if (!entity.isValid()) {
      removeEntity(entity);
      return;
    }
    AttributedEntity attributedEntity = new AttributedEntity(entity);
    Map<StrifeAttribute, Double> statMap;
    if (entity instanceof Player) {
      Champion champion = plugin.getChampionManager().getChampion(entity.getUniqueId());
      statMap = champion.getAttributeValues(true);
    } else {
      statMap = buildEntityStats(entity);
    }
    for (StrifeAttribute attr : statMap.keySet()) {
      attributedEntity.setAttribute(attr, statMap.get(attr));
    }
    trackedEntities.put(entity.getUniqueId(), attributedEntity);
    System.out.println(trackedEntities.toString());
  }

  public void removeEntity(LivingEntity entity) {
    if (trackedEntities.get(entity.getUniqueId()) != null) {
      trackedEntities.remove(entity.getUniqueId());
    }
  }

  public AttributedEntity getEntity(LivingEntity entity, boolean update) {
    if (update || !trackedEntities.containsKey((entity.getUniqueId()))) {
      updateEntity(entity);
    }
    return trackedEntities.get(entity.getUniqueId());
  }

  public double getStat(LivingEntity entity, StrifeAttribute attribute) {
    if (!trackedEntities.containsKey(entity.getUniqueId())) {
      updateEntity(entity);
    }
    return trackedEntities.get(entity.getUniqueId()).getAttribute(attribute);
  }

  public Map<StrifeAttribute, Double> buildEntityStats(LivingEntity entity) {
    return plugin.getMonsterManager().getBaseStats(entity.getType(), getEntityLevel(entity));
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
