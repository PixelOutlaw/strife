package land.face.strife.data.conditions;

import land.face.strife.data.StrifeMob;
import land.face.strife.util.PlayerDataUtil;
import org.bukkit.entity.LivingEntity;

public class VelocityCondition extends Condition {

  private final VelocityType velocityType;
  private final boolean absolute;

  public VelocityCondition(VelocityType velocityType, boolean absolute) {
    this.velocityType = velocityType;
    this.absolute = absolute;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    if (getCompareTarget() == CompareTarget.SELF) {
      return velocityMatched(attacker.getEntity());
    } else {
      return velocityMatched(target.getEntity());
    }
  }

  private boolean velocityMatched(LivingEntity target) {
    double velocity = 0;
    switch (velocityType) {
      case TOTAL:
        velocity = target.getVelocity().length();
        break;
      case VERTICAL:
        velocity = target.getVelocity().getY();
        break;
      case HORIZONTAL:
        velocity = target.getVelocity().clone().setY(0).length();
        break;
    }
    if (absolute) {
      velocity = Math.abs(velocity);
    }
    return PlayerDataUtil.conditionCompare(getComparison(), velocity, getValue());
  }

  public enum VelocityType {
    VERTICAL,
    HORIZONTAL,
    TOTAL
  }
}
