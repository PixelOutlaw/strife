package info.faceland.strife.data;

import info.faceland.strife.data.ability.Ability;
import info.faceland.strife.data.champion.LifeSkillType;
import java.util.HashMap;
import java.util.Map;

public class CombatDetailsContainer {

  private Map<LifeSkillType, Float> skillWeight = new HashMap<>();
  private float totalExp = 0;

  public void addWeights(Ability ability) {
    if (totalExp == 0 && !skillWeight.isEmpty()) {
      skillWeight.clear();
    }
    for (LifeSkillType type : ability.getAbilityIconData().getExpWeights().keySet()) {
      skillWeight.put(type, skillWeight.getOrDefault(type, 0f) + ability.getAbilityIconData().getExpWeights().get(type));
    }
  }

  public void addExp(float amount) {
    totalExp += amount;
  }

  public Map<LifeSkillType, Float> getExpValues() {
    if (totalExp == 0) {
      return null;
    }
    float totalWeight = 0;
    for (double amount : skillWeight.values()) {
      totalWeight += amount;
    }
    float xpTotal = totalExp;
    xpTotal += 0.05 * totalWeight;
    xpTotal *= (0.9f + Math.random() * 0.2f);
    Map<LifeSkillType, Float> rewards = new HashMap<>();
    for (LifeSkillType type : skillWeight.keySet()) {
      rewards.put(type, Math.max(1, xpTotal * (skillWeight.get(type) / totalWeight)));
    }
    return rewards;
  }

  public void clearAll() {
    totalExp = 0;
    skillWeight.clear();
  }
}
