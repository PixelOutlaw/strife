package land.face.strife.timers;

import static org.bukkit.attribute.Attribute.GENERIC_FOLLOW_RANGE;

import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.ability.EntityAbilitySet.TriggerAbilityType;
import land.face.strife.util.LogUtil;
import land.face.strife.util.PlayerDataUtil;
import land.face.strife.util.TargetingUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.scheduler.BukkitRunnable;

public class EntityAbilityTimer extends BukkitRunnable {

  private final StrifeMob strifeMob;
  private boolean alwaysRun;
  private StrifePlugin plugin = StrifePlugin.getInstance();

  public EntityAbilityTimer(StrifeMob strifeMob) {
    this.strifeMob = strifeMob;
    if (strifeMob.getUniqueEntity() != null) {
      alwaysRun = strifeMob.getUniqueEntity().isAlwaysRunTimer();
    }
    runTaskTimer(StrifePlugin.getInstance(), 20L, 5L);
    LogUtil.printDebug("Created EntityAbilityTimer with id " + getTaskId());
  }

  @Override
  public void run() {
    if (strifeMob == null || strifeMob.getEntity() == null || !strifeMob.getEntity().isValid()) {
      LogUtil.printDebug("Cancelled EntityAbilityTimer  due to null entity");
      cancel();
      return;
    }
    if (alwaysRun) {
      LogUtil.printDebug("Timer for " + PlayerDataUtil.getName(strifeMob.getEntity()) + " running");
      plugin.getAbilityManager().abilityCast(strifeMob, TriggerAbilityType.TIMER);
      return;
    }
    LivingEntity target = TargetingUtil.getMobTarget(strifeMob);
    if (target == null) {
      return;
    }
    double max = Math.pow(strifeMob.getEntity().getAttribute(GENERIC_FOLLOW_RANGE).getValue(), 2);
    if (strifeMob.getEntity().getLocation().getWorld() != target.getLocation().getWorld()
        || strifeMob.getEntity().getLocation().distanceSquared(target.getLocation()) > max) {
      ((Mob) strifeMob.getEntity()).setTarget(null);
      return;
    }
    LogUtil.printDebug("Timer for " + PlayerDataUtil.getName(strifeMob.getEntity()) + " running");
    plugin.getAbilityManager().abilityCast(strifeMob, TriggerAbilityType.TIMER);
  }
}
