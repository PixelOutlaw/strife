package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import land.face.strife.util.CorruptionUtil;

public class Corrupt extends Effect {

  private float amount;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    getPlugin().getCorruptionManager().addCorruption(target, applyMultipliers(caster, amount), true);
  }

  public void setAmount(float amount) {
    this.amount = amount;
  }
}