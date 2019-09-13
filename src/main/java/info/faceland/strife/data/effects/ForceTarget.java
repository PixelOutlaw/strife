package info.faceland.strife.data.effects;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.util.TargetingUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;

public class ForceTarget extends Effect {

  private boolean casterTargetsTarget;
  private boolean overwrite;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    LivingEntity fromTarget = casterTargetsTarget ? caster.getEntity() : target.getEntity();
    LivingEntity toTarget = casterTargetsTarget ? target.getEntity() : caster.getEntity();
    if (!(fromTarget instanceof Mob)) {
      return;
    }
    if (overwrite) {
      ((Mob) fromTarget).setTarget(toTarget);
      return;
    }
    LivingEntity mobTarget = TargetingUtil.getMobTarget(fromTarget);
    if (mobTarget == null) {
      ((Mob) fromTarget).setTarget(toTarget);
    }
  }

  public void setOverwrite(boolean overwrite) {
    this.overwrite = overwrite;
  }

  public void setCasterTargetsTarget(boolean casterTargetsTarget) {
    this.casterTargetsTarget = casterTargetsTarget;
  }
}
