package info.faceland.strife.data.effects;

import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.util.LogUtil;
import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;

public class ForceTarget extends Effect {

  private boolean overwrite;

  @Override
  public void apply(AttributedEntity caster, LivingEntity target) {
    if (caster.getEntity() instanceof Creature) {
      if (!overwrite && ((Creature) caster.getEntity()).getTarget() != null) {
        return;
      }
      ((Creature) caster.getEntity()).setTarget(target);
    } else {
      LogUtil.printWarning(caster.getEntity().getName() + " is not a creature. Target failed.");
    }
  }

  public void setOverwrite(boolean overwrite) {
    this.overwrite = overwrite;
  }
}
