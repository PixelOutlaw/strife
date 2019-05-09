package info.faceland.strife.data;

import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.ability.Ability;
import info.faceland.strife.data.champion.Champion;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.LivingEntity;

public class AttributedEntity {
  private final Map<StrifeAttribute, Double> attributeCache;
  private final Champion champion;
  private final Map<Ability, Long> cooldownMap;
  private LivingEntity livingEntity;

  public AttributedEntity(Champion champion) {
    this.attributeCache = new HashMap<>();
    this.livingEntity = champion.getPlayer();
    this.champion = champion;
    this.cooldownMap = new HashMap<>();
  }

  public AttributedEntity(LivingEntity livingEntity) {
    this.attributeCache = new HashMap<>();
    this.livingEntity = livingEntity;
    this.champion = null;
    this.cooldownMap = new HashMap<>();
  }

  public double getAttribute(StrifeAttribute attribute) {
    return attributeCache.getOrDefault(attribute, 0D);
  }

  public LivingEntity getEntity() {
    return livingEntity;
  }

  public void setLivingEntity(LivingEntity livingEntity) {
    this.livingEntity = livingEntity;
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

  public boolean isCooledDown(Ability ability) {
    if (cooldownMap.containsKey(ability)) {
      return System.currentTimeMillis() > cooldownMap.get(ability);
    }
    return true;
  }

  public void setCooldown(Ability ability) {
    cooldownMap.put(ability, System.currentTimeMillis() + ability.getCooldown() * 1000);
  }
}
