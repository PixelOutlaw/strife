package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.DamageUtil.DamageScale;
import land.face.strife.util.StatUtil;

public class ChangeEnergy extends Effect {

  private float amount;
  private DamageScale damageScale;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (target.getStat(StrifeStat.ENERGY) == 0) {
      return;
    }
    float restoreAmount = amount;
    for (StrifeStat attr : getStatMults().keySet()) {
      restoreAmount += getStatMults().get(attr) * caster.getStat(attr);
    }
    switch (damageScale) {
      case FLAT:
        StatUtil.changeEnergy(target, restoreAmount);
        return;
      case TARGET_CURRENT_ENERGY:
        StatUtil.changeEnergy(target, restoreAmount * target.getEnergy());
        return;
      case TARGET_MISSING_ENERGY:
        StatUtil.changeEnergy(target, restoreAmount * getMissingEnergy(target));
        return;
      case TARGET_MAX_ENERGY:
        StatUtil.changeEnergy(target, restoreAmount * StatUtil.getMaximumEnergy(target));
        return;
      case CASTER_CURRENT_ENERGY:
        StatUtil.changeEnergy(target, restoreAmount * caster.getEnergy());
        return;
      case CASTER_MISSING_ENERGY:
        StatUtil.changeEnergy(target, restoreAmount * getMissingEnergy(caster));
        return;
      case CASTER_MAX_ENERGY:
        StatUtil.changeEnergy(target, restoreAmount * StatUtil.getMaximumEnergy(caster));
    }
  }

  public void setAmount(float amount) {
    this.amount = amount;
  }

  public void setDamageScale(DamageScale damageScale) {
    this.damageScale = damageScale;
  }

  private float getMissingEnergy(StrifeMob mob) {
    return 1 - mob.getEnergy() / StatUtil.getMaximumEnergy(mob);
  }
}