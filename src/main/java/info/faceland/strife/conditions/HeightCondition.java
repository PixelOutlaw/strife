package info.faceland.strife.conditions;

import info.faceland.strife.data.AttributedEntity;

public class HeightCondition implements Condition {

  private final CompareTarget compareTarget;

  public HeightCondition(CompareTarget compareTarget) {
    this.compareTarget = compareTarget;
  }

  public boolean isMet(AttributedEntity attacker, AttributedEntity target) {
    if (compareTarget == CompareTarget.SELF) {
      return attacker.getEntity().getLocation().getY() > target.getEntity().getLocation().getY();
    } else {
      return attacker.getEntity().getLocation().getY() < target.getEntity().getLocation().getY();
    }
  }
}
