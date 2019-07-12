package info.faceland.strife.conditions;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.managers.DarknessManager;
import info.faceland.strife.util.PlayerDataUtil;

public class CorruptionCondition implements Condition {

  private final CompareTarget compareTarget;
  private final Comparison comparison;
  private final double value;

  private static final DarknessManager DARKNESS_MANAGER = StrifePlugin.getInstance()
      .getDarknessManager();

  public CorruptionCondition(CompareTarget compareTarget, Comparison comparison, double value) {
    this.compareTarget = compareTarget;
    this.comparison = comparison;
    this.value = value;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    double stacks;
    if (compareTarget == CompareTarget.SELF) {
      stacks = DARKNESS_MANAGER.getCorruptionStacks(attacker.getEntity());
    } else {
      stacks = DARKNESS_MANAGER.getCorruptionStacks(target.getEntity());
    }
    return PlayerDataUtil.conditionCompare(comparison, stacks, value);
  }
}
