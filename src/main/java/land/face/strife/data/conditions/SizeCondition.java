package land.face.strife.data.conditions;

import land.face.strife.data.StrifeMob;
import land.face.strife.util.PlayerDataUtil;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Slime;

public class SizeCondition extends Condition {

  public boolean isMet(StrifeMob caster, StrifeMob target) {
    StrifeMob actualTarget = getCompareTarget() == CompareTarget.SELF ? caster : target;
    int size = -1;
    if (actualTarget.getEntity() instanceof Slime) {
      size = ((Slime) actualTarget.getEntity()).getSize();
    } else if (actualTarget.getEntity() instanceof Phantom) {
      size = ((Phantom) actualTarget.getEntity()).getSize();
    }
    if (size == -1) {
      return false;
    }
    return PlayerDataUtil.conditionCompare(getComparison(), size, getValue());
  }
}
