package land.face.strife.data.effects;

import land.face.strife.data.BonusDamage;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.DamageUtil.DamageScale;
import land.face.strife.util.StatUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class Bleed extends Effect {

  private float amount;
  private boolean applyBleedMods;
  private DamageScale damageScale;
  private boolean ignoreArmor;
  private boolean ignoreResist;
  private boolean bypassBarrier;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    float bleedAmount = amount;
    BonusDamage container = new BonusDamage(damageScale, null, null, bleedAmount);
    bleedAmount = DamageUtil.applyDamageScale(caster, target, container);
    if (applyBleedMods) {
      bleedAmount *= 1 + caster.getStat(StrifeStat.BLEED_DAMAGE) / 100;
    }
    DamageUtil.applyBleed(caster, target, applyMultipliers(caster, bleedAmount),
        bypassBarrier, ignoreArmor, ignoreResist);
  }
}