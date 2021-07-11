package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import land.face.strife.util.DamageUtil;

public class Corrupt extends Effect {

  private float amount;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    DamageUtil.applyCorrupt(target.getEntity(), applyMultipliers(caster, amount), true);
  }

  public void setAmount(float amount) {
    this.amount = amount;
  }
}