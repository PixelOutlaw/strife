package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import land.face.strife.listeners.LoreAbilityListener;
import land.face.strife.managers.LoreAbilityManager.TriggerType;

public class TriggerLoreAbility extends Effect {

  public TriggerLoreAbility(TriggerType triggerType) {
    this.triggerType = triggerType;
  }

  private final TriggerType triggerType;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    LoreAbilityListener.executeBoundEffects(caster, target, caster.getLoreAbilities(triggerType));
    LoreAbilityListener.executeFiniteEffects(caster, target, triggerType);
  }

}