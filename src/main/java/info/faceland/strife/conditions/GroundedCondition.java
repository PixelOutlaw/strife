package info.faceland.strife.conditions;

import info.faceland.strife.data.StrifeMob;

public class GroundedCondition implements Condition {

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    return target.getEntity().isOnGround();
  }
}
