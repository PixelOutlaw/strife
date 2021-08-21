package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import lombok.Setter;

public class Stinger extends Effect {

  @Setter
  private int amount;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    target.getEntity().setBeeStingersInBody(target.getEntity().getBeeStingersInBody() + amount);
  }
}