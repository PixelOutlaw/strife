package land.face.strife.data.conditions;

import land.face.strife.data.StrifeMob;
import land.face.strife.data.effects.EndlessEffect;

public class EndlessEffectCondition extends Condition {

  private EndlessEffect endlessEffect;
  private boolean state;

  public EndlessEffectCondition(boolean state) {
    this.state = state;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    StrifeMob finalTarget = getCompareTarget() == CompareTarget.SELF ? attacker : target;
    return state == (EndlessEffect.getEndlessEffect(finalTarget, endlessEffect) != null);
  }

  public void setEndlessEffect(EndlessEffect endlessEffect) {
    this.endlessEffect = endlessEffect;
  }
}
