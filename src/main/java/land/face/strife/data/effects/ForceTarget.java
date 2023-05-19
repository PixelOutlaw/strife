package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import land.face.strife.util.TargetingUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.util.Vector;

@EqualsAndHashCode(callSuper = true)
@Data
public class ForceTarget extends Effect {

  private boolean casterToTarget;
  private boolean overwrite;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    LivingEntity fromTarget = casterToTarget ? caster.getEntity() : target.getEntity();
    LivingEntity toTarget = casterToTarget ? target.getEntity() : caster.getEntity();
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
    LookAt.forceLook(fromTarget, toTarget);
  }
}
