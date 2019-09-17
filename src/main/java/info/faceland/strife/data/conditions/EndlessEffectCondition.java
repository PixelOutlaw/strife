package info.faceland.strife.data.conditions;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.data.effects.EndlessEffect;

public class EndlessEffectCondition implements Condition {

  private final CompareTarget compareTarget;
  private EndlessEffect endlessEffect;
  private boolean state;

  public EndlessEffectCondition(CompareTarget target, boolean state) {
    this.compareTarget = target;
    this.state = state;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    StrifeMob finalTarget = compareTarget == CompareTarget.SELF ? attacker : target;
    return state == (endlessEffect.getEndlessTimer(finalTarget) != null);
  }

  public CompareTarget getCompareTarget() {
    return compareTarget;
  }

  public void setEndlessEffect(EndlessEffect endlessEffect) {
    this.endlessEffect = endlessEffect;
  }
}
