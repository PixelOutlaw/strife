package info.faceland.strife.conditions;

import static info.faceland.strife.conditions.Condition.CompareTarget.SELF;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.util.MoveUtil;
import org.bukkit.entity.Player;

public class MovingCondition implements Condition {

  private final CompareTarget compareTarget;
  private final boolean status;

  public MovingCondition(CompareTarget compareTarget, boolean status) {
    this.compareTarget = compareTarget;
    this.status = status;
  }

  public boolean isMet(StrifeMob caster, StrifeMob target) {
    if (compareTarget == SELF) {
      if (!(caster.getEntity() instanceof Player)) {
        throw new IllegalArgumentException("Move condition can only be used on players");
      }
      return MoveUtil.hasMoved((Player) caster.getEntity()) == status;
    } else {
      if (!(target.getEntity() instanceof Player)) {
        throw new IllegalArgumentException("Move condition can only be used on players");
      }
      return MoveUtil.hasMoved((Player) target.getEntity()) == status;
    }
  }
}
