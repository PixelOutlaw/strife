package land.face.strife.data.conditions;

import java.util.Set;
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
      for (Set<LoreAbility> las : actualTarget.getLoreAbilities().values()) {
        for (LoreAbility la : las) {
          if (loreId.equals(la.getId())) {
            return true;
          }
        }
      }
    }
    return false;
  }
}
