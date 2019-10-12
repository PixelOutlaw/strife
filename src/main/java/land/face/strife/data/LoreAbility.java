package land.face.strife.data;

import java.util.ArrayList;
import java.util.List;
import land.face.strife.data.ability.Ability;
import land.face.strife.data.effects.Effect;
import land.face.strife.managers.LoreAbilityManager.TriggerType;

public class LoreAbility {

  private final String id;
  private final TriggerType triggerType;
  private final String triggerText;
  private final List<String> description;
  private final Ability ability;
  private final List<Effect> effects;

  public LoreAbility(String id, TriggerType triggerType, String triggerText, Ability ability, List<String> description) {
    this.id = id;
    this.triggerType = triggerType;
    this.triggerText = triggerText;
    this.description = description;
    this.ability = ability;
    this.effects = new ArrayList<>();
  }

  public String getId() {
    return id;
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
}
