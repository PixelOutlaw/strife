package land.face.strife.data.conditions;

import land.face.strife.data.StrifeMob;

public class InCombatCondition extends Condition {

  private final boolean pvpOnly;
  private final boolean state;

  public InCombatCondition(boolean state, boolean pvpOnly) {
    this.state = state;
    this.pvpOnly = pvpOnly;
  }

  public boolean isMet(StrifeMob caster, StrifeMob target) {
    StrifeMob mob = getCompareTarget() == CompareTarget.SELF ? caster : target;
    if (state != mob.isInCombat()) {
      return false;
    }
    if (pvpOnly && !mob.isInPvp()) {
      return false;
    }
    return true;
  }
}
