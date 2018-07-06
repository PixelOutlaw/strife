package info.faceland.strife.data.effects;

import info.faceland.strife.data.AttributedEntity;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class Teleport extends Effect {

  private Vector vector;
  private boolean relative;

  @Override
  public void apply(AttributedEntity caster, LivingEntity target) {
    if (relative) {
      target.getLocation().add(vector);
    } else {
      target.teleport(
          new Location(caster.getEntity().getWorld(), vector.getX(), vector.getY(), vector.getZ()));
    }
  }

}
