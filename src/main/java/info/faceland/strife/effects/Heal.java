package info.faceland.strife.effects;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.effects.DealDamage.DamageScale;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.util.DamageUtil;
import org.bukkit.entity.LivingEntity;

public class Heal extends Effect {

  private double amount;
  private DamageScale damageScale;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    double heal = amount;
    for (StrifeStat attr : getStatMults().keySet()) {
      heal += getStatMults().get(attr) * caster.getStat(attr);
    }
    switch (damageScale) {
      case FLAT:
        DamageUtil.restoreHealth(target.getEntity(), heal);
        return;
      case TARGET_CURRENT_HEALTH:
        DamageUtil.restoreHealth(target.getEntity(), heal * target.getEntity().getHealth());
        return;
      case TARGET_MISSING_HEALTH:
        DamageUtil.restoreHealth(target.getEntity(), heal * getMissingHealth(target.getEntity()));
        return;
      case TARGET_MAX_HEALTH:
        DamageUtil.restoreHealth(target.getEntity(), heal * target.getEntity().getMaxHealth());
        return;
      case CASTER_CURRENT_HEALTH:
        DamageUtil.restoreHealth(target.getEntity(), heal * caster.getEntity().getHealth());
        return;
      case CASTER_MISSING_HEALTH:
        DamageUtil.restoreHealth(target.getEntity(), heal * getMissingHealth(caster.getEntity()));
        return;
      case CASTER_MAX_HEALTH:
        DamageUtil.restoreHealth(target.getEntity(), heal * caster.getEntity().getMaxHealth());
    }
  }

  public void setAmount(double amount) {
    this.amount = amount;
  }

  public void setDamageScale(DamageScale damageScale) {
    this.damageScale = damageScale;
  }

  private double getMissingHealth(LivingEntity ent) {
    return ent.getMaxHealth() - ent.getHealth();
  }
}