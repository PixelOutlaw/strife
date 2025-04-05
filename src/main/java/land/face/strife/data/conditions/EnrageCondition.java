package land.face.strife.data.conditions;

import land.face.strife.data.StrifeMob;
import lombok.Setter;

public class EnrageCondition extends Condition {

  @Setter
  private boolean onRageCooldown;

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    if (getCompareTarget() == CompareTarget.SELF) {
      target = attacker;
    }
    return onRageCooldown ? target.isOnRageCooldown() : target.isEnraged();
  }
}
