package land.face.strife.data.effects;

import land.face.strife.StrifePlugin;
import land.face.strife.data.LoadedChaser;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.DamageUtil.OriginLocation;
import land.face.strife.util.TargetingUtil;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class ChaserEffect extends Effect {

  private LoadedChaser loadedChaser;
  private OriginLocation originLocation;
  private Location overrideLocation;
  private boolean canLocationOverride;
  private boolean chaseCaster;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    double startSpeed = loadedChaser.getStartSpeed();
    Vector vector = new Vector(
        2 * startSpeed * (0.5 - StrifePlugin.RNG.nextFloat()),
        startSpeed * StrifePlugin.RNG.nextFloat(),
        2 * startSpeed * (0.5 - StrifePlugin.RNG.nextFloat())
    );
    Location location;
    if (overrideLocation == null) {
      if (canLocationOverride) {
        location = TargetingUtil.getOriginLocation(target.getEntity(), originLocation);
      } else {
        location = TargetingUtil.getOriginLocation(caster.getEntity(), originLocation);
      }
    } else {
      location = overrideLocation;
      overrideLocation = null;
    }

    float speedMult = 1 + caster.getStat(StrifeStat.PROJECTILE_SPEED) / 100;
    if (chaseCaster) {
      getPlugin().getChaserManager().createChaser(caster, getId(), vector, location, caster.getEntity(), speedMult);
    } else {
      getPlugin().getChaserManager().createChaser(caster, getId(), vector, location, target.getEntity(), speedMult);
    }
  }

  public boolean isCanLocationOverride() {
    return canLocationOverride;
  }

  public void setCanLocationOverride(boolean canLocationOverride) {
    this.canLocationOverride = canLocationOverride;
  }

  public void setChaseCaster(boolean chaseCaster) {
    this.chaseCaster = chaseCaster;
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