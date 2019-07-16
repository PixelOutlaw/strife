package info.faceland.strife.effects;

import info.faceland.strife.data.StrifeMob;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class Knockback extends Effect {

  private double power;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    target.getEntity().setVelocity(target.getEntity().getVelocity()
        .add(getVelocity(caster.getEntity(), target.getEntity()).multiply(power)));
  }

  private Vector getVelocity(Entity from, Entity to) {
    Vector p1 = to.getLocation().toVector().subtract(from.getLocation().toVector());
    p1.add(new Vector(0, 0.5, 0));
    return p1.normalize();
  }

  public double getPower() {
    return power;
  }

  public void setPower(double power) {
    this.power = power;
  }
}