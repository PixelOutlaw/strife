package info.faceland.strife.effects;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.data.ability.Ability;
import info.faceland.strife.tasks.AbilityTickTask;

public class CooldownReduction extends Effect {

  private Ability ability;
  private String abilityString;
  private double seconds;

  public void apply(StrifeMob caster, StrifeMob target) {
    if (ability == null) {
      ability = StrifePlugin.getInstance().getAbilityManager().getAbility(abilityString);
    }
    int abilityTicks = (int) ((seconds / 20D) * AbilityTickTask.ABILITY_TICK_RATE);
    StrifePlugin.getInstance().getAbilityManager()
        .cooldownReduce(target.getEntity(), ability, abilityTicks);
  }

  public void setSeconds(double seconds) {
    this.seconds = seconds;
  }

  public void setAbilityString(String abilityString) {
    this.abilityString = abilityString;
  }
}
