package info.faceland.strife.data.effects;

import info.faceland.strife.data.AttributedEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class Leap extends Effect {

  private double height;
  private double forward;

  @Override
  public void apply(AttributedEntity caster, LivingEntity target) {
    caster.getEntity().setVelocity(caster.getEntity().getVelocity().add(new Vector(0, height, 0)
        .add(caster.getEntity().getEyeLocation().getDirection().multiply(forward))));
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
}