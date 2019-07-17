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

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    double damage = amount;
    for (StrifeStat attr : getStatMults().keySet()) {
      damage += getStatMults().get(attr) * caster.getStat(attr);
    }
    LogUtil.printDebug("Damage Effect! " + damage + " | " + damageScale + " | " + damageType);
    switch (damageScale) {
      case FLAT:
        break;
      case TARGET_CURRENT_HEALTH:
        damage *= target.getEntity().getHealth() / target.getEntity().getMaxHealth();
        break;
      case CASTER_CURRENT_HEALTH:
        damage *= caster.getEntity().getHealth() / target.getEntity().getMaxHealth();
        break;
      case TARGET_MISSING_HEALTH:
        damage *= 1 - target.getEntity().getHealth() / target.getEntity().getMaxHealth();
        break;
      case CASTER_MISSING_HEALTH:
        damage *= 1 - caster.getEntity().getHealth() / caster.getEntity().getMaxHealth();
        break;
      case TARGET_MAX_HEALTH:
        damage *= target.getEntity().getMaxHealth();
        break;
      case CASTER_MAX_HEALTH:
        damage *= caster.getEntity().getMaxHealth();
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

  public enum DamageScale {
    FLAT,
    TARGET_CURRENT_HEALTH,
    CASTER_CURRENT_HEALTH,
    TARGET_MISSING_HEALTH,
    CASTER_MISSING_HEALTH,
    TARGET_MAX_HEALTH,
    CASTER_MAX_HEALTH,
    TARGET_CURRENT_BARRIER,
    CASTER_CURRENT_BARRIER,
    TARGET_MISSING_BARRIER,
    CASTER_MISSING_BARRIER,
    TARGET_MAX_BARRIER,
    CASTER_MAX_BARRIER,
  }
}