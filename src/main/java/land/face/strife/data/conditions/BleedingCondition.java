package land.face.strife.data.conditions;

import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.managers.BleedManager;

public class BleedingCondition extends Condition {

  private final boolean isBleeding;

  private static final BleedManager BLEED_MANAGER = StrifePlugin.getInstance().getBleedManager();

  public BleedingCondition(boolean isBleeding) {
    this.isBleeding = isBleeding;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    if (getCompareTarget() == CompareTarget.SELF) {
      return BLEED_MANAGER.isBleeding(attacker.getEntity()) == isBleeding;
    } else {
      return BLEED_MANAGER.isBleeding(target.getEntity()) == isBleeding;
    }
  }
}
