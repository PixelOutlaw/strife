package land.face.strife.data.effects;

import land.face.strife.data.BonusDamage;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.DamageUtil.DamageScale;
import lombok.Setter;

public class ChangeAir extends Effect {

  @Setter
  private float amount;
  @Setter
  private DamageScale damageScale;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    float realAmount = amount;
    BonusDamage container = new BonusDamage();
    container.setDamageScale(damageScale);
    container.setAmount(realAmount);
    realAmount = DamageUtil.applyDamageScale(caster, target, container);
    var air = target.getEntity().getRemainingAir();
    target.getEntity().setRemainingAir(Math.min(target.getEntity().getMaximumAir(), air + (int) realAmount));
  }
}