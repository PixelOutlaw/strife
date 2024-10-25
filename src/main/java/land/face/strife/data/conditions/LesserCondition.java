package land.face.strife.data.conditions;

import land.face.strife.data.StrifeMob;
import land.face.strife.util.StatUtil;
import org.bukkit.Bukkit;

public class LesserCondition extends Condition {

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    //Bukkit.getLogger().info("[str] enterlesser");
    int attackerLevel = StatUtil.getMobLevel(attacker.getEntity());
    int defenderLevel = StatUtil.getMobLevel(target.getEntity());
    //Bukkit.getLogger().info("[str] attacklv " + attackerLevel);
    //Bukkit.getLogger().info("[str] deflv " + defenderLevel);
    if (target.isPlayer()) {
      //Bukkit.getLogger().info("[str] isplayer");
      boolean result = defenderLevel + 30 < attackerLevel;
      //Bukkit.getLogger().info("return " + result);
      return result;
    }
    if (target.isElite()) {
      //Bukkit.getLogger().info("[str] targetelite return false");
      return false;
    }
    if (attackerLevel + 10 < defenderLevel) {
      //Bukkit.getLogger().info("[str] target +10 return false");
      return false;
    }
    //Bukkit.getLogger().info("[str] return true default");
    return true;
  }

  public CompareTarget getCompareTarget() {
    return CompareTarget.OTHER;
  }
}
