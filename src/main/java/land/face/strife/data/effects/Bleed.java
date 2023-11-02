package land.face.strife.data.effects;

import land.face.strife.data.BonusDamage;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.DamageUtil.DamageScale;
import lombok.Setter;

public class Bleed extends Effect {

  @Setter
  private float amount;
  @Setter
  private DamageScale damageScale;
  @Setter
  private boolean ignoreArmor, ignoreResist, bypassBarrier, applyBleedMods;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    float bleedAmount = amount;
    BonusDamage container = new BonusDamage();
    container.setDamageScale(damageScale);
    container.setAmount(bleedAmount);

    bleedAmount = DamageUtil.applyDamageScale(caster, target, container);
    if (applyBleedMods) {
      bleedAmount *= 1 + caster.getStat(StrifeStat.BLEED_DAMAGE) / 100;
    }
    DamageUtil.applyBleed(caster, target, applyMultipliers(caster, bleedAmount),
        bypassBarrier, ignoreArmor, ignoreResist);
  }
}