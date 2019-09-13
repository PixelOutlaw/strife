package info.faceland.strife.data.conditions;

import static info.faceland.strife.data.conditions.Condition.CompareTarget.SELF;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.util.MoveUtil;
import org.bukkit.entity.Player;

public class MovingCondition implements Condition {

  private final CompareTarget compareTarget;
  private final boolean state;

  public MovingCondition(CompareTarget compareTarget, boolean state) {
    this.compareTarget = compareTarget;
    this.state = state;
  }

  public boolean isMet(StrifeMob caster, StrifeMob target) {
    if (compareTarget == SELF) {
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

  public CompareTarget getCompareTarget() {
    return compareTarget;
  }
}
