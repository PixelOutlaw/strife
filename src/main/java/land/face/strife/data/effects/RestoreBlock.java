package land.face.strife.data.effects;

import land.face.strife.data.BonusDamage;
import land.face.strife.data.StrifeMob;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.DamageUtil.DamageScale;
import land.face.strife.util.DamageUtil.DamageType;
import lombok.Setter;

public class RestoreBlock extends Effect {

  @Setter
  private float amount;
  @Setter
  private float flatBonus = 0;
  @Setter
  private DamageScale damageScale;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    float restoreAmount = amount;
    BonusDamage bonusDamage = new BonusDamage();
    bonusDamage.setDamageScale(damageScale);
    bonusDamage.setDamageType(DamageType.TRUE_DAMAGE);
    bonusDamage.setAmount(restoreAmount);
    restoreAmount = DamageUtil.applyDamageScale(caster, target, bonusDamage);
    restoreAmount += flatBonus;
    target.setBlock(target.getBlock() + applyMultipliers(caster, restoreAmount));
  }
}