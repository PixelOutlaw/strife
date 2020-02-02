package land.face.strife.data;

import java.util.List;
import java.util.Map;
import land.face.strife.data.effects.Effect;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class WorldSpaceEffect {

  private final Map<Integer, List<Effect>> effectSchedule;
  private final int maxTicks;
  private final float maxDisplacement;
  private final StrifeMob caster;
  private final float gravity;
  private final float friction;

  private Location location;
  private Vector velocity;
  private int lifespan;

  private int currentTick = 0;

  public WorldSpaceEffect(final StrifeMob caster, final Map<Integer, List<Effect>> effectSchedule,
      Location location, final Vector velocity, final float gravity, final float friction,
      final float maxDisplacement, final int maxTicks, final int lifespan) {
    this.caster = caster;
    this.effectSchedule = effectSchedule;
    this.maxDisplacement = maxDisplacement;
    this.velocity = velocity;
    this.gravity = gravity;
    this.friction = friction;
    this.maxTicks = maxTicks;
    this.lifespan = lifespan;
    this.location = location;
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

  public Map<Integer, List<Effect>> getEffectSchedule() {
    return effectSchedule;
  }

  public int getMaxTicks() {
    return maxTicks;
  }

  public StrifeMob getCaster() {
    return caster;
  }

  public void setLocation(Location location) {
    this.location = location;
  }

  public int getCurrentTick() {
    return currentTick;
  }

  public void setCurrentTick(int currentTick) {
    this.currentTick = currentTick;
  }

  public int getLifespan() {
    return lifespan;
  }

  public void setLifespan(int lifespan) {
    this.lifespan = lifespan;
  }

  public float getGravity() {
    return gravity;
  }

  public float getFriction() {
    return friction;
  }

  public float getMaxDisplacement() {
    return maxDisplacement;
  }
}
