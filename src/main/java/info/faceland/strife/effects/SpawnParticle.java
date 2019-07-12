package info.faceland.strife.effects;

import info.faceland.strife.data.StrifeMob;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;

public class SpawnParticle extends Effect {

  private Particle particle;
  private int quantity;
  private float spread;
  private float speed;
  private ParticleOriginLocation particleOriginLocation = ParticleOriginLocation.CENTER;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    Location loc = getLoc(target.getEntity());
    target.getEntity().getWorld()
        .spawnParticle(particle, loc, quantity, spread, spread, spread, speed);
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

  public void setSpread(float spread) {
    this.spread = spread;
  }

  public void setParticleOriginLocation(
      ParticleOriginLocation particleOriginLocation) {
    this.particleOriginLocation = particleOriginLocation;
  }

  private Location getLoc(LivingEntity le) {
    switch (particleOriginLocation) {
      case HEAD:
        return le.getEyeLocation();
      case CENTER:
        return le.getEyeLocation().clone()
            .subtract(le.getEyeLocation().clone().subtract(le.getLocation()).multiply(0.5));
      case GROUND:
        return le.getLocation();
    }
    return null;
  }

  public enum ParticleOriginLocation {
    HEAD,
    CENTER,
    GROUND
  }
}
