package land.face.strife.managers;

import java.util.HashMap;
import java.util.Map;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.util.TargetingUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class ThreatEngine {

  private static final Map<LivingEntity, Float> PENDING_THREAT = new HashMap<>();
  private static BukkitTask task;

  public static void addThreat(LivingEntity livingEntity, float amount) {
    PENDING_THREAT.put(livingEntity, PENDING_THREAT.getOrDefault(livingEntity, 0f) + amount);
  }

  public static void start(StrifePlugin plugin) {
    PENDING_THREAT.clear();
    if (task != null && !task.isCancelled()) {
      task.cancel();
    }
    task = Bukkit.getScheduler().runTaskTimer(plugin, ThreatEngine::applyThreat, 20L, 11L);
  }

  private static void applyThreat() {
    for (var pt : PENDING_THREAT.entrySet()) {
      var targets = pt.getKey().getWorld().getNearbyEntities(pt.getKey().getLocation(), 64, 64, 64);
      targets.removeIf(entity -> {
        if (!(entity instanceof LivingEntity) || entity instanceof Player) {
          return true;
        }
        if (TargetingUtil.isFriendly(pt.getKey(), (LivingEntity) entity)) {
          return true;
        }
        return false;
      });
      for (var e : targets) {
        StrifeMob mob = StrifePlugin.getInstance().getStrifeMobManager().getStatMob((LivingEntity) e);
        mob.getThreatTargets().put(pt.getKey().getUniqueId(),
            mob.getThreatTargets().getOrDefault(pt.getKey().getUniqueId(), 0f) + pt.getValue());
      }
    }
    PENDING_THREAT.clear();
  }
}