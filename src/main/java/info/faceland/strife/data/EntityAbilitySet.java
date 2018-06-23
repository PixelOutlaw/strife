package info.faceland.strife.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityAbilitySet {

  private final Map<Integer, List<Ability>> onHitAbilities;
  private final Map<Integer, List<Ability>> whenHitAbilities;
  private final Map<Integer, List<Ability>> phaseShiftAbilities;
  private final Map<Integer, List<Ability>> timerAbilities;

  public EntityAbilitySet() {
    this.onHitAbilities = new HashMap<>();
    this.whenHitAbilities = new HashMap<>();
    this.phaseShiftAbilities = new HashMap<>();
    this.timerAbilities = new HashMap<>();
  }

  public void addAbilityPhase(int phase, AbilityType type, List<Ability> abilityList) {
    switch (type) {
      case ON_HIT:
        onHitAbilities.put(phase, abilityList);
        break;
      case WHEN_HIT:
        whenHitAbilities.put(phase, abilityList);
        break;
      case TIMER:
        timerAbilities.put(phase, abilityList);
        break;
      case PHASE_SHIFT:
        phaseShiftAbilities.put(phase, abilityList);
        break;
    }
  }

  public Map<Integer, List<Ability>> getOnHitAbilities() {
    return onHitAbilities;
  }

  public Map<Integer, List<Ability>> getWhenHitAbilities() {
    return whenHitAbilities;
  }

  public Map<Integer, List<Ability>> getPhaseShiftAbilities() {
    return phaseShiftAbilities;
  }

  public Map<Integer, List<Ability>> getTimerAbilities() {
    return timerAbilities;
  }

  public enum AbilityType {
    ON_HIT,
    WHEN_HIT,
    PHASE_SHIFT,
    TIMER
  }

}
