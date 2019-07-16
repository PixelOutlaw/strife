package info.faceland.strife.effects;

import info.faceland.strife.data.StrifeMob;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class Teleport extends Effect {

  private Vector vector;
  private boolean relative;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (relative) {
      target.getEntity().getLocation().add(vector);
    } else {
      target.getEntity().teleport(
          new Location(caster.getEntity().getWorld(), vector.getX(), vector.getY(), vector.getZ()));
    }
  }

}
