package info.faceland.strife.effects;

import info.faceland.strife.data.AttributedEntity;
import org.bukkit.entity.LivingEntity;

public class Ignite extends Effect {

  private int duration = 0;

  @Override
  public void execute(AttributedEntity caster, LivingEntity target) {
    for (LivingEntity le : getTargets(caster.getEntity(), target, range)) {
      le.setFireTicks(Math.max(duration, le.getFireTicks()));
    }
  }

  public int getDuration() {
    return duration;
  }

  public void setDuration(int duration) {
    this.duration = duration;
  }

}
