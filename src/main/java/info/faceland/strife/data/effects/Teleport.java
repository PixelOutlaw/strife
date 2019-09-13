package info.faceland.strife.data.effects;

import info.faceland.strife.data.StrifeMob;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;

public class Teleport extends Effect {

  private Vector vector;
  private boolean targeted;
  private boolean relative;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (targeted) {
      Location location = target.getEntity().getLocation().clone();
      location.setDirection(caster.getEntity().getLocation().getDirection());
      caster.getEntity().teleport(location, TeleportCause.PLUGIN);
      return;
    }
    if (relative) {
      Location location = target.getEntity().getLocation().clone();
      location.add(vector);
      caster.getEntity().teleport(location, TeleportCause.PLUGIN);
      return;
    }
    Location location = new Location(caster.getEntity().getWorld(), vector.getX(), vector.getY(),
        vector.getZ());
    location.setDirection(target.getEntity().getLocation().getDirection());
    target.getEntity().teleport(location, TeleportCause.PLUGIN);
  }

  public void setVector(Vector vector) {
    this.vector = vector;
  }

  public void setTargeted(boolean targeted) {
    this.targeted = targeted;
  }

  public void setRelative(boolean relative) {
    this.relative = relative;
  }
}
