package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;

public class LookAt extends Effect {

  @Setter
  private boolean casterToTarget;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    LivingEntity fromTarget = casterToTarget ? caster.getEntity() : target.getEntity();
    LivingEntity toTarget = casterToTarget ? target.getEntity() : caster.getEntity();
    forceLook(fromTarget, toTarget);
  }

  public static void forceLook(LivingEntity fromTarget, LivingEntity toTarget) {
    if (fromTarget == toTarget) {
      return;
    }
    if (fromTarget instanceof Mob) {
      ((Mob) fromTarget).lookAt(toTarget);
    } else {
      Location newLoc = fromTarget.getLocation().clone();
      Vector savedVelocity = fromTarget.getVelocity();
      newLoc.setDirection(toTarget.getLocation().subtract(fromTarget.getLocation()).toVector().normalize());
      fromTarget.teleport(newLoc, TeleportCause.PLUGIN);
      fromTarget.setVelocity(savedVelocity);
    }
  }
}
