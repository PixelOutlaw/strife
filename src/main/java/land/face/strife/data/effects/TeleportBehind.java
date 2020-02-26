package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;

public class TeleportBehind extends Effect {

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    Vector direction = target.getEntity().getLocation().getDirection();
    direction.setY(0.001);
    direction.normalize();

    Location location = target.getEntity().getLocation().clone();
    location.add(new Vector(0, 0.25, 0));
    location.subtract(direction.multiply(1));
    int attempts = 0;
    while (attempts <= 5 && location.getWorld().getBlockAt(location).getType().isSolid()) {
      location.subtract(direction.multiply(0.2));
      attempts++;
    }
    location.setDirection(target.getEntity().getLocation().getDirection());
    caster.getEntity().teleport(location, TeleportCause.PLUGIN);
  }
}
