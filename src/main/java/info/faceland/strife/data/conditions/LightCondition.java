package info.faceland.strife.data.conditions;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.util.PlayerDataUtil;

public class LightCondition implements Condition {

  private final CompareTarget compareTarget;
  private final Comparison comparison;
  private final int value;

  public LightCondition(CompareTarget compareTarget, Comparison comparison, int value) {
    this.compareTarget = compareTarget;
    this.comparison = comparison;
    this.value = value;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    int lightLevel =
        compareTarget == CompareTarget.SELF ? attacker.getEntity().getLocation().getBlock()
            .getLightLevel() : target.getEntity().getLocation().getBlock().getLightLevel();
    return PlayerDataUtil.conditionCompare(comparison, lightLevel, value);
  }

  public CompareTarget getCompareTarget() {
    return compareTarget;
  }
}
