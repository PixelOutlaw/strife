package land.face.strife.data.effects;

import land.face.strife.data.BonusDamage;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.DamageUtil.DamageScale;
import land.face.strife.util.DamageUtil.DamageType;
import lombok.Setter;

@Setter
public class RestoreBarrier extends Effect {

  private float amount;
  private float flatBonus = 0;
  private DamageScale damageScale;
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
    target.changeBarrier(caster, restoreAmount);
    if (newDelayTicks >= 0) {
      target.setBarrierDelayTicks(newDelayTicks);
    }
  }
}