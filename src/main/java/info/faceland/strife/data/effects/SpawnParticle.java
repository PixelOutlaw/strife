package info.faceland.strife.data.effects;

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
