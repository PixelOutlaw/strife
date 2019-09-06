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
      LogUtil.printDebug("Cancelled endless effect due to null entity");
      cancel();
      return;
    }
    if (!endlessEffect.getCancelConditions().isEmpty() && PlayerDataUtil.areConditionsMet(caster,
        null, endlessEffect.getCancelConditions())) {
      doCancelEffects();
      return;
    }
    for (Effect effect : endlessEffect.getRunEffects()) {
      LogUtil.printDebug("Executing " + effect.getId() + " as part of " + endlessEffect.getId());
      plugin.getEffectManager().execute(effect, caster, caster.getEntity());
    }
    ticks--;
    if (ticks < 1) {
      doExpiry();
    }
  }

  private void doCancelEffects() {
    for (Effect effect : endlessEffect.getCancelEffects()) {
      LogUtil.printDebug("Executing " + effect.getId() + " as part of " + endlessEffect.getId());
      plugin.getEffectManager().execute(effect, caster, caster.getEntity());
    }
    cancel();
    LogUtil.printDebug("Cancelled endless effect due to fail/stop conditions met");
  }

  private void doExpiry() {
    LogUtil.printDebug("Cancelled endless effect due to max tick duration reached");
    for (Effect effect : endlessEffect.getExpiryEffects()) {
      LogUtil.printDebug("Executing " + effect.getId() + " as part of " + endlessEffect.getId());
      plugin.getEffectManager().execute(effect, caster, caster.getEntity());
    }
    cancel();
  }
}
