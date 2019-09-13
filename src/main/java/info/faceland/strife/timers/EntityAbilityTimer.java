package info.faceland.strife.timers;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.data.ability.EntityAbilitySet.TriggerAbilityType;
import info.faceland.strife.util.LogUtil;
import info.faceland.strife.util.PlayerDataUtil;
import info.faceland.strife.util.TargetingUtil;
import org.bukkit.scheduler.BukkitRunnable;

public class EntityAbilityTimer extends BukkitRunnable {

  private StrifePlugin plugin = StrifePlugin.getInstance();
  private final StrifeMob target;

  public EntityAbilityTimer(StrifeMob target) {
    this.target = target;
    runTaskTimer(StrifePlugin.getInstance(), 0L, 15L);
    LogUtil.printDebug("Created EntityAbilityTimer with id " + getTaskId());
  }

  @Override
  public void run() {
    if (target == null || target.getEntity() == null || !target.getEntity().isValid()) {
      LogUtil.printDebug("Cancelled EntityAbilityTimer  due to null entity");
      cancel();
      return;
    }
    if (TargetingUtil.getMobTarget(target) == null) {
      return;
    }
    LogUtil.printDebug("Timer for " + PlayerDataUtil.getName(target.getEntity()) + " running");
    plugin.getAbilityManager().abilityCast(target, TriggerAbilityType.TIMER);
  }
}
