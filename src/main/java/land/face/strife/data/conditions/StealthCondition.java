package land.face.strife.data.conditions;

import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import org.bukkit.entity.Player;

public class StealthCondition extends Condition {

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    if (getCompareTarget() == CompareTarget.SELF) {
      target = attacker;
    }
    return target.getEntity() instanceof Player && StrifePlugin.getInstance().getStealthManager()
        .isStealthed((Player) target.getEntity());
  }
}
