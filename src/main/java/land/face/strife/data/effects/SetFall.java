package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;

public class SetFall extends Effect {

  private float amount;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    target.getEntity().setFallDistance(amount);
  }

  public void setAmount(float amount) {
    this.amount = amount;
  }
}