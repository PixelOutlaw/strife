package land.face.strife.data.ability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import land.face.strife.StrifePlugin;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EntityAbilitySet {

  public static final TriggerAbilityType[] TYPES = TriggerAbilityType.values();
  public static final Phase[] PHASES = Phase.values();
  @EqualsAndHashCode.Include
  private final Map<TriggerAbilityType, Map<Phase, List<Ability>>> abilityMegaMap;
  @Setter @Getter
  private Phase phase = Phase.PHASE_ONE;

  public EntityAbilitySet() {
    abilityMegaMap = new HashMap<>();
    for (TriggerAbilityType type: TYPES) {
      abilityMegaMap.put(type, new HashMap<>());
    }
  }

  public EntityAbilitySet(EntityAbilitySet abilitySet) {
    abilityMegaMap = abilitySet == null ? new HashMap<>() : abilitySet.abilityMegaMap;
  }

  public EntityAbilitySet(ConfigurationSection configurationSection) {
    abilityMegaMap = buildAbilityMap(configurationSection);
  }

  public Map<Phase, List<Ability>> getAbilities(TriggerAbilityType type) {
    return abilityMegaMap.get(type);
  }

  public static List<Ability> getAbilities(EntityAbilitySet abilitySet, TriggerAbilityType type) {
    Map<Phase, List<Ability>> abilitySection = abilitySet.getAbilities(type);
    if (abilitySection == null || !abilitySection.containsKey(abilitySet.getPhase())) {
      return null;
    }
    return abilitySection.get(abilitySet.getPhase());
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
      for (Phase phase : appliedSet.abilityMegaMap.get(type).keySet()) {
        if (originSet.abilityMegaMap.get(type).containsKey(phase)) {
          originSet.abilityMegaMap.get(type).put(phase, appliedSet.abilityMegaMap.get(type).get(phase));
          continue;
        }
        originSet.abilityMegaMap.get(type).get(phase).addAll(appliedSet.abilityMegaMap.get(type).get(phase));
      }
    }
  }

  public static Map<TriggerAbilityType, Map<Phase, List<Ability>>> buildAbilityMap(
      ConfigurationSection cs) {
    Map<TriggerAbilityType, Map<Phase, List<Ability>>> abilitySet = new HashMap<>();
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

  private static Map<Phase, List<Ability>> buildPhaseList(ConfigurationSection cs) {
    Map<Phase, List<Ability>> abilityPhaseMap = new HashMap<>();
    if (cs == null) {
      return abilityPhaseMap;
    }
    for (Phase phase : PHASES) {
      List<Ability> abilities = new ArrayList<>();
      List<String> abilityStrings = cs.getStringList(phase.toString());
      for (String s : abilityStrings) {
        abilities.add(StrifePlugin.getInstance().getAbilityManager().getAbility(s));
      }
      abilityPhaseMap.put(phase, abilities);
    }
    return abilityPhaseMap;
  }

  public static Phase phaseFromEntityHealth(LivingEntity le) {
    double percent = le.getHealth() / le.getMaxHealth();
    if (percent >= 0.799) {
      return Phase.PHASE_ONE;
    } else if (percent >= 0.599) {
      return Phase.PHASE_TWO;
    } else if (percent >= 0.399) {
      return Phase.PHASE_THREE;
    } else if (percent >= 0.199) {
      return Phase.PHASE_FOUR;
    }
    return Phase.PHASE_FIVE;
  }

  public enum TriggerAbilityType {
    ON_HIT,
    WHEN_MELEE_HIT,
    WHEN_HIT,
    WHEN_RANGED_HIT,
    PHASE_SHIFT,
    SHOOT,
    TIMER,
    DEATH
  }

  public enum Phase {
    PHASE_ONE,
    PHASE_TWO,
    PHASE_THREE,
    PHASE_FOUR,
    PHASE_FIVE
  }

}
