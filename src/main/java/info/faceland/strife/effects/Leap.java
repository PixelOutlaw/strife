package info.faceland.strife.effects;

import info.faceland.strife.data.StrifeMob;
import org.bukkit.util.Vector;

public class Leap extends Effect {

  private double height;
  private double forward;
  private boolean zeroVelocity;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (zeroVelocity) {
      target.getEntity().setVelocity(new Vector());
    }
    target.getEntity().setVelocity(target.getEntity().getVelocity().add(new Vector(0, height/10, 0)
        .add(target.getEntity().getEyeLocation().getDirection().multiply(forward/10))));
  }

  public double getHeight() {
    return height;
  }

  public void setHeight(double height) {
    this.height = height;
  }

  public double getForward() {
    return forward;
  }

  public void setForward(double forward) {
    this.forward = forward;
  }

  public boolean isZeroVelocity() {
    return zeroVelocity;
  }

  public void setZeroVelocity(boolean zeroVelocity) {
    this.zeroVelocity = zeroVelocity;
  }
}