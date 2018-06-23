package info.faceland.strife.effects;

import info.faceland.strife.data.AttributedEntity;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;

public class SpawnParticle extends Effect {

  private Particle particle;
  private int quantity;
  private float speed;

  @Override
  public void apply(AttributedEntity caster, LivingEntity target) {
    target.getWorld().spawnParticle(particle, target.getLocation(), quantity, getRange(), getRange(), getRange(), speed);
  }

  public Particle getParticle() {
    return particle;
  }

  public void setParticle(Particle particle) {
    this.particle = particle;
  }

  public int getQuantity() {
    return quantity;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  public float getSpeed() {
    return speed;
  }

  public void setSpeed(float speed) {
    this.speed = speed;
  }

}
