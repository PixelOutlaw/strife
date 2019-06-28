package info.faceland.strife.conditions;

import info.faceland.strife.data.AttributedEntity;
import org.bukkit.entity.LivingEntity;

public class BurningCondition implements Condition {

  private final CompareTarget compareTarget;
  private final boolean isBurning;

  public BurningCondition(CompareTarget compareTarget, boolean isBurning) {
    this.compareTarget = compareTarget;
    this.isBurning = isBurning;
  }

  public boolean isMet(AttributedEntity attacker, AttributedEntity target) {
    if (compareTarget == CompareTarget.SELF) {
      return isBurning(attacker.getEntity()) == isBurning;
    } else {
      return isBurning(target.getEntity()) == isBurning;
    }
  }

  private boolean isBurning(LivingEntity livingEntity) {
    return livingEntity.getFireTicks() > 0;
  }
}
