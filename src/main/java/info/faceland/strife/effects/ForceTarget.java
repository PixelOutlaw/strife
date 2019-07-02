package info.faceland.strife.effects;

import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.util.LogUtil;
import org.bukkit.entity.Mob;

public class ForceTarget extends Effect {

  private boolean overwrite;

  @Override
  public void apply(AttributedEntity caster, AttributedEntity target) {
    if (caster.getEntity() instanceof Mob) {
      if (!overwrite && ((Mob) caster.getEntity()).getTarget() != null) {
        return;
      }
      ((Mob) caster.getEntity()).setTarget(target.getEntity());
    } else {
      LogUtil.printWarning(caster.getEntity().getName() + " is not a mob. Target failed.");
    }
  }

  public void setOverwrite(boolean overwrite) {
    this.overwrite = overwrite;
  }
}
