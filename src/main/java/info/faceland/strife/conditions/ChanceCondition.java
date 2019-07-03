package info.faceland.strife.conditions;

import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.util.DamageUtil;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.potion.PotionEffectType;

public class ChanceCondition implements Condition {

  private final double chance;
  private final Map<StrifeAttribute, Double> statMults = new HashMap<>();

  public ChanceCondition(double chance) {
    this.chance = chance;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    double bonusChance = 1;
    for (StrifeAttribute attr : statMults.keySet()) {
      bonusChance += attacker.getAttribute(attr) * statMults.get(attr);
    }
    return DamageUtil.rollBool(chance * bonusChance,
        attacker.getEntity().hasPotionEffect(PotionEffectType.LUCK));
  }
}
