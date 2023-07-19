package land.face.strife.data.conditions;

import land.face.strife.data.StrifeMob;
import land.face.strife.data.effects.OrderEntityEffect;
import land.face.strife.data.pojo.OrderEntity;
import lombok.Setter;

public class OrderEntityCanOrderCondition extends Condition {

  @Setter
  private OrderEntityEffect effect;

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    OrderEntity orderEntity;
    if (getCompareTarget() == CompareTarget.SELF) {
      orderEntity = effect.getActiveEntities().get(attacker.getEntity().getUniqueId());
    } else {
      orderEntity = effect.getActiveEntities().get(target.getEntity().getUniqueId());
    }
    if (orderEntity == null) {
      return true;
    }
    return orderEntity.isIdle();
  }
}
