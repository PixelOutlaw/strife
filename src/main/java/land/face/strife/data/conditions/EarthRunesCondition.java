package land.face.strife.data.conditions;

import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.util.PlayerDataUtil;

public class EarthRunesCondition extends Condition {

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    int runes;
    if (getCompareTarget() == CompareTarget.SELF) {
      runes = StrifePlugin.getInstance().getBlockManager().getEarthRunes(attacker.getEntity().getUniqueId());
    } else {
      runes = StrifePlugin.getInstance().getBlockManager().getEarthRunes(target.getEntity().getUniqueId());
    }
    return PlayerDataUtil.conditionCompare(getComparison(), runes, Math.round(getValue()));
  }
}
