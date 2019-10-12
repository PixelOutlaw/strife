package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.DamageUtil.DamageScale;
import land.face.strife.util.StatUtil;

public class Bleed extends Effect {

  private float amount;
  private boolean applyBleedMods;
  private DamageScale damageScale;
  private boolean ignoreArmor;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    float bleedAmount = amount;
    for (StrifeStat attr : getStatMults().keySet()) {
      bleedAmount += getStatMults().get(attr) * caster.getStat(attr);
    }
    bleedAmount = DamageUtil.applyDamageScale(caster, target, bleedAmount, damageScale, null, null);
    if (applyBleedMods) {
      bleedAmount *= 1 + caster.getStat(StrifeStat.BLEED_DAMAGE) / 100;
      bleedAmount *= 1 - target.getStat(StrifeStat.BLEED_RESIST) / 100;
    }
    if (!ignoreArmor) {
      bleedAmount *= StatUtil.getArmorMult(caster, target);
    }
    DamageUtil.applyBleed(target, bleedAmount);
  }

  public void setDamageScale(DamageScale damageScale) {
    this.damageScale = damageScale;
  }

  public void setAmount(float amount) {
    this.amount = amount;
  }

  public void setIgnoreArmor(boolean ignoreArmor) {
    this.ignoreArmor = ignoreArmor;
  }

  public void setApplyBleedMods(boolean applyBleedMods) {
    this.applyBleedMods = applyBleedMods;
  }
}