package land.face.strife.data.effects.TargetingComparators;

import java.util.Comparator;
import land.face.strife.data.ability.Ability;

public class AbilityComparator implements Comparator<Ability> {

  public int compare(Ability a1, Ability a2) {
    return Integer.compare(getAbilityLevel(a1), getAbilityLevel(a2));
  }

  private int getAbilityLevel(Ability ability) {
    int level = 0;
    if (ability.getAbilityIconData() != null) {
      for (int val : ability.getAbilityIconData().getLifeSkillRequirements().values()) {
        if (val > level) {
          level = val;
        }
      }
      level = Math.max(level, ability.getAbilityIconData().getLevelRequirement());
    }
    return level;
  }
}
