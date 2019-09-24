package info.faceland.strife.data.conditions;

import info.faceland.strife.data.StrifeMob;
import org.bukkit.entity.LivingEntity;

public class BurningCondition extends Condition {

  private final boolean isBurning;

  public BurningCondition(boolean isBurning) {
    this.isBurning = isBurning;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    if (getCompareTarget() == CompareTarget.SELF) {
      return isBurning(attacker.getEntity()) == isBurning;
    } else {
      return isBurning(target.getEntity()) == isBurning;
    }
  }

  private boolean isBurning(LivingEntity livingEntity) {
    return livingEntity.getFireTicks() > 0;
  }
}
