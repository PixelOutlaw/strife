package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;

public class AddEarthRunes extends Effect {

  private int amount;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    target.setEarthRunes(target.getEarthRunes() + amount);
  }

  public void setAmount(int amount) {
    this.amount = amount;
  }
}