package land.face.strife.data.conditions;

import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.DamageUtil;
import org.bukkit.potion.PotionEffectType;

public class ChanceCondition extends Condition {

  private final float chance;

  public ChanceCondition(float chance) {
    this.chance = chance;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    float bonusChance = 1f;
    for (StrifeStat attr : getStatMults().keySet()) {
      bonusChance += attacker.getStat(attr) * getStatMults().get(attr);
    }
    return DamageUtil.rollBool(chance * bonusChance,
        attacker.getEntity().hasPotionEffect(PotionEffectType.LUCK));
  }

  public CompareTarget getCompareTarget() {
    return CompareTarget.SELF;
  }
}
