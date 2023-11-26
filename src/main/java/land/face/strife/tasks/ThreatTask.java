package land.face.strife.tasks;

import java.lang.ref.WeakReference;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import org.bukkit.scheduler.BukkitRunnable;

public class ThreatTask extends BukkitRunnable {

  // TICKS EVERY 40 SERVER TICKS, 2s
  private final WeakReference<StrifeMob> parentMob;

  public ThreatTask(StrifeMob parentMob) {;
    this.parentMob = new WeakReference<>(parentMob);
    runTaskTimer(StrifePlugin.getInstance(), 10L, 40L);
  }

  @Override
  public void run() {
    StrifeMob mob = parentMob.get();
    if (mob == null || mob.getEntity() == null || !mob.getEntity().isValid()) {
      cancel();
      return;
    }
    if (mob.getThreatLevel() > 0) {
      mob.setThreatLevel(Math.max(0, mob.getThreatLevel() * 0.85f - 1f));
    }
    if (!mob.isInCombat()) {
      mob.getThreatTargets().clear();
    }
  }
}
