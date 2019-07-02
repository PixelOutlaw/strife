package info.faceland.strife.effects;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.managers.AttributedEntityManager;
import info.faceland.strife.managers.UniqueEntityManager;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public class Summon extends Effect {

  private String uniqueEntity;
  private int amount;
  private double spawnRange;

  private static final AttributedEntityManager entityManager = StrifePlugin.getInstance()
      .getAttributedEntityManager();
  private static final UniqueEntityManager uniqueManager = StrifePlugin.getInstance()
      .getUniqueEntityManager();

  @Override
  public void apply(AttributedEntity caster, AttributedEntity target) {
    for (int i = 0; i < amount; i++) {
      Location loc =
          target == null ? caster.getEntity().getLocation() : target.getEntity().getLocation();
      LivingEntity summon = uniqueManager.spawnUnique(uniqueEntity, loc);
      AttributedEntity summonedEntity = entityManager.getAttributedEntity(summon);
      caster.getMinions().add(summonedEntity);
      summonedEntity.setDespawnOnUnload(true);
    }
  }

  public String getUniqueEntity() {
    return uniqueEntity;
  }

  public void setUniqueEntity(String uniqueEntity) {
    this.uniqueEntity = uniqueEntity;
  }

  public int getAmount() {
    return amount;
  }

  public void setAmount(int amount) {
    this.amount = amount;
  }

  public double getSpawnRange() {
    return spawnRange;
  }

  public void setSpawnRange(double spawnRange) {
    this.spawnRange = spawnRange;
  }
}
