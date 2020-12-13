package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;

public class RemoveBuff extends Effect {

  private String buffId;
  private boolean fromCaster;
  private int stacks;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    target.removeBuff(buffId, fromCaster ? caster.getEntity().getUniqueId() : null, stacks);
  }

  public void setBuffId(String buffId) {
    this.buffId = buffId;
  }

  public void setFromCaster(boolean fromCaster) {
    this.fromCaster = fromCaster;
  }

  public void setStacks(int stacks) {
    this.stacks = stacks;
  }

}