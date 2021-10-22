package land.face.strife.data.effects;

import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.AbilitySlot;
import land.face.strife.util.LogUtil;

public class CooldownReduction extends Effect {

  private String abilityString;
  private AbilitySlot slot;
  private int milliseconds;

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
      selectedAbility = caster.getChampion().getSaveData().getAbility(slot).getId();
    }
    StrifePlugin.getInstance().getAbilityManager()
        .reduceCooldowns(caster.getEntity(), selectedAbility, milliseconds);
  }

  public void setSeconds(double seconds) {
    this.milliseconds = (int) (seconds * 1000);
  }

  public void setAbilityString(String abilityString) {
    this.abilityString = abilityString;
  }

  public void setSlot(AbilitySlot slot) {
    this.slot = slot;
  }
}
