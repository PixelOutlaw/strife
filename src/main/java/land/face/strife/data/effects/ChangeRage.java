package land.face.strife.data.effects;

import land.face.strife.data.BonusDamage;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.DamageUtil.DamageScale;

@lombok.Setter
public class ChangeRage extends Effect {

  private float amount;
  private DamageScale damageScale;
  private boolean refreshEnrage;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (target.getStat(StrifeStat.MAXIMUM_RAGE) == 0) {
      return;
    }
    float restoreAmount = amount;

    BonusDamage bonusDamage = new BonusDamage();
    bonusDamage.setDamageScale(damageScale);
    bonusDamage.setAmount(restoreAmount);

    restoreAmount = DamageUtil.applyDamageScale(caster, target, bonusDamage);
    target.changeRage(applyMultipliers(caster, restoreAmount));
    if (refreshEnrage) {
      target.refreshEnrage();
    }
  }
}