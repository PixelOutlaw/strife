package info.faceland.strife.conditions;

import info.faceland.strife.data.StrifeMob;
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
    if (whitelist) {
      return types.contains(target.getEntity().getType());
    } else {
      return !types.contains(target.getEntity().getType());
    }
  }
}
