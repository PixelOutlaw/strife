package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import land.face.strife.util.DamageUtil.OriginLocation;
import org.bukkit.Location;

public abstract class LocationEffect extends Effect {

  private OriginLocation origin = OriginLocation.CENTER;

  public void applyAtLocation(StrifeMob caster, Location location) {

  }

  public OriginLocation getOrigin() {
    return origin;
  }

  public void setOrigin(OriginLocation origin) {
    this.origin = origin;
  }
}
