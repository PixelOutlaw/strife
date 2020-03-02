package land.face.strife.data.conditions;

import land.face.strife.data.StrifeMob;

public class UniqueCondition extends Condition {

  private final String uniqueId;

  public UniqueCondition(String uniqueId) {
    this.uniqueId = uniqueId;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    if (getCompareTarget() == CompareTarget.SELF) {
      target = attacker;
    }
    return uniqueId.equals(target.getUniqueEntityId());
  }
}
