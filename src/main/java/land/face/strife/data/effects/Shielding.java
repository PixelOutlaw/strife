package land.face.strife.data.effects;

import land.face.strife.data.BonusDamage;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.DamageUtil.DamageScale;
import land.face.strife.util.DamageUtil.DamageType;
import lombok.Setter;

@Setter
public class Shielding extends Effect {

  private float amount;
  private float flatBonus = 0;
  private DamageScale damageScale;
  private int durationTicks;
  private boolean strictDuration;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    float restoreAmount = amount;

    int duration = -1;
    if (durationTicks > 0) {
      var scaledDuration = (float) durationTicks;
      if (!strictDuration) {
        scaledDuration *= 1 + caster.getStat(StrifeStat.EFFECT_DURATION) / 100;
      }
      duration = (int) scaledDuration;
    }

    BonusDamage bonusDamage = new BonusDamage();
    bonusDamage.setDamageScale(damageScale);
    bonusDamage.setDamageType(DamageType.TRUE_DAMAGE);
    bonusDamage.setAmount(restoreAmount);

    restoreAmount = DamageUtil.applyDamageScale(caster, target, bonusDamage);
    restoreAmount += flatBonus;
    target.addShielding(restoreAmount, duration);
  }
}