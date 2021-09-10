package land.face.strife.data.conditions;

import land.face.strife.data.LoreAbility;
import land.face.strife.data.StrifeMob;

public class LoreCondition extends Condition {

  private final String loreId;

  public LoreCondition(String loreId) {
    this.loreId = loreId;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    StrifeMob actualTarget = getCompareTarget() == CompareTarget.SELF ? attacker : target;
    if (actualTarget.getChampion() != null) {
      for (LoreAbility la : actualTarget.getLoreAbilities()) {
        if (loreId.equals(la.getId())) {
          return true;
        }
      }
    }
    return false;
  }
}
