package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import land.face.strife.util.DamageUtil.OriginLocation;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

@Getter
@Setter
public abstract class LocationEffect extends Effect {

  private OriginLocation origin = OriginLocation.CENTER;
  private String extra = "";

  public void applyAtLocation(StrifeMob caster, Location location) {

  }
}
