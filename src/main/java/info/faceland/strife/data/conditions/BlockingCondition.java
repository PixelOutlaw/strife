package info.faceland.strife.data.conditions;

import info.faceland.strife.data.StrifeMob;
import org.bukkit.entity.Player;

public class BlockingCondition implements Condition {

  private final CompareTarget compareTarget;
  private final boolean state;

  public BlockingCondition(CompareTarget compareTarget, boolean state) {
    this.compareTarget = compareTarget;
    this.state = state;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    if (compareTarget == CompareTarget.SELF) {
      if (!(attacker.getEntity() instanceof Player)) {
        return false;
      }
      return ((Player) attacker.getEntity()).isBlocking() == state;
    } else {
      if (!(target.getEntity() instanceof Player)) {
        return false;
      }
      return ((Player) target.getEntity()).isBlocking() == state;
    }
  }

  public CompareTarget getCompareTarget() {
    return compareTarget;
  }
}
