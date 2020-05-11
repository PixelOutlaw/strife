package land.face.strife.data.conditions;

import java.util.Set;
import land.face.strife.data.StrifeMob;
import land.face.strife.util.LogUtil;
import me.libraryaddict.disguise.DisguiseAPI;
import org.bukkit.entity.EntityType;

public class EntityTypeCondition extends Condition {

  private final Set<EntityType> types;
  private final boolean whitelist;
  private final boolean useDisguise;

  public EntityTypeCondition(Set<EntityType> types, boolean whitelist, boolean useDisguise) {
    this.types = types;
    this.whitelist = whitelist;
    this.useDisguise = useDisguise;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    LogUtil.printDebug("EntityType condition, type=" + target.getEntity().getType());
    EntityType type;
    if (useDisguise && DisguiseAPI.isDisguised(target.getEntity())) {
      type = DisguiseAPI.getDisguise(target.getEntity()).getType().getEntityType();
    } else {
      type = target.getEntity().getType();
    }
    if (whitelist) {
      LogUtil.printDebug("EntityType condition, whitelist=true, " + types.toString());
      return types.contains(type);
    } else {
      LogUtil.printDebug("EntityType condition, whitelist=false, " + types.toString());
      return !types.contains(type);
    }
  }

  public CompareTarget getCompareTarget() {
    return CompareTarget.OTHER;
  }
}
