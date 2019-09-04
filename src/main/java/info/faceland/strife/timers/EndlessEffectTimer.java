package info.faceland.strife.timers;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.effects.Effect;
import info.faceland.strife.effects.EndlessEffect;
import info.faceland.strife.util.LogUtil;
import info.faceland.strife.util.PlayerDataUtil;
import org.bukkit.scheduler.BukkitRunnable;

public class EndlessEffectTimer extends BukkitRunnable {

  private StrifePlugin plugin = StrifePlugin.getInstance();
  private final StrifeMob caster;
  private final EndlessEffect endlessEffect;
  private int ticks;

  public EndlessEffectTimer(EndlessEffect effect, StrifeMob caster, int tickRate, int ticks) {
    this.endlessEffect = effect;
    this.caster = caster;
    this.ticks = ticks;
    runTaskTimer(StrifePlugin.getInstance(), 0L, tickRate);
    LogUtil.printDebug("Created EndlessEffect with id " + getTaskId());
  }

  @Override
  public void run() {
    if (caster == null || caster.getEntity() == null || !caster.getEntity().isValid()) {
      cancel();
      LogUtil.printDebug("Cancelled endless effect due to null entity");
      return;
    }
    if (!endlessEffect.getFailConditions().isEmpty() &&
        PlayerDataUtil.areConditionsMet(caster, null, endlessEffect.getFailConditions())) {
      cancel();
      LogUtil.printDebug("Cancelled endless effect due to fail/stop conditions met");
      return;
    }
    for (Effect effect : endlessEffect.getCachedEffects()) {
      LogUtil.printDebug("Executing " + effect.getId() + " as part of " + endlessEffect.getId());
      plugin.getEffectManager().execute(effect, caster, caster.getEntity());
    }
    ticks--;
    if (ticks < 1) {
      LogUtil.printDebug("Cancelled endless effect due to max tick duration reached");
      cancel();
    }
  }
}
