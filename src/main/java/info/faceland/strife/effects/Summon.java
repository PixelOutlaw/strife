package info.faceland.strife.effects;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.AttributedEntity;
import org.bukkit.entity.LivingEntity;

public class Summon extends Effect {

  private String uniqueEntity;
  private int amount;

  @Override
  public void apply(AttributedEntity caster, LivingEntity target) {
    for (int i = 0; i < amount; i++) {
      StrifePlugin.getInstance().getUniqueEntityManager()
          .spawnUnique(uniqueEntity, caster.getEntity().getLocation());
      StrifePlugin.getInstance().getUniqueEntityManager().getLiveUniquesMap()
          .get(target).setMaster(caster.getEntity());
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
}
