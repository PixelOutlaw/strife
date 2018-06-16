package info.faceland.strife.effects;

import info.faceland.strife.data.AttributedEntity;
import org.bukkit.entity.LivingEntity;

public class Ignite extends Effect {

  private int duration = 0;

  @Override
  public void apply(AttributedEntity caster, LivingEntity target) {
    target.setFireTicks(Math.max(duration, target.getFireTicks()));
  }

  public int getDuration() {
    return duration;
  }

  public void setDuration(int duration) {
    this.duration = duration;
  }

}
