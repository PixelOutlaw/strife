package info.faceland.strife.conditions;

import info.faceland.strife.data.AttributedEntity;
import java.util.Set;
import org.bukkit.entity.EntityType;

public class EntityTypeCondition implements Condition {

  private final Set<EntityType> types;
  private final Boolean whitelist;

  public EntityTypeCondition(Set<EntityType> types, Boolean whitelist) {
    this.types = types;
    this.whitelist = whitelist;
  }

  public boolean isMet(AttributedEntity attacker, AttributedEntity target) {
    if (whitelist) {
      return types.contains(target.getEntity().getType());
    } else {
      return !types.contains(target.getEntity().getType());
    }
  }
}
