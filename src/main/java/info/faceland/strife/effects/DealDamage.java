package info.faceland.strife.effects;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.util.DamageUtil;
import info.faceland.strife.util.DamageUtil.DamageType;
import info.faceland.strife.util.LogUtil;

public class DealDamage extends Effect {

  private double amount;
  private DamageScale damageScale;
  private DamageType damageType;
  private boolean applyEffects;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    double damage = amount;
    for (StrifeStat attr : getStatMults().keySet()) {
      damage += getStatMults().get(attr) * caster.getAttributes().getOrDefault(attr, 0D);
    }
    LogUtil.printDebug("Damage Effect! " + damage + " | " + damageScale + " | " + damageType);
    switch (damageScale) {
      case CURRENT:
        damage *= target.getEntity().getHealth() / target.getEntity().getMaxHealth();
        break;
      case MISSING:
        damage *= 1 - target.getEntity().getHealth() / target.getEntity().getMaxHealth();
        break;
      case MAXIMUM:
        damage *= target.getEntity().getMaxHealth();
        break;
    }
    LogUtil.printDebug("[Pre-Damage] Target Health: " + target.getEntity().getHealth());
    DamageUtil.dealDirectDamage(caster, target, damage, damageType);
    LogUtil.printDebug("[Post-Damage] Target Health: " + target.getEntity().getHealth());
  }

  public void setAmount(double amount) {
    this.amount = amount;
  }

  public void setDamageScale(DamageScale damageScale) {
    this.damageScale = damageScale;
  }

  public void setDamageType(DamageType damageType) {
    this.damageType = damageType;
  }

  public void setApplyEffects(boolean applyEffects) {
    this.applyEffects = applyEffects;
  }

  public enum DamageScale {
    FLAT,
    MAXIMUM,
    CURRENT,
    MISSING
  }
}