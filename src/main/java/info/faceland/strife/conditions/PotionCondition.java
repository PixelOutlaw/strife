package info.faceland.strife.conditions;

import static info.faceland.strife.conditions.Condition.CompareTarget.OTHER;
import static info.faceland.strife.conditions.Condition.CompareTarget.SELF;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.util.PlayerDataUtil;
import org.bukkit.potion.PotionEffectType;

public class PotionCondition implements Condition {

  private final CompareTarget compareTarget;
  private final Comparison comparison;
  private final PotionEffectType potionEffect;
  private final int intensity;

  public PotionCondition(PotionEffectType potionEffect, CompareTarget compareTarget,
      Comparison comparison, int intensity) {
    this.compareTarget = compareTarget;
    this.comparison = comparison;
    this.potionEffect = potionEffect;
    this.intensity = intensity;
  }

  public boolean isMet(StrifeMob caster, StrifeMob target) {
    if (compareTarget == SELF && !caster.getEntity().hasPotionEffect(potionEffect) ||
        compareTarget == OTHER && !target.getEntity().hasPotionEffect(potionEffect)) {
      return false;
    }
    int appliedIntensity;
    if (compareTarget == SELF) {
      appliedIntensity = caster.getEntity().getPotionEffect(potionEffect).getAmplifier();
    } else {
      appliedIntensity = target.getEntity().getPotionEffect(potionEffect).getAmplifier();
    }
    return PlayerDataUtil.conditionCompare(comparison, appliedIntensity, intensity);
  }
}
