package land.face.strife.data.effects;

import java.util.ArrayList;
import java.util.List;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import org.bukkit.Location;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;

public class Teleport extends Effect {

  private Vector vector;
  private boolean targeted;
  private boolean relative;
  private List<Effect> destinationEffects = new ArrayList<>();
  private List<Effect> originEffects = new ArrayList<>();

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    StrifePlugin.getInstance().getEffectManager().processEffectList(caster, caster.getEntity(), originEffects);
    target.getEntity().setVelocity(new Vector(0, 0, 0));
    if (targeted) {
      Location location = target.getEntity().getLocation().clone();
      location.setDirection(caster.getEntity().getLocation().getDirection());
      caster.getEntity().teleport(location, TeleportCause.PLUGIN);
    } else if (relative) {
      Location location = target.getEntity().getLocation().clone();
      location.add(vector);
      caster.getEntity().teleport(location, TeleportCause.PLUGIN);
      return;
    } else {
      Location location = new Location(caster.getEntity().getWorld(),
          vector.getX(), vector.getY(), vector.getZ());
      location.setDirection(target.getEntity().getLocation().getDirection());
      target.getEntity().teleport(location, TeleportCause.PLUGIN);
    }
    StrifePlugin.getInstance().getEffectManager().processEffectList(caster, caster.getEntity(), destinationEffects);
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
