package info.faceland.strife.effects;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.managers.MinionManager;
import info.faceland.strife.managers.StrifeMobManager;
import info.faceland.strife.managers.UniqueEntityManager;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public class Summon extends Effect {

  private String uniqueEntity;
  private int amount;
  private double lifespanSeconds;
  private double spawnRange;

  private static final StrifeMobManager entityManager = StrifePlugin.getInstance()
      .getStrifeMobManager();
  private static final UniqueEntityManager uniqueManager = StrifePlugin.getInstance()
      .getUniqueEntityManager();
  private static final MinionManager minionManager = StrifePlugin.getInstance().getMinionManager();

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    for (int i = 0; i < amount; i++) {
      Location loc =
          target == null ? caster.getEntity().getLocation() : target.getEntity().getLocation();
      LivingEntity summon = uniqueManager.spawnUnique(uniqueEntity, loc);
      if (summon == null) {
        return;
      }
      StrifeMob summonedEntity = entityManager.getAttributedEntity(summon);
      caster.getMinions().add(summonedEntity);
      summonedEntity.setDespawnOnUnload(true);
      minionManager.getMinionDecayMap().put(summon, (int) ((lifespanSeconds * 20D) / 11D));
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

  public double getLifespanSeconds() {
    return lifespanSeconds;
  }

  public void setLifespanSeconds(double lifespanSeconds) {
    this.lifespanSeconds = lifespanSeconds;
  }
}
