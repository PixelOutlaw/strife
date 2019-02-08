package info.faceland.strife.data.effects;

import info.faceland.strife.data.AttributedEntity;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class Teleport extends Effect {

  private Vector vector;
  private boolean relative;

  @Override
  public void apply(AttributedEntity caster, AttributedEntity target) {
    if (relative) {
      target.getEntity().getLocation().add(vector);
    } else {
      target.getEntity().teleport(
          new Location(caster.getEntity().getWorld(), vector.getX(), vector.getY(), vector.getZ()));
    }
  }

}
