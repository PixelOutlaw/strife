package info.faceland.strife.timers;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.data.effects.Effect;
import info.faceland.strife.data.effects.EndlessEffect;
import info.faceland.strife.util.LogUtil;
import info.faceland.strife.util.PlayerDataUtil;
import org.bukkit.scheduler.BukkitRunnable;

public class EndlessEffectTimer extends BukkitRunnable {

  private StrifePlugin plugin = StrifePlugin.getInstance();
  private final StrifeMob mob;
  private final EndlessEffect endlessEffect;
  private int ticks;

  public EndlessEffectTimer(EndlessEffect effect, StrifeMob mob, int tickRate, int ticks) {
    this.endlessEffect = effect;
    this.mob = mob;
    this.ticks = ticks;
    runTaskTimer(StrifePlugin.getInstance(), 0L, tickRate);
    LogUtil.printDebug("Created EndlessEffect with id " + getTaskId());
  }

  @Override
  public void run() {
    if (mob == null) {
      LogUtil.printDebug("Cancelled endless effect due to null entity");
      cancel();
      return;
    }
    if (mob.getEntity() == null || !mob.getEntity().isValid()) {
      LogUtil.printDebug("Cancelled endless effect due to invalid entity");
      endlessEffect.removeEffectOnTarget(mob);
      cancel();
      return;
    }
    if (!endlessEffect.getCancelConditions().isEmpty() && PlayerDataUtil.areConditionsMet(mob,
        null, endlessEffect.getCancelConditions())) {
      doCancelEffects();
      return;
    }
    for (Effect effect : endlessEffect.getRunEffects()) {
      LogUtil.printDebug("Executing " + effect.getId() + " as part of " + endlessEffect.getId());
      plugin.getEffectManager().execute(effect, mob, mob.getEntity());
    }
    ticks--;
    if (ticks < 1) {
      doExpiry();
    }
  }

  private void doCancelEffects() {
    LogUtil.printDebug("Cancelled endless effect due to fail/stop conditions met");
    for (Effect effect : endlessEffect.getCancelEffects()) {
      LogUtil.printDebug("Executing " + effect.getId() + " as part of " + endlessEffect.getId());
      plugin.getEffectManager().execute(effect, mob, mob.getEntity());
    }
    endlessEffect.removeEffectOnTarget(mob);
    cancel();
  }

  public void doExpiry() {
    LogUtil.printDebug("Cancelled endless effect due to max tick duration reached");
    for (Effect effect : endlessEffect.getExpiryEffects()) {
      LogUtil.printDebug("Executing " + effect.getId() + " as part of " + endlessEffect.getId());
      plugin.getEffectManager().execute(effect, mob, mob.getEntity());
    }
    endlessEffect.removeEffectOnTarget(mob);
    cancel();
  }
}
