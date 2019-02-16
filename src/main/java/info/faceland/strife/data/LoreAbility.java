package info.faceland.strife.data;

import info.faceland.strife.data.effects.Effect;
import info.faceland.strife.managers.LoreAbilityManager.TriggerType;
import java.util.ArrayList;
import java.util.List;

public class LoreAbility {

  private final TriggerType triggerType;
  private final String triggerText;
  private final List<String> description;
  private final Ability ability;
  private final List<Effect> effects;
  private boolean singleTarget;

  public LoreAbility(TriggerType triggerType, String triggerText, Ability ability,
      boolean singleTarget, List<String> description) {
    this.triggerType = triggerType;
    this.triggerText = triggerText;
    this.description = description;
    this.ability = ability;
    this.effects = new ArrayList<>();
    this.singleTarget = singleTarget;
  }

  public TriggerType getTriggerType() {
    return triggerType;
  }

  public String getTriggerText() {
    return triggerText;
  }

  public List<String> getDescription() {
    return description;
  }

  public Ability getAbility() {
    return ability;
  }

  public List<Effect> getEffects() {
    return effects;
  }

  public void addEffect(Effect effect) {
    if (!effects.contains(effect)) {
      effects.add(effect);
    }
  }

  public boolean isSingleTarget() {
    return singleTarget;
  }
}
