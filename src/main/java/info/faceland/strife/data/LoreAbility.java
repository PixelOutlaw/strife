package info.faceland.strife.data;

import info.faceland.strife.managers.LoreAbilityManager.TriggerType;

public class LoreAbility {

  private TriggerType triggerType;
  private Ability ability;

  public LoreAbility(TriggerType triggerType, Ability ability) {
    this.triggerType = triggerType;
    this.ability = ability;
  }

  public TriggerType getTriggerType() {
    return triggerType;
  }

  public void setTriggerType(TriggerType triggerType) {
    this.triggerType = triggerType;
  }

  public Ability getAbility() {
    return ability;
  }

  public void setAbility(Ability ability) {
    this.ability = ability;
  }
}
