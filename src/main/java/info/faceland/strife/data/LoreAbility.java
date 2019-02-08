package info.faceland.strife.data;

import info.faceland.strife.managers.LoreAbilityManager.TriggerType;

public class LoreAbility {

  private final TriggerType triggerType;
  private final Ability ability;
  private boolean singleTarget;

  public LoreAbility(TriggerType triggerType, Ability ability, boolean singleTarget) {
    this.triggerType = triggerType;
    this.ability = ability;
    this.singleTarget = singleTarget;
  }

  public TriggerType getTriggerType() {
    return triggerType;
  }

  public Ability getAbility() {
    return ability;
  }

  public boolean isSingleTarget() {
    return singleTarget;
  }
}
