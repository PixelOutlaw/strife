package info.faceland.strife.data.conditions;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.util.PlayerDataUtil;

public class EarthRunesCondition implements Condition {

  private final CompareTarget compareTarget;
  private final Comparison comparison;
  private final int value;

  public EarthRunesCondition(CompareTarget compareTarget, Comparison comparison, int value) {
    this.compareTarget = compareTarget;
    this.comparison = comparison;
    this.value = value;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    int runes;
    if (compareTarget == CompareTarget.SELF) {
      runes = StrifePlugin.getInstance().getBlockManager().getEarthRunes(attacker.getEntity().getUniqueId());
    } else {
      runes = StrifePlugin.getInstance().getBlockManager().getEarthRunes(target.getEntity().getUniqueId());
    }
    return PlayerDataUtil.conditionCompare(comparison, runes, value);
  }

  public CompareTarget getCompareTarget() {
    return compareTarget;
  }
}
