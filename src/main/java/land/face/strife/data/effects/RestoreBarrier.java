package land.face.strife.data.effects;

import land.face.strife.data.BonusDamage;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.DamageUtil.DamageScale;
import land.face.strife.util.DamageUtil.DamageType;

public class RestoreBarrier extends Effect {

  private float amount;
  private DamageScale damageScale;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (target.getStat(StrifeStat.BARRIER) < 0.1 || target.hasTrait(StrifeTrait.NO_BARRIER_ALLOWED)) {
      return;
    }
    float restoreAmount = amount;
    BonusDamage bonusDamage = new BonusDamage(damageScale, DamageType.TRUE_DAMAGE, null, restoreAmount);
    restoreAmount = DamageUtil.applyDamageScale(caster, target, bonusDamage);
    target.restoreBarrier(applyMultipliers(caster, restoreAmount));
  }

  public void setAmount(float amount) {
    this.amount = amount;
  }

  public void setDamageScale(DamageScale damageScale) {
    this.damageScale = damageScale;
  }
}