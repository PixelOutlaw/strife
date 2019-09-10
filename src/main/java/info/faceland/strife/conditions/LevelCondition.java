package info.faceland.strife.conditions;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.util.PlayerDataUtil;
import org.bukkit.entity.Player;

public class LevelCondition implements Condition {

  private final Comparison comparison;
  private final int level;

  public LevelCondition(Comparison comparison, int level) {
    this.comparison = comparison;
    this.level = level;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    if (attacker.getEntity() instanceof Player) {
      return PlayerDataUtil
          .conditionCompare(comparison, ((Player) attacker.getEntity()).getLevel(), level);
    }
    return false;
  }

  public CompareTarget getCompareTarget() {
    return CompareTarget.SELF;
  }
}
