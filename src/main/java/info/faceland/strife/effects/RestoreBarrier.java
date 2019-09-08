package info.faceland.strife.effects;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.util.DamageUtil;
import info.faceland.strife.util.DamageUtil.DamageScale;

public class RestoreBarrier extends Effect {

  private float amount;
  private DamageScale damageScale;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (target.getStat(StrifeStat.BARRIER) == 0) {
      return;
    }
    float restoreAmount = amount;
    for (StrifeStat attr : getStatMults().keySet()) {
      restoreAmount += getStatMults().get(attr) * caster.getStat(attr);
    }
    switch (damageScale) {
      case FLAT:
        DamageUtil.restoreBarrier(target, restoreAmount);
        return;
      case TARGET_CURRENT_BARRIER:
        DamageUtil.restoreBarrier(target, restoreAmount * getCurrentBarrier(target));
        return;
      case TARGET_MISSING_BARRIER:
        DamageUtil.restoreBarrier(target, restoreAmount * getMissingBarrier(target));
        return;
      case TARGET_MAX_BARRIER:
        DamageUtil.restoreBarrier(target, restoreAmount * target.getStat(StrifeStat.BARRIER));
        return;
      case CASTER_CURRENT_BARRIER:
        DamageUtil.restoreBarrier(target, restoreAmount * getCurrentBarrier(caster));
        return;
      case CASTER_MISSING_BARRIER:
        DamageUtil.restoreBarrier(target, restoreAmount * getMissingBarrier(caster));
        return;
      case CASTER_MAX_BARRIER:
        DamageUtil.restoreBarrier(target, restoreAmount * target.getStat(StrifeStat.BARRIER));
    }
  }

  public void setAmount(float amount) {
    this.amount = amount;
  }

  public void setDamageScale(DamageScale damageScale) {
    this.damageScale = damageScale;
  }

  private float getCurrentBarrier(StrifeMob mob) {
    return StrifePlugin.getInstance().getBarrierManager().getCurrentBarrier(mob);
  }

  private float getMissingBarrier(StrifeMob mob) {
    return 1 - getCurrentBarrier(mob) / mob.getStat(StrifeStat.BARRIER);
  }
}