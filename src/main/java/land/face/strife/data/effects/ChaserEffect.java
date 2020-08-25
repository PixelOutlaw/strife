package land.face.strife.data.effects;

import land.face.strife.StrifePlugin;
import land.face.strife.data.LoadedChaser;
import land.face.strife.data.StrifeMob;
import land.face.strife.util.DamageUtil.OriginLocation;
import land.face.strife.util.TargetingUtil;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class ChaserEffect extends Effect {

  private LoadedChaser loadedChaser;
  private OriginLocation originLocation;
  private Location overrideLocation;
  private boolean canLocationOverride;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    double startSpeed = loadedChaser.getStartSpeed();
    Vector vector = new Vector(
        2 * startSpeed * (0.5 - Math.random()),
        startSpeed * Math.random(),
        2 * startSpeed * (0.5 - Math.random())
    );
    Location location;
    if (overrideLocation == null) {
      location = TargetingUtil.getOriginLocation(caster.getEntity(), originLocation);
    } else {
      location = overrideLocation;
      overrideLocation = null;
    }

    StrifePlugin.getInstance().getChaserManager().createChaser(caster, getId(), vector, location, target.getEntity());
  }

  public boolean isCanLocationOverride() {
    return canLocationOverride;
  }

  public void setCanLocationOverride(boolean canLocationOverride) {
    this.canLocationOverride = canLocationOverride;
  }


  public void setOverrideLocation(Location location) {
    overrideLocation = location;
  }

  public void setLoadedChaser(LoadedChaser loadedChaser) {
    this.loadedChaser = loadedChaser;
  }

  public void setOriginLocation(OriginLocation originLocation) {
    this.originLocation = originLocation;
  }
}