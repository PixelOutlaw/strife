package info.faceland.strife.data.condition;

import static info.faceland.strife.data.condition.Condition.CompareTarget.*;

import info.faceland.strife.data.AttributedEntity;
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

  public boolean isMet(AttributedEntity attacker, AttributedEntity target) {
    if (compareTarget == SELF && !attacker.getEntity().hasPotionEffect(potionEffect) ||
        compareTarget == OTHER && !target.getEntity().hasPotionEffect(potionEffect)) {
      return false;
    }
    int value = compareTarget == SELF ?
        attacker.getEntity().getPotionEffect(potionEffect).getAmplifier()
        : target.getEntity().getPotionEffect(potionEffect).getAmplifier();
    return PlayerDataUtil.conditionCompare(comparison, value, intensity);
  }
}
