package info.faceland.strife.data.conditions;

import static info.faceland.strife.data.conditions.Condition.CompareTarget.SELF;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.util.MoveUtil;
import org.bukkit.entity.Player;

public class MovingCondition extends Condition {

  private final boolean state;

  public MovingCondition(boolean state) {
    this.state = state;
  }

  public boolean isMet(StrifeMob caster, StrifeMob target) {
    if (getCompareTarget() == SELF) {
      if (!(caster.getEntity() instanceof Player)) {
        throw new IllegalArgumentException("Move condition can only be used on players");
      }
      return MoveUtil.hasMoved((Player) caster.getEntity()) == state;
    } else {
      if (!(target.getEntity() instanceof Player)) {
        throw new IllegalArgumentException("Move condition can only be used on players");
      }
      return MoveUtil.hasMoved((Player) target.getEntity()) == state;
    }
  }
}
