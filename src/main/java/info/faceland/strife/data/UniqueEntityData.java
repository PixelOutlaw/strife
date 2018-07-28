package info.faceland.strife.data;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.LivingEntity;

public class UniqueEntityData {

  private final UniqueEntity uniqueEntity;
  private LivingEntity master;
  private Map<Ability, Long> cooldownMap;
  private int phase;

  public UniqueEntityData(UniqueEntity uniqueEntity) {
    this.uniqueEntity = uniqueEntity;
    this.master = null;
    this.cooldownMap = new HashMap<>();
    this.phase = 0;
  }

  public UniqueEntity getUniqueEntity() {
    return uniqueEntity;
  }

  public int getPhase() {
    return phase;
  }

  public void setPhase(int phase) {
    this.phase = phase;
  }

  public LivingEntity getMaster() {
    return master;
  }

  public void setMaster(LivingEntity master) {
    this.master = master;
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
