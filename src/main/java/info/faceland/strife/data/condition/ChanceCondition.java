package info.faceland.strife.data.condition;

import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.util.DamageUtil;
import org.bukkit.potion.PotionEffectType;

public class ChanceCondition implements Condition {

  private final double chance;
  private final Comparison comparison;

  public ChanceCondition(Comparison comparison, double chance) {
    this.comparison = comparison;
    this.chance = chance;
  }

  public boolean isMet(AttributedEntity attacker, AttributedEntity target) {
    return DamageUtil.rollBool(chance, attacker.getEntity().hasPotionEffect(PotionEffectType.LUCK));
  }
}
