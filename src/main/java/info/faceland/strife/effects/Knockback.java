package info.faceland.strife.effects;

import info.faceland.strife.data.StrifeMob;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class Knockback extends Effect {

  private double power;
  private double height;
  private boolean zeroVelocity;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (zeroVelocity) {
      target.getEntity().setVelocity(new Vector());
    }
    target.getEntity().setVelocity(target.getEntity().getVelocity()
        .add(getVelocity(caster.getEntity(), target.getEntity()).multiply(power/10)));
  }

  private Vector getVelocity(Entity from, Entity to) {
    Vector p1 = to.getLocation().toVector().subtract(from.getLocation().toVector());
    p1.add(new Vector(0, height/10, 0));
    return p1.normalize();
  }

  public double getPower() {
    return power;
  }

  public void setPower(double power) {
    this.power = power;
  }

  public double getHeight() {
    return height;
  }

  public void setHeight(double height) {
    this.height = height;
  }

  public boolean isZeroVelocity() {
    return zeroVelocity;
  }

  public void setZeroVelocity(boolean zeroVelocity) {
    this.zeroVelocity = zeroVelocity;
  }
}