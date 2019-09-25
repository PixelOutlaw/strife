package info.faceland.strife.data;

import info.faceland.strife.data.effects.Effect;
import java.util.List;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class WorldSpaceEffectEntity {

  private final Map<Integer, List<Effect>> effectSchedule;
  private final int maxTicks;
  private final Vector velocity;
  private final StrifeMob caster;
  private Location location;
  private int currentTick;
  private int lifespan;

  public WorldSpaceEffectEntity(final StrifeMob caster,
      final Map<Integer, List<Effect>> effectSchedule, Location location, final Vector velocity,
      final int maxTicks, int lifespan) {
    this.caster = caster;
    this.effectSchedule = effectSchedule;
    this.velocity = velocity;
    this.maxTicks = maxTicks;
    this.lifespan = lifespan;
    this.location = location;
    this.currentTick = 0;
  }

  public Vector getVelocity() {
    return velocity;
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
}
