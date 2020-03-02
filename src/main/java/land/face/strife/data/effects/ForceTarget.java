package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import land.face.strife.util.TargetingUtil;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;

public class ForceTarget extends Effect {

  private boolean casterTargetsTarget;
  private boolean overwrite;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    LivingEntity fromTarget = casterTargetsTarget ? caster.getEntity() : target.getEntity();
    LivingEntity toTarget = casterTargetsTarget ? target.getEntity() : caster.getEntity();
    if (fromTarget instanceof Mob) {
      if (overwrite) {
        ((Mob) fromTarget).setTarget(toTarget);
        return;
      }
      LivingEntity mobTarget = TargetingUtil.getMobTarget(fromTarget);
      if (mobTarget == null) {
        ((Mob) fromTarget).setTarget(toTarget);
      }
    }
    Location newLoc = fromTarget.getLocation().clone();
    Vector savedVelocity = fromTarget.getVelocity();
    newLoc.setDirection(toTarget.getEyeLocation().subtract(fromTarget.getEyeLocation()).toVector().normalize());
    fromTarget.teleport(newLoc, TeleportCause.PLUGIN);
    fromTarget.setVelocity(savedVelocity);
  }

  public void setOverwrite(boolean overwrite) {
    this.overwrite = overwrite;
  }

  public void setCasterTargetsTarget(boolean casterTargetsTarget) {
    this.casterTargetsTarget = casterTargetsTarget;
  }
}
