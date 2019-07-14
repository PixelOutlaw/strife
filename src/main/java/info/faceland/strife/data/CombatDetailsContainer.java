package info.faceland.strife.data;

import info.faceland.strife.data.champion.LifeSkillType;
import java.util.HashMap;
import java.util.Map;

public class CombatDetailsContainer {

  private Map<LifeSkillType, Float> skillWeight = new HashMap<>();
  private float totalExp = 0;

  public void addWeight(LifeSkillType type, float amount) {
    skillWeight.put(type, skillWeight.getOrDefault(type, 0f) + amount);
  }

  public void addExp(float amount) {
    totalExp += amount;
  }

  public Map<LifeSkillType, Float> getExpValues() {
    float totalWeight = 0;
    for (double amount : skillWeight.values()) {
      totalWeight += amount;
    }
    float xpTotal = totalExp;
    xpTotal *= 1 + ((double) totalWeight / 1000);
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
