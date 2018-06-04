package info.faceland.strife.data;

import info.faceland.strife.StrifePlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityAbilitySet {

    private final StrifePlugin plugin;
    private final Map<Integer, List<Ability>> onHitAbilities;
    private final Map<Integer, List<Ability>> whenHitAbilities;
    private final Map<Integer, List<Ability>> phaseShiftAbilities;
    private final Map<Integer, List<Ability>> timerAbilities;

    public EntityAbilitySet(StrifePlugin plugin) {
        this.plugin = plugin;
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

    public void execute(int phase, AbilityType type) {
        switch (type) {
            case ON_HIT:
                executeAbilities(onHitAbilities, phase);
                break;
            case WHEN_HIT:
                executeAbilities(whenHitAbilities, phase);
                break;
            case TIMER:
                executeAbilities(phaseShiftAbilities, phase);
                break;
            case PHASE_SHIFT:
                executeAbilities(timerAbilities, phase);
                break;
        }
    }

    private void executeAbilities(Map<Integer, List<Ability>> abilitySection, int phase) {
        if (abilitySection.containsKey(phase)) {
            executeAbilityList(abilitySection.get(phase));
            return;
        }
        if (phase < 5) {
            executeAbilities(abilitySection, phase + 1);
            return;
        }
        plugin.getLogger().severe("Attempted to use ability phase higher than 5? Likely a code bug...");
    }

    private void executeAbilityList(List<Ability> abilities) {
        for (Ability a : abilities) {
            //a.execute
        }
    }

    public enum AbilityType {
        ON_HIT,
        WHEN_HIT,
        PHASE_SHIFT,
        TIMER
    }

}
