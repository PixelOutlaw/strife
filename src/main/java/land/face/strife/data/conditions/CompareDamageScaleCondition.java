package land.face.strife.data.conditions;

import land.face.strife.data.BonusDamage;
import land.face.strife.data.StrifeMob;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.DamageUtil.DamageScale;
import land.face.strife.util.PlayerDataUtil;
import lombok.Setter;

@Setter
public class CompareDamageScaleCondition extends Condition {

  private float amountOne;
  private float amountTwo;
  private DamageScale damageScaleOne = DamageScale.FLAT;
  private DamageScale damageScaleTwo = DamageScale.FLAT;

  public boolean isMet(StrifeMob caster, StrifeMob target) {
    BonusDamage container1 = new BonusDamage();
    container1.setDamageScale(damageScaleOne);
    container1.setAmount(amountOne);

    BonusDamage container2 = new BonusDamage();
    container2.setDamageScale(damageScaleTwo);
    container2.setAmount(amountTwo);

    float valOne = DamageUtil.applyDamageScale(caster, target, container1);
    float valTwo = DamageUtil.applyDamageScale(caster, target, container2);

    if (getCompareTarget() == CompareTarget.SELF) {
      return PlayerDataUtil.conditionCompare(getComparison(), valOne, valTwo);
    } else {
      return PlayerDataUtil.conditionCompare(getComparison(), valTwo, valOne);
    }
  }
}
