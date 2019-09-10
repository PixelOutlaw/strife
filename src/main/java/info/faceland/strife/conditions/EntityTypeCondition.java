package info.faceland.strife.conditions;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.util.LogUtil;
import java.util.Set;
import org.bukkit.entity.EntityType;

public class EntityTypeCondition implements Condition {

  private final Set<EntityType> types;
  private final Boolean whitelist;

  public EntityTypeCondition(Set<EntityType> types, Boolean whitelist) {
    this.types = types;
    this.whitelist = whitelist;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    LogUtil.printDebug("EntityType condition, type=" + target.getEntity().getType());
    if (whitelist) {
      LogUtil.printDebug("EntityType condition, whitelist=true, " + types.toString());
      return types.contains(target.getEntity().getType());
    } else {
      LogUtil.printDebug("EntityType condition, whitelist=false, " + types.toString());
      return !types.contains(target.getEntity().getType());
    }
  }

  public CompareTarget getCompareTarget() {
    return CompareTarget.OTHER;
  }
}
