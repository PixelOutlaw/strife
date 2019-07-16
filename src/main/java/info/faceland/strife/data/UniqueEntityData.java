package info.faceland.strife.data;

public class UniqueEntityData {

  private final UniqueEntity uniqueEntity;
  private int phase;
  private Spawner spawner;

  public UniqueEntityData(UniqueEntity uniqueEntity) {
    this.uniqueEntity = uniqueEntity;
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

  public Spawner getSpawner() {
    return spawner;
  }

  public void setSpawner(Spawner spawner) {
    this.spawner = spawner;
  }

}
