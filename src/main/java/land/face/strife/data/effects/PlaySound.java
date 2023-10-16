package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

@Getter @Setter
public class PlaySound extends LocationEffect {

  private String sound;
  private float volume;
  private float pitch;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    Location loc = target.getEntity().getLocation().clone();
    loc.getWorld().playSound(loc, sound, volume, pitch);
  }

  @Override
  public void applyAtLocation(StrifeMob caster, Location location) {
    location.getWorld().playSound(location, sound, volume, pitch);
  }
}
