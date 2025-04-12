package land.face.strife.data;

import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import java.util.ArrayList;
import java.util.List;
import land.face.strife.data.ability.Ability;
import land.face.strife.data.effects.Effect;
import land.face.strife.managers.LoreAbilityManager.TriggerType;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.TextComponent;

@Getter @Setter
public class LoreAbility {

  private final String id;
  private final TriggerType triggerType;
  private final String triggerText;
  private final List<String> description;
  private final List<TextComponent> descAsComponents;
  private final List<String> rawDescription;
  private final Ability ability;
  private final List<Effect> effects;
  private final boolean cancelIfDead;
  private final boolean hide;

  public LoreAbility(String id, TriggerType triggerType, String triggerText, Ability ability,
      List<String> description, List<String> rawDescription, boolean hide, boolean cancelIfDead) {
    this.id = id;
    this.triggerType = triggerType;
    this.triggerText = triggerText;
    this.description = description;
    this.rawDescription = rawDescription;
    this.ability = ability;
    this.effects = new ArrayList<>();
    this.hide = hide;
    this.cancelIfDead = cancelIfDead;
    descAsComponents = new ArrayList<>();
    description.forEach(line -> descAsComponents.add(PaletteUtil.convert(line)));
  }

  public void addEffect(Effect effect) {
    if (!effects.contains(effect)) {
      effects.add(effect);
    }
  }
}
