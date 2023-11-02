package land.face.strife.data.effects;

import land.face.strife.data.BonusDamage;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.DamageUtil.DamageScale;
import land.face.strife.util.DamageUtil.DamageType;
import lombok.Setter;

public class RestoreBarrier extends Effect {

  @Setter
  private float amount;
  @Setter
  private float flatBonus = 0;
  @Setter
  private DamageScale damageScale;
  @Setter
  private int newDelayTicks;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (target.getMaxBarrier() < 0.1 || target.hasTrait(StrifeTrait.NO_BARRIER_ALLOWED)) {
      return;
    }
    float restoreAmount = amount;

    BonusDamage bonusDamage = new BonusDamage();
    bonusDamage.setDamageScale(damageScale);
    bonusDamage.setDamageType(DamageType.TRUE_DAMAGE);
    bonusDamage.setAmount(restoreAmount);

    restoreAmount = DamageUtil.applyDamageScale(caster, target, bonusDamage);
    restoreAmount += flatBonus;
    target.restoreBarrier(applyMultipliers(caster, restoreAmount));
    if (newDelayTicks >= 0) {
      target.setBarrierDelayTicks(newDelayTicks);
    }
  }
}