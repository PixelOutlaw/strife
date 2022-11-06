package land.face.strife.data.conditions;

import land.face.strife.data.StrifeMob;

public class HasMinionCondition extends Condition {

  private final String minionId;

  public HasMinionCondition(String minionId) {
    this.minionId = minionId;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    int count = 0;
    if (getCompareTarget() == CompareTarget.SELF) {
      for (StrifeMob minion : target.getMinions()) {
        if (minionId == null || minionId.equals(minion.getUniqueEntityId())) {
          count++;
        }
      }
    } else {
      for (StrifeMob minion : attacker.getMinions()) {
        if (minionId == null || minionId.equals(minion.getUniqueEntityId())) {
          count++;
        }
      }
    }
    return count >= (int) getValue();
  }
}
