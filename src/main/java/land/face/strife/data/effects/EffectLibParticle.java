package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import land.face.strife.util.DamageUtil.OriginLocation;
import land.face.strife.util.TargetingUtil;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;

public class EffectLibParticle extends LocationEffect {

  @Setter
  private String particleClass;
  @Setter
  private ConfigurationSection particleConfig;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    Location location = TargetingUtil.getOriginLocation(target.getEntity(), getOrigin());
    Entity entity = target.getEntity();
    if (entity == null) {
      applyAtLocation(caster, location);
    } else {
      if (getOrigin() == OriginLocation.GROUND) {
        getPlugin().getEffectLibManager()
            .start(particleClass, particleConfig, entity.getLocation(), entity);
      } else {
        getPlugin().getEffectLibManager()
            .start(particleClass, particleConfig, location, entity);
      }
    }
  }

  @Override
  public void applyAtLocation(StrifeMob caster, Location location) {
    getPlugin().getEffectLibManager()
        .start(particleClass, particleConfig, location);
  }
}
