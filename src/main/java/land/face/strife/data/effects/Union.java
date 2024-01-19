package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Union extends Effect {

  private String unionId;
  private boolean disable;
  private boolean strictDuration;
  private float ticks;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (disable) {
      if (getPlugin().getUnionManager().hasActiveUnion(target)) {
        getPlugin().getUnionManager().endUnion(target, unionId);
      }
      return;
    }
    if (getPlugin().getUnionManager().hasActiveUnion(target)) {
      return;
    }
    float ticks = this.ticks;
    if (!strictDuration) {
      ticks *= (1f + caster.getStat(StrifeStat.EFFECT_DURATION) / 100);
    }
    getPlugin().getUnionManager().activateUnion(target, unionId, (int) ticks);
  }
}
