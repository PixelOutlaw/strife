package land.face.strife.data.conditions;

import java.util.Set;
import land.face.strife.data.LoreAbility;
import land.face.strife.data.StrifeMob;

public class LoreCondition extends Condition {

  private final String loreId;
  private final boolean inverted;

  public LoreCondition(String loreId, boolean inverted) {
    this.loreId = loreId;
    this.inverted = inverted;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    StrifeMob actualTarget = getCompareTarget() == CompareTarget.SELF ? attacker : target;
    if (actualTarget.getChampion() != null) {
      for (Set<LoreAbility> las : actualTarget.getChampion().getLoreAbilities().values()) {
        for (LoreAbility la : las) {
          if (loreId.equals(la.getId())) {
            return !inverted;
          }
        }
      }
      return inverted;
    }
    return false;
  }
}
