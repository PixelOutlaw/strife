package land.face.strife.data.effects;

import land.face.strife.data.BonusDamage;
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
  private boolean bypassBarrier;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    float bleedAmount = amount;
    BonusDamage container = new BonusDamage(damageScale, null, null, bleedAmount);
    bleedAmount = DamageUtil.applyDamageScale(caster, target, container);
    if (applyBleedMods) {
      bleedAmount *= 1 + caster.getStat(StrifeStat.BLEED_DAMAGE) / 100;
      bleedAmount *= 1 - target.getStat(StrifeStat.BLEED_RESIST) / 100;
    }
    if (!ignoreArmor) {
      bleedAmount *= StatUtil.getArmorMult(caster, target);
    }
    DamageUtil.applyBleed(target, applyMultipliers(caster, bleedAmount), bypassBarrier);
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

  public void setBypassBarrier(boolean bypassBarrier) {
    this.bypassBarrier = bypassBarrier;
  }

  public void setApplyBleedMods(boolean applyBleedMods) {
    this.applyBleedMods = applyBleedMods;
  }
}