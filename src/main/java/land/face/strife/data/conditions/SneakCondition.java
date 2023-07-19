package land.face.strife.data.conditions;

import land.face.strife.data.StrifeMob;
import land.face.strife.util.PlayerDataUtil;
import org.bukkit.entity.Player;

public class SneakCondition extends Condition {

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    StrifeMob trueTarget = getCompareTarget() == CompareTarget.SELF ? attacker : target;
    if (trueTarget == null || !(trueTarget.getEntity() instanceof Player)) {
      return false;
    }
    return trueTarget.getEntity().isSneaking();
  }
}
