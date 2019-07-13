package info.faceland.strife.data;

import info.faceland.strife.data.champion.LifeSkillType;
import java.util.HashMap;
import java.util.Map;

public class CombatDetailsContainer {

  private Map<LifeSkillType, Float> skillWeight = new HashMap<>();
  private float totalExp = 0;
  private int weightsAdded = 0;

  public void addWeight(LifeSkillType type, float amount) {
    skillWeight.put(type, skillWeight.getOrDefault(type, 0f) + amount);
    weightsAdded++;
  }

  public void addExp(float amount) {
    totalExp += amount;
  }

  public Map<LifeSkillType, Float> getExpValues() {
    float total = 0;
    for (double amount : skillWeight.values()) {
      total += amount;
    }
    total *= 1 + ((double) weightsAdded / 100);
    total += 0.2 * weightsAdded;
    total *= (0.9f + Math.random() * 0.2f);
    Map<LifeSkillType, Float> rewards = new HashMap<>();
    for (LifeSkillType type : skillWeight.keySet()) {
      rewards.put(type, totalExp * (skillWeight.get(type) / total));
    }
    return rewards;
  }

  public void clearAll() {
    totalExp = 0;
    weightsAdded = 0;
    skillWeight.clear();
  }
}
