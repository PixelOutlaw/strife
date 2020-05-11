package land.face.strife.data.effects;

import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;

public class Ignite extends Effect {

  private int duration = 0;
  private boolean forceDuration;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (forceDuration) {
      target.getEntity().setFireTicks(duration);
    } else {
      target.getEntity().setFireTicks(Math.max(duration, target.getEntity().getFireTicks()));
    }
    StrifePlugin.getInstance().getDamageOverTimeTask().trackBurning(target.getEntity());
  }

  public void setForceDuration(boolean forceDuration) {
    this.forceDuration = forceDuration;
  }

  public void setDuration(int duration) {
    this.duration = duration;
  }

}
