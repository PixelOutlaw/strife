package land.face.strife.data.effects;

import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.DamageUtil.DamageScale;

public class ChangeEnergy extends Effect {

  private float amount;
  private DamageScale damageScale;
  private boolean bump;

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
        DamageUtil.restoreEnergy(target, restoreAmount, bump);
        return;
      case TARGET_CURRENT_ENERGY:
        DamageUtil.restoreBarrier(target, restoreAmount * getEnergy(target));
        return;
      case TARGET_MISSING_ENERGY:
        DamageUtil.restoreBarrier(target, restoreAmount * getMissingEnergy(target));
        return;
      case TARGET_MAX_ENERGY:
        DamageUtil.restoreBarrier(target, restoreAmount * target.getStat(StrifeStat.ENERGY));
        return;
      case CASTER_CURRENT_ENERGY:
        DamageUtil.restoreBarrier(target, restoreAmount * getEnergy(caster));
        return;
      case CASTER_MISSING_ENERGY:
        DamageUtil.restoreBarrier(target, restoreAmount * getMissingEnergy(caster));
        return;
      case CASTER_MAX_ENERGY:
        DamageUtil.restoreBarrier(target, restoreAmount * target.getStat(StrifeStat.ENERGY));
    }
  }

  public void setAmount(float amount) {
    this.amount = amount;
  }

  public void setDamageScale(DamageScale damageScale) {
    this.damageScale = damageScale;
  }

  public void setBump(boolean bump) {
    this.bump = bump;
  }

  private float getEnergy(StrifeMob mob) {
    return StrifePlugin.getInstance().getEnergyManager().getEnergy(mob);
  }

  private float getMissingEnergy(StrifeMob mob) {
    return 1 - getEnergy(mob) / mob.getStat(StrifeStat.ENERGY);
  }
}