package land.face.strife.data.conditions;

import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnionCondition extends Condition {

  private final String unionId;

  public UnionCondition(String unionId) {
    this.unionId = unionId;
  }

  public boolean isMet(StrifeMob caster, StrifeMob target) {
    if (getCompareTarget() == CompareTarget.SELF) {
      return StrifePlugin.getInstance().getUnionManager().hasActiveUnion(caster, unionId);
    } else {
      return StrifePlugin.getInstance().getUnionManager().hasActiveUnion(target, unionId);
    }
  }
}
