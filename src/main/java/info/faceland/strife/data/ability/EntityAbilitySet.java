package info.faceland.strife.data.ability;

import info.faceland.strife.StrifePlugin;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;

public class EntityAbilitySet {

  public static final TriggerAbilityType[] TYPES = TriggerAbilityType.values();
  public static final TriggerAbilityPhase[] PHASES = TriggerAbilityPhase.values();

  private final Map<TriggerAbilityType, Map<TriggerAbilityPhase, Set<Ability>>> abilityMegaMap;
  private TriggerAbilityPhase phase = TriggerAbilityPhase.PHASE_ONE;

  public EntityAbilitySet(EntityAbilitySet abilitySet) {
    abilityMegaMap = abilitySet == null ? new HashMap<>() : abilitySet.abilityMegaMap;
  }

  public EntityAbilitySet(ConfigurationSection configurationSection) {
    abilityMegaMap = buildAbilityMap(configurationSection);
  }

  public TriggerAbilityPhase getPhase() {
    return phase;
  }

  public void setPhase(TriggerAbilityPhase phase) {
    this.phase = phase;
  }

  public Map<TriggerAbilityPhase, Set<Ability>> getAbilities(TriggerAbilityType type) {
    return abilityMegaMap.get(type);
  }

  public static void mergeAbilitySets(EntityAbilitySet appliedSet, EntityAbilitySet originSet) {
    if (appliedSet == null) {
      return;
    }
    for (TriggerAbilityType type : TYPES) {
      if (appliedSet.abilityMegaMap.get(type) == null) {
        continue;
      }
      if (originSet.abilityMegaMap.get(type) == null) {
        originSet.abilityMegaMap.put(type, appliedSet.abilityMegaMap.get(type));
        continue;
      }
      for (TriggerAbilityPhase phase : appliedSet.abilityMegaMap.get(type).keySet()) {
        if (originSet.abilityMegaMap.get(type).containsKey(phase)) {
          originSet.abilityMegaMap.get(type)
              .put(phase, appliedSet.abilityMegaMap.get(type).get(phase));
          continue;
        }
        originSet.abilityMegaMap.get(type).get(phase)
            .addAll(appliedSet.abilityMegaMap.get(type).get(phase));
      }
    }
  }

  public static Map<TriggerAbilityType, Map<TriggerAbilityPhase, Set<Ability>>> buildAbilityMap(
      ConfigurationSection cs) {
    Map<TriggerAbilityType, Map<TriggerAbilityPhase, Set<Ability>>> abilitySet = new HashMap<>();
    if (cs == null) {
      return abilitySet;
    }
    for (TriggerAbilityType abilityType : TYPES) {
      ConfigurationSection typeSection = cs.getConfigurationSection(abilityType.toString());
      if (typeSection == null) {
        continue;
      }
      abilitySet.put(abilityType, buildPhaseList(typeSection));
    }
    return abilitySet;
  }

  private static Map<TriggerAbilityPhase, Set<Ability>> buildPhaseList(ConfigurationSection cs) {
    Map<TriggerAbilityPhase, Set<Ability>> abilityPhaseMap = new HashMap<>();
    if (cs == null) {
      return abilityPhaseMap;
    }
    for (TriggerAbilityPhase phase : PHASES) {
      Set<Ability> abilities = new HashSet<>();
      List<String> abilityStrings = cs.getStringList(phase.toString());
      for (String s : abilityStrings) {
        abilities.add(StrifePlugin.getInstance().getAbilityManager().getAbility(s));
      }
      abilityPhaseMap.put(phase, abilities);
    }
    return abilityPhaseMap;
  }

  public static TriggerAbilityPhase phaseFromEntityHealth(LivingEntity le) {
    double percent = le.getHealth() / le.getMaxHealth();
    int index = 5 - (int) Math.floor(percent * 5);
    for (TriggerAbilityPhase phase : PHASES) {
      if (index == phase.ordinal()) {
        return phase;
      }
    }
    return TriggerAbilityPhase.PHASE_FIVE;
  }

  public enum TriggerAbilityType {
    ON_HIT,
    WHEN_HIT,
    PHASE_SHIFT,
    TIMER
  }

  public enum TriggerAbilityPhase {
    PHASE_ONE,
    PHASE_TWO,
    PHASE_THREE,
    PHASE_FOUR,
    PHASE_FIVE
  }

}
