package info.faceland.strife.effects;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.util.DamageUtil;
import info.faceland.strife.util.StatUtil;

public class Bleed extends Effect {

  private double amount;
  private boolean applyBleedMods;
  private boolean ignoreArmor;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    double bleedAmount = amount;
    if (applyBleedMods) {
      bleedAmount *= 1 + caster.getStat(StrifeStat.BLEED_DAMAGE) / 100;
      bleedAmount *= 1 - target.getStat(StrifeStat.BLEED_RESIST) / 100;
    }
    for (StrifeStat attr : getStatMults().keySet()) {
      bleedAmount *= 1 + (getStatMults().get(attr) * caster.getStat(attr));
    }
    if (!ignoreArmor) {
      bleedAmount *= StatUtil.getArmorMult(caster, target);
    }
    DamageUtil.applyBleed(target.getEntity(), bleedAmount);
  }

  public void setAmount(double amount) {
    this.amount = amount;
  }

  public void setIgnoreArmor(boolean ignoreArmor) {
    this.ignoreArmor = ignoreArmor;
  }

  public void setApplyBleedMods(boolean applyBleedMods) {
    this.applyBleedMods = applyBleedMods;
  }
}