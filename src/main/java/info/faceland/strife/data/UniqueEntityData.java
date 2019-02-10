package info.faceland.strife.data;

import org.bukkit.entity.LivingEntity;

public class UniqueEntityData {

  private final UniqueEntity uniqueEntity;
  private LivingEntity master;
  private int phase;

  public UniqueEntityData(UniqueEntity uniqueEntity) {
    this.uniqueEntity = uniqueEntity;
    this.master = null;
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

}
