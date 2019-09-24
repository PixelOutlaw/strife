package info.faceland.strife.data.conditions;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.data.effects.EndlessEffect;

public class EndlessEffectCondition extends Condition {

  private EndlessEffect endlessEffect;
  private boolean state;

  public EndlessEffectCondition(boolean state) {
    this.state = state;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    StrifeMob finalTarget = getCompareTarget() == CompareTarget.SELF ? attacker : target;
    return state == (endlessEffect.getEndlessTimer(finalTarget) != null);
  }

  public void setEndlessEffect(EndlessEffect endlessEffect) {
    this.endlessEffect = endlessEffect;
  }
}
