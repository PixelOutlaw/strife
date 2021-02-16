package land.face.strife.data.conditions;

import land.face.strife.data.StrifeMob;

public class InCombatCondition extends Condition {

  private final boolean state;

  public InCombatCondition(boolean state) {
    this.state = state;
  }

  public boolean isMet(StrifeMob caster, StrifeMob target) {
    StrifeMob mob = getCompareTarget() == CompareTarget.SELF ? caster : target;
    return state == mob.isInCombat();
  }
}
