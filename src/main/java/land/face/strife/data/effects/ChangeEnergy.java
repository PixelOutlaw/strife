package land.face.strife.data.effects;

import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.DamageUtil;
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
        DamageUtil.restoreEnergy(target, restoreAmount);
        return;
      case TARGET_CURRENT_ENERGY:
        DamageUtil.restoreEnergy(target, restoreAmount * getEnergy(target));
        return;
      case TARGET_MISSING_ENERGY:
        DamageUtil.restoreEnergy(target, restoreAmount * getMissingEnergy(target));
        return;
      case TARGET_MAX_ENERGY:
        DamageUtil.restoreEnergy(target, restoreAmount * StatUtil.getMaximumEnergy(target));
        return;
      case CASTER_CURRENT_ENERGY:
        DamageUtil.restoreEnergy(target, restoreAmount * getEnergy(caster));
        return;
      case CASTER_MISSING_ENERGY:
        DamageUtil.restoreEnergy(target, restoreAmount * getMissingEnergy(caster));
        return;
      case CASTER_MAX_ENERGY:
        DamageUtil.restoreEnergy(target, restoreAmount * StatUtil.getMaximumEnergy(caster));
    }
  }

  public void setAmount(float amount) {
    this.amount = amount;
  }

  public void setDamageScale(DamageScale damageScale) {
    this.damageScale = damageScale;
  }

  private float getEnergy(StrifeMob mob) {
    return StrifePlugin.getInstance().getEnergyManager().getEnergy(mob);
  }

  private float getMissingEnergy(StrifeMob mob) {
    return 1 - getEnergy(mob) / StatUtil.getMaximumEnergy(mob);
  }
}