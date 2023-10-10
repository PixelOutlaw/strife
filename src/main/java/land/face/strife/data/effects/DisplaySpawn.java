package land.face.strife.data.effects;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import land.face.strife.data.StrifeMob;
import land.face.strife.util.TargetingUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

@Getter @Setter
public class DisplaySpawn extends LocationEffect {

  private String displayId;
  private boolean entityLock;
  private FaceColor faceColor;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (entityLock) {
      getPlugin().getDisplayManager().create(displayId, target.getEntity(), faceColor);
    } else {
      applyAtLocation(caster, getLoc(target.getEntity()));
    }
  }

  @Override
  public void applyAtLocation(StrifeMob caster, Location location) {
    getPlugin().getDisplayManager().create(displayId, location, faceColor);
  }

  public Location getLoc(LivingEntity le) {
    return TargetingUtil.getOriginLocation(le, getOrigin());
  }
}
