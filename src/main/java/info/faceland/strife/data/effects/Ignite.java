package info.faceland.strife.data.effects;

import info.faceland.strife.data.AttributedEntity;
import org.bukkit.entity.LivingEntity;

public class Ignite extends Effect {

  private int duration = 0;

  @Override
  public void apply(AttributedEntity caster, AttributedEntity target) {
    target.getEntity().setFireTicks(Math.max(duration, target.getEntity().getFireTicks()));
  }

  public int getDuration() {
    return duration;
  }

  public void setDuration(int duration) {
    this.duration = duration;
  }

}
