package land.face.strife.data.effects;

import java.util.ArrayList;
import java.util.List;
import land.face.strife.StrifePlugin;
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

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    applyAtLocation(caster, TargetingUtil.getOriginLocation(target.getEntity(), getOrigin()));
  }

  @Override
  public void applyAtLocation(StrifeMob caster, Location location) {
    TargetResponse response = new TargetResponse();
    response.setLocation(location);
    StrifePlugin.getInstance().getEffectManager().executeEffectList(caster, response, originEffects);
    caster.getEntity().setVelocity(new Vector(0, 0, 0));
    if (targeted) {
      location.setDirection(caster.getEntity().getLocation().getDirection());
      caster.getEntity().teleport(location, TeleportCause.PLUGIN);
    } else if (relative) {
      location.add(vector);
      caster.getEntity().teleport(location, TeleportCause.PLUGIN);
    } else {
      location = new Location(caster.getEntity().getWorld(), vector.getX(), vector.getY(), vector.getZ());
      location.setDirection(caster.getEntity().getEyeLocation().getDirection());
      caster.getEntity().teleport(location, TeleportCause.PLUGIN);
    }
    StrifePlugin.getInstance().getEffectManager().processEffectList(caster, response, destinationEffects);
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

}
