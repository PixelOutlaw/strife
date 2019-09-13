package info.faceland.strife.data.conditions;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.managers.BleedManager;

public class BleedingCondition implements Condition {

  private final CompareTarget compareTarget;
  private final boolean isBleeding;

  private static final BleedManager BLEED_MANAGER = StrifePlugin.getInstance().getBleedManager();

  public BleedingCondition(CompareTarget compareTarget, boolean isBleeding) {
    this.compareTarget = compareTarget;
    this.isBleeding = isBleeding;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    if (compareTarget == CompareTarget.SELF) {
      return BLEED_MANAGER.isBleeding(attacker.getEntity()) == isBleeding;
    } else {
      return BLEED_MANAGER.isBleeding(target.getEntity()) == isBleeding;
    }
  }

  public CompareTarget getCompareTarget() {
    return compareTarget;
  }
}
