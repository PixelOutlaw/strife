package land.face.strife.data.conditions;

import land.face.strife.data.StrifeMob;
import land.face.strife.data.effects.OrderEntityEffect;
import land.face.strife.data.pojo.OrderEntity;
import lombok.Setter;

public class OrderEntityExistsCondition extends Condition {

  @Setter
  private OrderEntityEffect effect;

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    OrderEntity orderEntity;
    if (getCompareTarget() == CompareTarget.SELF) {
      return effect.getActiveEntities().containsKey(attacker.getEntity().getUniqueId());
    } else {
      return effect.getActiveEntities().containsKey(target.getEntity().getUniqueId());
    }
  }
}
