package info.faceland.strife.data.condition;

import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.util.PlayerDataUtil;
import org.bukkit.entity.Player;

public class BonusLevelCondition implements Condition {

  private final Comparison comparison;
  private final int level;

  public BonusLevelCondition(Comparison comparison, int level) {
    this.comparison = comparison;
    this.level = level;
  }

  public boolean isMet(AttributedEntity attacker, AttributedEntity target) {
    if (attacker.getChampion() != null) {
      return PlayerDataUtil
          .conditionCompare(comparison, attacker.getChampion().getBonusLevels(), level);
    }
    return false;
  }
}
