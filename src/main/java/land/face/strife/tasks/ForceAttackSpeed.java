package land.face.strife.tasks;

import static org.bukkit.attribute.Attribute.GENERIC_ATTACK_SPEED;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ForceAttackSpeed extends BukkitRunnable {

  private static final Map<UUID, Double> ATTACK_TIMES = new HashMap<>();

  @Override
  public void run() {
    for (Player p : Bukkit.getOnlinePlayers()) {
      double attacksPerSecond = ATTACK_TIMES.getOrDefault(p.getUniqueId(), -1D);
      if (attacksPerSecond == -1) {
        continue;
      }
      for (AttributeModifier mod : p.getAttribute(GENERIC_ATTACK_SPEED).getModifiers()) {
        p.getAttribute(GENERIC_ATTACK_SPEED).removeModifier(mod);
      }
      p.getAttribute(GENERIC_ATTACK_SPEED).setBaseValue(attacksPerSecond);
    }
  }

  public static void addAttackTime(Player player, double attackTime) {
    ATTACK_TIMES.put(player.getUniqueId(), attackTime);
  }

}
