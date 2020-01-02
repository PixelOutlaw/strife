package land.face.strife.data.effects;

import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;

public class UntoggleAbility extends Effect {

  private String abilityString;

  public void apply(StrifeMob caster, StrifeMob target) {
    StrifePlugin.getInstance().getAbilityManager()
        .unToggleAbility(caster, abilityString);
  }

  public void setAbilityString(String abilityString) {
    this.abilityString = abilityString;
  }

}
