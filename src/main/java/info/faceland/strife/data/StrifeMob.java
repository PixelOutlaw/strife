package info.faceland.strife.data;

import info.faceland.strife.data.ability.Ability;
import info.faceland.strife.data.champion.Champion;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.stats.StrifeTrait;
import io.netty.util.internal.ConcurrentSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.bukkit.entity.LivingEntity;

public class StrifeMob {

  private final Map<StrifeStat, Double> attributeCache = new HashMap<>();
  private final Map<Ability, Long> cooldownMap = new HashMap<>();

  private final Champion champion;
  private LivingEntity livingEntity;

  private final Set<StrifeMob> minions = new ConcurrentSet<>();

  private boolean despawnOnUnload = false;

  public StrifeMob(Champion champion) {
    this.livingEntity = champion.getPlayer();
    this.champion = champion;
  }

  public StrifeMob(LivingEntity livingEntity) {
    this.livingEntity = livingEntity;
    this.champion = null;
  }

  public double getAttribute(StrifeStat attribute) {
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

  public Map<StrifeStat, Double> getAttributes() {
    return attributeCache;
  }

  public void setAttributes(Map<StrifeStat, Double> attributes) {
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

  public boolean isMinionOf(StrifeMob strifeMob) {
    return strifeMob.getMinions().contains(this);
  }

  public boolean isMasterOf(StrifeMob strifeMob) {
    return getMinions().contains(strifeMob);
  }

  public boolean isMasterOf(LivingEntity entity) {
    for (StrifeMob strifeMob : minions) {
      if (strifeMob.getEntity() == entity) {
        return true;
      }
    }
    return false;
  }

  public boolean hasTrait(StrifeTrait trait) {
    if (champion == null) {
      return false;
    }
    return champion.hasTrait(trait);
  }

  public Set<StrifeMob> getMinions() {
    for (StrifeMob minion : minions) {
      if (minion == null || minion.getEntity() == null || !minion.getEntity().isValid()) {
        minions.remove(minion);
      }
    }
    return minions;
  }

  public boolean isDespawnOnUnload() {
    return despawnOnUnload;
  }

  public void setDespawnOnUnload(boolean despawnOnUnload) {
    this.despawnOnUnload = despawnOnUnload;
  }
}
