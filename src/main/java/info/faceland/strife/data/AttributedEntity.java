package info.faceland.strife.data;

import info.faceland.strife.attributes.StrifeAttribute;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.LivingEntity;

public class AttributedEntity {
  private final Map<StrifeAttribute, Double> attributeCache;
  private final LivingEntity livingEntity;
  private final Champion champion;

  public AttributedEntity(Champion champion) {
    this.attributeCache = new HashMap<>();
    this.livingEntity = champion.getPlayer();
    this.champion = champion;
  }

  public AttributedEntity(LivingEntity livingEntity) {
    this.attributeCache = new HashMap<>();
    this.livingEntity = livingEntity;
    this.champion = null;
  }

  public double getAttribute(StrifeAttribute attribute) {
    return attributeCache.getOrDefault(attribute, 0D);
  }

  public LivingEntity getEntity() {
    return livingEntity;
  }

  public Champion getChampion() {
    return champion;
  }

  public Map<StrifeAttribute, Double> getAttributes() {
    return attributeCache;
  }

  public void setAttributes(Map<StrifeAttribute, Double> attributes) {
    attributeCache.clear();
    attributeCache.putAll(attributes);
  }
}
