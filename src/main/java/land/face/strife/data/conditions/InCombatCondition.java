package land.face.strife.data.conditions;

import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.util.TargetingUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;

public class InCombatCondition extends Condition {

  private final boolean state;

  public InCombatCondition(boolean state) {
    this.state = state;
  }

  public boolean isMet(StrifeMob caster, StrifeMob target) {
    LivingEntity entity = getCompareTarget() == CompareTarget.SELF ? caster.getEntity() : target.getEntity();
    if (entity instanceof Player) {
      return state == StrifePlugin.getInstance().getCombatStatusManager()
          .isInCombat((Player) entity);
    }
    if (entity instanceof Mob) {
      return state == (TargetingUtil.getMobTarget(entity) != null);
    }
    throw new IllegalArgumentException("Combat condition can only work on players and mobs");
  }
}
