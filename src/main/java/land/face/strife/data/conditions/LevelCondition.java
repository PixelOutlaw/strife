package land.face.strife.data.conditions;

import land.face.strife.data.StrifeMob;
import land.face.strife.util.PlayerDataUtil;
import org.bukkit.entity.Player;

public class LevelCondition extends Condition {

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    if (attacker.getEntity() instanceof Player) {
      return PlayerDataUtil.conditionCompare(getComparison(),
          ((Player) attacker.getEntity()).getLevel(), Math.round(getValue()));
    }
    return false;
  }

  public CompareTarget getCompareTarget() {
    return CompareTarget.SELF;
  }
}
