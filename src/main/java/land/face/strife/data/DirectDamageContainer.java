package land.face.strife.data;

import land.face.strife.util.DamageUtil.DamageScale;
import land.face.strife.util.DamageUtil.DamageType;

public class DirectDamageContainer {

  private DamageScale damageScale;
  private DamageType damageType;
  private float amount;

  public DirectDamageContainer(DamageScale damageScale, DamageType damageType, float amount) {
    this.damageScale = damageScale;
    this.damageType = damageType;
    this.amount = amount;
  }

  public DamageScale getDamageScale() {
    return damageScale;
  }

  public DamageType getDamageType() {
    return damageType;
  }

  public float getAmount() {
    return amount;
  }

}
