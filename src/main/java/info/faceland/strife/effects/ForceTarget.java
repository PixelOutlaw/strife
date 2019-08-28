package info.faceland.strife.effects;

import info.faceland.strife.data.StrifeMob;
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
    if (((Mob) fromTarget).getTarget() == null || !((Mob) fromTarget).getTarget().isValid()) {
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
