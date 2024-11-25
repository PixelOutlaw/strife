package land.face.strife.data.effects;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.conditions.Condition;
import land.face.strife.stats.StrifeStat;
import land.face.strife.timers.EndlessEffectTimer;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;

@Getter
@Setter
public class EndlessEffect extends Effect {

  private final List<Effect> runEffects = new ArrayList<>();
  private final List<Effect> expiryEffects = new ArrayList<>();
  private final List<Effect> cancelEffects = new ArrayList<>();
  private final Set<Condition> cancelConditions = new HashSet<>();

  private StrifeStat reducerStat;
  private float reducerValue;
  private int tickRate;
  private float maxDuration;
  private boolean strictDuration;

  private static final Set<EndlessEffectTimer> runningEndlessEffects = new HashSet<>();

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    int newTickRate = tickRate;
    if (reducerStat != null) {
      float tickDivisor = 1 + (caster.getStat(reducerStat) / reducerValue);
      newTickRate = Math.max(1, (int) ((float) tickRate / tickDivisor));
    }
    float newDuration = maxDuration;
    if (!strictDuration) {
      newDuration *= 1 + caster.getStat(StrifeStat.EFFECT_DURATION) / 100;
    }
    newDuration = (newDuration * 20) / newTickRate;

    /*
    for (var effect : runningEndlessEffects) {
      if (effect.getEndlessEffect().getId().equals(getId()) && effect.getMob() == target) {
        effect.bump((int) newDuration);
        return;
      }
    }
    */

    EndlessEffectTimer timer = new EndlessEffectTimer(this, target, newTickRate, (int) newDuration);
    runningEndlessEffects.add(timer);
  }

  public static EndlessEffectTimer getEndlessEffect(StrifeMob mob, EndlessEffect effect) {
    for (EndlessEffectTimer timer : runningEndlessEffects) {
      if (timer.getEndlessEffect() == effect && timer.getMob() == mob) {
        return timer;
      }
    }
    return null;
  }

  public static void removeEffectOnTarget(StrifeMob target, EndlessEffect effect) {
    EndlessEffectTimer timer = getEndlessEffect(target, effect);
    if (timer != null) {
      if (!timer.isCancelled()) {
        timer.cancel();
      }
      runningEndlessEffects.remove(timer);
    }
  }

  public static void cancelEffects(LivingEntity target) {
    Iterator<EndlessEffectTimer> it = runningEndlessEffects.iterator();
    while (it.hasNext()) {
      EndlessEffectTimer timer = it.next();
      if (timer.getMob().getEntity() != target) {
        continue;
      }
      if (!timer.isCancelled()) {
        timer.cancel();
      }
      it.remove();
    }
  }
}