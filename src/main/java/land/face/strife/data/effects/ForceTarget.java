package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import land.face.strife.util.TargetingUtil;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;

public class ForceTarget extends Effect {

  @Setter
  private boolean casterToTarget;
  @Setter
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
