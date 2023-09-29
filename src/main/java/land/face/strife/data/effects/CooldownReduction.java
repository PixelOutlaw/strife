package land.face.strife.data.effects;

import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.AbilitySlot;
import land.face.strife.util.LogUtil;
import lombok.Setter;

public class CooldownReduction extends Effect {

  private String abilityString;
  private AbilitySlot slot;
  @Setter
  private float amount;
  @Setter
  private boolean percent;

  public void apply(StrifeMob caster, StrifeMob target) {
    String selectedAbility = abilityString;
    if (selectedAbility == null) {
      if (slot == null) {
        LogUtil.printError("ATTENTION DINGUS!");
        LogUtil.printError("You can't cool down an ability without defining a slot or ability...");
        return;
      }
      if (caster.getChampion() == null || caster.getChampion().getSaveData().getAbility(slot) == null) {
        return;
      }
      selectedAbility = caster.getChampion().getSaveData().getAbility(slot);
    }
    if (percent) {
      StrifePlugin.getInstance().getAbilityManager().reduceCooldownPercent(caster.getEntity(),
          selectedAbility, amount);
    } else {
      StrifePlugin.getInstance().getAbilityManager().reduceCooldownMillis(caster.getEntity(),
          selectedAbility, (int) (amount * 1000));
    }
  }

  public void setAbilityString(String abilityString) {
    this.abilityString = abilityString;
  }

  public void setSlot(AbilitySlot slot) {
    this.slot = slot;
  }
}
