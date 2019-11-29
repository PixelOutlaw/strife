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

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    double startSpeed = loadedChaser.getStartSpeed();
    Vector vector = new Vector(
        2 * startSpeed * (0.5 - Math.random()),
        startSpeed * Math.random(),
        2 * startSpeed * (0.5 - Math.random())
    );
    Location location = TargetingUtil.getOriginLocation(caster.getEntity(), originLocation);

    StrifePlugin.getInstance().getChaserManager().createChaser(caster, getId(), vector, location,
        target.getEntity());
  }

  public void setLoadedChaser(LoadedChaser loadedChaser) {
    this.loadedChaser = loadedChaser;
  }

  public void setOriginLocation(OriginLocation originLocation) {
    this.originLocation = originLocation;
  }
}