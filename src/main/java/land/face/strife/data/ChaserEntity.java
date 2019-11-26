package land.face.strife.data;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class ChaserEntity {

  private final StrifeMob caster;
  private final String chaserId;
  private final int lifespan;
  private final float speed;

  private Vector velocity;
  private Location location;
  private LivingEntity target;
  private int currentTick;

  public ChaserEntity(final StrifeMob caster, String chaserId, Location location,
      LivingEntity target, float speed, Vector velocity, int lifespan) {
    this.caster = caster;
    this.chaserId = chaserId;
    this.speed = speed;
    this.velocity = velocity;
    this.lifespan = lifespan;
    this.location = location;
    this.target = target;
    this.currentTick = 0;
  }

  public StrifeMob getCaster() {
    return caster;
  }

  public String getChaserId() {
    return chaserId;
  }

  public int getLifespan() {
    return lifespan;
  }

  public float getSpeed() {
    return speed;
  }

  public Vector getVelocity() {
    return velocity;
  }

  public void setVelocity(Vector velocity) {
    this.velocity = velocity;
  }

  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }

  public LivingEntity getTarget() {
    return target;
  }

  public void setTarget(LivingEntity target) {
    this.target = target;
  }

  public int getCurrentTick() {
    return currentTick;
  }

  public void setCurrentTick(int currentTick) {
    this.currentTick = currentTick;
  }
}
