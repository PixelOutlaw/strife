package info.faceland.strife.data.conditions;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.util.PlayerDataUtil;

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
