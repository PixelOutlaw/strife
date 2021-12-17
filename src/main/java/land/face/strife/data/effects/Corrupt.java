package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import land.face.strife.util.CorruptionUtil;

public class Corrupt extends Effect {

  private float amount;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    CorruptionUtil.applyCorrupt(target, applyMultipliers(caster, amount), true);
  }

  public void setAmount(float amount) {
    this.amount = amount;
  }
}