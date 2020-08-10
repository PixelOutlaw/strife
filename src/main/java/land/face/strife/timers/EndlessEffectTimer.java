package land.face.strife.timers;

import java.util.HashSet;
import java.util.Set;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.TargetResponse;
import land.face.strife.data.effects.EndlessEffect;
import land.face.strife.util.LogUtil;
import land.face.strife.util.PlayerDataUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

public class EndlessEffectTimer extends BukkitRunnable {

  private final StrifePlugin plugin = StrifePlugin.getInstance();
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
      EndlessEffect.removeEffectOnTarget(mob, endlessEffect);
      cancel();
      return;
    }
    if (!endlessEffect.getCancelConditions().isEmpty()) {
      if (PlayerDataUtil.areConditionsMet(mob, mob, endlessEffect.getCancelConditions())) {
        runCancelEffects();
        cancel();
        return;
      }
    }

    Set<LivingEntity> entities = new HashSet<>();
    entities.add(mob.getEntity());
    TargetResponse response = new TargetResponse(entities);

    plugin.getEffectManager().executeEffectList(mob, response, endlessEffect.getRunEffects());

    ticks--;
    if (ticks < 1) {
      doExpiry();
    }
  }

  private void runCancelEffects() {
    LogUtil.printDebug("Cancelled endless effect due to fail/stop conditions met");

    Set<LivingEntity> entities = new HashSet<>();
    entities.add(mob.getEntity());
    TargetResponse response = new TargetResponse(entities);

    plugin.getEffectManager().processEffectList(mob, response, endlessEffect.getCancelEffects());

    EndlessEffect.removeEffectOnTarget(mob, endlessEffect);
    cancel();
  }

  public void doExpiry() {
    LogUtil.printDebug("Cancelled endless effect due to max tick duration reached");

    Set<LivingEntity> entities = new HashSet<>();
    entities.add(mob.getEntity());
    TargetResponse response = new TargetResponse(entities);

    plugin.getEffectManager().processEffectList(mob, response, endlessEffect.getExpiryEffects());

    EndlessEffect.removeEffectOnTarget(mob, endlessEffect);
  }

  public StrifeMob getMob() {
    return mob;
  }

  public EndlessEffect getEndlessEffect() {
    return endlessEffect;
  }
}
