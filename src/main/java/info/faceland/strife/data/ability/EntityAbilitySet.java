package info.faceland.strife.data.ability;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityAbilitySet {

  private final Map<AbilityType, Map<Integer, List<Ability>>> ABILITY_MEGA_MAP = new HashMap<>();
  private int phase;

  public int getPhase() {
    return phase;
  }

  public void setPhase(int phase) {
    this.phase = phase;
  }

  public void addAbilityPhase(int phase, AbilityType type, List<Ability> abilityList) {
    if (!ABILITY_MEGA_MAP.containsKey(type)) {
      ABILITY_MEGA_MAP.put(type, new HashMap<>());
    }
    ABILITY_MEGA_MAP.get(type).put(phase, abilityList);
  }

  public Map<Integer, List<Ability>> getAbilities(AbilityType type) {
    return ABILITY_MEGA_MAP.get(type);
  }

  public enum AbilityType {
    ON_HIT,
    WHEN_HIT,
    PHASE_SHIFT,
    TIMER
  }

}
