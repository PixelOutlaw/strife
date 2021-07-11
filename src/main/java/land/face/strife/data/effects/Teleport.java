package land.face.strife.data.effects;

import java.util.ArrayList;
import java.util.List;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.TargetResponse;
import land.face.strife.util.TargetingUtil;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;

public class Teleport extends LocationEffect {

  private Vector vector;
  private boolean targeted;
  private boolean relative;
  private final List<Effect> destinationEffects = new ArrayList<>();
  private final List<Effect> originEffects = new ArrayList<>();
  private final List<String> worldSwapWhitelist = new ArrayList<>();

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    applyAtLocation(caster, TargetingUtil.getOriginLocation(target.getEntity(), getOrigin()));
  }

  @Override
  public void applyAtLocation(StrifeMob caster, Location location) {

    if (caster.getEntity().getLocation().getWorld() != location.getWorld()) {
      if (!worldSwapWhitelist.contains(location.getWorld().getName())) {
        return;
      }
    }

    TargetResponse response = new TargetResponse(TargetingUtil.getOriginLocation(caster.getEntity(), getOrigin()));
    getPlugin().getEffectManager().executeEffectList(caster, response, originEffects);

    Location finalLocation = location;
    caster.getEntity().setVelocity(new Vector(0, 0, 0));
    caster.getEntity().setFallDistance(0);
    if (targeted) {
      finalLocation.setDirection(caster.getEntity().getLocation().getDirection());
      caster.getEntity().teleport(finalLocation, TeleportCause.PLUGIN);
    } else if (relative) {
      finalLocation.add(vector);
      caster.getEntity().teleport(finalLocation, TeleportCause.PLUGIN);
    } else {
      finalLocation = new Location(caster.getEntity().getWorld(), vector.getX(), vector.getY(), vector.getZ());
      finalLocation.setDirection(caster.getEntity().getEyeLocation().getDirection());
      caster.getEntity().teleport(finalLocation, TeleportCause.PLUGIN);
    }
    response.setLocation(finalLocation);

    getPlugin().getEffectManager().processEffectList(caster, response, destinationEffects);
  }

  public void setVector(Vector vector) {
    this.vector = vector;
  }

  public void setTargeted(boolean targeted) {
    this.targeted = targeted;
  }

  public void setRelative(boolean relative) {
    this.relative = relative;
  }

  public List<Effect> getDestinationEffects() {
    return destinationEffects;
  }

  public List<Effect> getOriginEffects() {
    return originEffects;
  }

  public List<String> getWorldSwapWhitelist() {
    return worldSwapWhitelist;
  }

}
