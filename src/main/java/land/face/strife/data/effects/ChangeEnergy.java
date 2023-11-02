package land.face.strife.data.effects;

import land.face.strife.data.BonusDamage;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.DamageUtil.DamageScale;
import land.face.strife.util.PlayerDataUtil;
import land.face.strife.util.StatUtil;
import lombok.Setter;

public class ChangeEnergy extends Effect {

  private float amount;
  private DamageScale damageScale;

  @Setter
  private int tickDuration;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (target.getStat(StrifeStat.ENERGY) == 0) {
      return;
    }
    float restoreAmount = amount;

    BonusDamage bonusDamage = new BonusDamage();
    bonusDamage.setDamageScale(damageScale);
    bonusDamage.setAmount(restoreAmount);

    restoreAmount = DamageUtil.applyDamageScale(caster, target, bonusDamage);


    if (tickDuration == -1) {
      StatUtil.changeEnergy(target, applyMultipliers(caster, restoreAmount));
    } else {
      PlayerDataUtil.restoreEnergyOverTime(target.getEntity(),
          applyMultipliers(caster, restoreAmount), tickDuration);
    }
  }

  public void setAmount(float amount) {
    this.amount = amount;
  }

  public void setDamageScale(DamageScale damageScale) {
    this.damageScale = damageScale;
  }
}