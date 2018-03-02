package info.faceland.strife.data;

import info.faceland.strife.attributes.StrifeAttribute;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.LivingEntity;

public class AttributedEntity {
  private final Map<StrifeAttribute, Double> attributeCache;
  private final LivingEntity livingEntity;

  public AttributedEntity(LivingEntity livingEntity) {
    this.attributeCache = new HashMap<>();
    this.livingEntity = livingEntity;
  }

  public double getAttribute(StrifeAttribute attribute) {
    if (attributeCache.get(attribute) == null) {
      return 0;
    }
    return attributeCache.get(attribute);
  }

  public LivingEntity getEntity() {
    return livingEntity;
  }

  public Map<StrifeAttribute, Double> getAttributes() {
    return attributeCache;
  }

  public void setAttributes(Map<StrifeAttribute, Double> attributes) {
    attributeCache.clear();
    attributeCache.putAll(attributes);
  }
}
