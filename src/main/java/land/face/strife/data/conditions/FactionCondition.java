package land.face.strife.data.conditions;

import land.face.strife.data.StrifeMob;

public class FactionCondition extends Condition {

  private final String factionId;

  public FactionCondition(String factionId) {
    this.factionId = factionId;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    if (getCompareTarget() == CompareTarget.SELF) {
      target = attacker;
    }
    return target.getFactions().contains(factionId);
  }
}
