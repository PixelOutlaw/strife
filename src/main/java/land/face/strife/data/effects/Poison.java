package land.face.strife.data.effects;

import land.face.strife.data.DamageModifiers;
import land.face.strife.data.StrifeMob;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.DamageUtil.AbilityMod;
import lombok.Setter;

@Setter
public class Poison extends Effect {

  private double flatChanceBonus;
  private double durationBonus;
  private double chanceMultiplier;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    var mods = new DamageModifiers();
    mods.getAbilityMods().put(AbilityMod.POISON_CHANCE, (float) flatChanceBonus);
    mods.getAbilityMods().put(AbilityMod.POISON_CHANCE_MULTIPLIER, (float) chanceMultiplier);
    mods.getAbilityMods().put(AbilityMod.POISON_DURATION, (float) durationBonus);
    DamageUtil.attemptPoison(caster, target, mods);
  }
}
