package land.face.strife.data.effects;

import land.face.strife.data.ChaserEntity;
import land.face.strife.data.LoadedChaser;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.DamageUtil.OriginLocation;
import land.face.strife.util.TargetingUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.util.Vector;

@Getter
@Setter
public class ChaserEffect extends Effect {

  private LoadedChaser loadedChaser;
  private OriginLocation originLocation;
  private Location overrideLocation;
  private boolean canLocationOverride;
  private boolean chaseCaster;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
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
    float durationMult = loadedChaser.isStrictDuration() ? 1 : 1 + caster.getStat(StrifeStat.EFFECT_DURATION) / 100;
    Vector direction = chaseCaster ?
        caster.getEntity().getLocation().clone().subtract(target.getEntity().getLocation()).toVector().normalize() :
        target.getEntity().getLocation().clone().subtract(caster.getEntity().getLocation()).toVector().normalize();
    ChaserEntity.create(
        loadedChaser,
        location,
        direction,
        caster,
        chaseCaster ? caster.getEntity() : target.getEntity(),
        null,
        durationMult,
        speedMult
    );
  }
}