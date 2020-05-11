package land.face.strife.data.effects.TargetingComparators;

import java.util.Comparator;
import land.face.strife.data.ability.Ability;

public class AbilityComparator implements Comparator<Ability> {

  public int compare(Ability a1, Ability a2) {
    int req1 = 0;
    if (a1.getAbilityIconData() != null) {
      for (int val : a1.getAbilityIconData().getLifeSkillRequirements().values()) {
        if (val > req1) {
          req1 = val;
        }
      }
    }
    int req2 = 0;
    if (a2.getAbilityIconData() != null) {
      for (int val : a2.getAbilityIconData().getLifeSkillRequirements().values()) {
        if (val > req2) {
          req2 = val;
        }
      }
    }
    return Integer.compare(req1, req2);
  }
}
