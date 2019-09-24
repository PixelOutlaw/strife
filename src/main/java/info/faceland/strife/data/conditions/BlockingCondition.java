package info.faceland.strife.data.conditions;

import info.faceland.strife.data.StrifeMob;
import org.bukkit.entity.Player;

public class BlockingCondition extends Condition {

  private final boolean state;

  public BlockingCondition(boolean state) {
    this.state = state;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    if (getCompareTarget() == CompareTarget.SELF) {
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
}
