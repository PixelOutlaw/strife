package info.faceland.strife.effects;

import info.faceland.strife.data.StrifeMob;
import org.bukkit.Location;
import org.bukkit.Particle;

public class SpawnParticle extends Effect {

  private Particle particle;
  private int quantity;
  private float speed;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    Location loc = target.getEntity().getLocation().clone()
        .add(target.getEntity().getEyeLocation());
    loc.multiply(0.5);
    target.getEntity().getWorld()
        .spawnParticle(particle, loc, quantity, getRange(), getRange(), getRange(), speed);
  }

  public void setParticle(Particle particle) {
    this.particle = particle;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  public void setSpeed(float speed) {
    this.speed = speed;
  }

}
