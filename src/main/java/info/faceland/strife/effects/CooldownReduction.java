package info.faceland.strife.effects;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.data.ability.Ability;
import info.faceland.strife.stats.AbilitySlot;

public class CooldownReduction extends Effect {

  private Ability ability;
  private String abilityString;
  private AbilitySlot slot;
  private double seconds;

  public void apply(StrifeMob caster, StrifeMob target) {
    Ability selectedAbility;
    if (isForceTargetCaster()) {
      target = caster;
    }
    if (slot != null) {
      if (target.getChampion() == null
          || target.getChampion().getSaveData().getAbility(slot) == null) {
        return;
      }
      selectedAbility = target.getChampion().getSaveData().getAbility(slot);
    } else if (ability == null) {
      ability = StrifePlugin.getInstance().getAbilityManager().getAbility(abilityString);
      selectedAbility = ability;
    } else {
      selectedAbility = ability;
    }
    int abilityTicks = (int) (seconds * 20);
    StrifePlugin.getInstance().getAbilityManager()
        .cooldownReduce(target.getEntity(), selectedAbility, abilityTicks);
  }

  public void setSeconds(double seconds) {
    this.seconds = seconds;
  }

  public void setAbilityString(String abilityString) {
    this.abilityString = abilityString;
  }

  public void setSlot(AbilitySlot slot) {
    this.slot = slot;
  }
}
