package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import org.bukkit.Location;

public class Lightning extends LocationEffect {

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    applyAtLocation(caster, target.getEntity().getLocation());
  }

  @Override
  public void applyAtLocation(StrifeMob caster, Location location) {
    location.getWorld().strikeLightningEffect(location);
  }
}