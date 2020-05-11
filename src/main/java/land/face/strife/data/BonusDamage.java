package land.face.strife.data;

import land.face.strife.stats.StrifeStat;
import land.face.strife.util.DamageUtil.DamageScale;
import land.face.strife.util.DamageUtil.DamageType;

public class BonusDamage {

  private final DamageScale damageScale;
  private final DamageType damageType;
  private final StrifeStat damageStat;
  private final float amount;

  public BonusDamage(DamageScale damageScale, DamageType damageType,
      StrifeStat damageStat, float amount) {
    this.damageScale = damageScale;
    this.damageType = damageType;
    this.damageStat = damageStat;
    this.amount = amount;
  }

  public DamageScale getDamageScale() {
    return damageScale;
  }

  public DamageType getDamageType() {
    return damageType;
  }

  public StrifeStat getDamageStat() {
    return damageStat;
  }

  public float getAmount() {
    return amount;
  }

}
