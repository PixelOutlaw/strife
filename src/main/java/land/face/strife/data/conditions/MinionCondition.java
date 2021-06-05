package land.face.strife.data.conditions;

import land.face.strife.data.StrifeMob;

public class MinionCondition extends Condition {

  private final boolean isOwner;

  public MinionCondition(boolean isOwner) {
    this.isOwner = isOwner;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    if (getCompareTarget() == CompareTarget.SELF) {
      if (isOwner) {
        return target.getMinions().contains(attacker);
      }
      return attacker.getMaster() != null;
    } else {
      if (isOwner) {
        return attacker.getMinions().contains(target);
      }
      return target.getMaster() != null;
    }
  }
}
