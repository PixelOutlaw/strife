package land.face.strife.data.conditions;

import static land.face.strife.data.conditions.Condition.CompareTarget.OTHER;
import static land.face.strife.data.conditions.Condition.CompareTarget.SELF;

import land.face.strife.data.StrifeMob;
import land.face.strife.util.PlayerDataUtil;
import org.bukkit.potion.PotionEffectType;

public class PotionCondition extends Condition {

  private final PotionEffectType potionEffect;
  private final int intensity;

  public PotionCondition(PotionEffectType potionEffect, int intensity) {
    this.potionEffect = potionEffect;
    this.intensity = intensity;
  }

  public boolean isMet(StrifeMob caster, StrifeMob target) {
    if (getCompareTarget() == SELF && !caster.getEntity().hasPotionEffect(potionEffect) ||
        getCompareTarget() == OTHER && !target.getEntity().hasPotionEffect(potionEffect)) {
      return false;
    }
    int appliedIntensity;
    if (getCompareTarget() == SELF) {
      appliedIntensity = caster.getEntity().getPotionEffect(potionEffect).getAmplifier();
    } else {
      appliedIntensity = target.getEntity().getPotionEffect(potionEffect).getAmplifier();
    }
    return PlayerDataUtil.conditionCompare(getComparison(), appliedIntensity, intensity);
  }
}
