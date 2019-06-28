package info.faceland.strife.conditions;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.managers.DarknessManager;

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

  public boolean isMet(AttributedEntity attacker, AttributedEntity target) {
    double stacks;
    if (compareTarget == CompareTarget.SELF) {
      stacks = DARKNESS_MANAGER.getCorruptionStacks(attacker.getEntity());
    } else {
      stacks = DARKNESS_MANAGER.getCorruptionStacks(target.getEntity());
    }
    switch (comparison) {
      case EQUAL:
        return stacks == value;
      case LESS_THAN:
        return stacks < value;
      case GREATER_THAN:
        return stacks > value;
      default:
        return false;
    }
  }
}
