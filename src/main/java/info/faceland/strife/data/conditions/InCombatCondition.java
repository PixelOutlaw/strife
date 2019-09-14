package info.faceland.strife.data.conditions;

import static info.faceland.strife.data.conditions.Condition.CompareTarget.SELF;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.util.TargetingUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

public class InCombatCondition implements Condition {

  private final CompareTarget compareTarget;
  private final boolean state;

  public InCombatCondition(CompareTarget compareTarget, boolean state) {
    this.compareTarget = compareTarget;
    this.state = state;
  }

  public boolean isMet(StrifeMob caster, StrifeMob target) {
    LivingEntity entity = compareTarget == SELF ? caster.getEntity() : target.getEntity();
    if (entity instanceof Player) {
      return state == StrifePlugin.getInstance().getCombatStatusManager()
          .isInCombat((Player) entity);
    }
    if (entity instanceof Mob) {
      return state == (TargetingUtil.getMobTarget(entity) != null);
    }
    throw new IllegalArgumentException("Combat condition can only work on players and mobs");
  }

  public CompareTarget getCompareTarget() {
    return compareTarget;
  }
}
