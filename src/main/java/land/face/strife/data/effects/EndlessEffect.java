package land.face.strife.data.effects;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.conditions.Condition;
import land.face.strife.stats.StrifeStat;
import land.face.strife.timers.EndlessEffectTimer;

public class EndlessEffect extends Effect {

  private final List<Effect> runEffects = new ArrayList<>();
  private final List<Effect> expiryEffects = new ArrayList<>();
  private final List<Effect> cancelEffects = new ArrayList<>();
  private final Set<Condition> cancelConditions = new HashSet<>();
  private int tickRate;
  private int maxDuration;
  private boolean strictDuration;

  private final Map<StrifeMob, EndlessEffectTimer> runningEffects = new ConcurrentHashMap<>();

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    float newDuration = maxDuration;
    newDuration = (newDuration * 20) / tickRate;
    if (!strictDuration) {
      newDuration = maxDuration * (1 + caster.getStat(StrifeStat.EFFECT_DURATION) / 100);
    }
    EndlessEffectTimer timer = new EndlessEffectTimer(this, target, tickRate, (int) newDuration);
    runningEffects.put(target, timer);
  }

  public EndlessEffectTimer getEndlessTimer(StrifeMob target) {
    return runningEffects.getOrDefault(target, null);
  }

  public void removeEffectOnTarget(StrifeMob target) {
    if (runningEffects.containsKey(target)) {
      if (!runningEffects.get(target).isCancelled()) {
        runningEffects.get(target).cancel();
      }
      runningEffects.remove(target);
    }
  }

  public Set<Condition> getCancelConditions() {
    return cancelConditions;
  }

  public List<Effect> getRunEffects() {
    return runEffects;
  }

  public List<Effect> getExpiryEffects() {
    return expiryEffects;
  }

  public List<Effect> getCancelEffects() {
    return cancelEffects;
  }

  public void setMaxDuration(int maxDuration) {
    this.maxDuration = maxDuration;
  }

  public void setTickRate(int tickRate) {
    this.tickRate = tickRate;
  }

  public void setStrictDuration(boolean strictDuration) {
    this.strictDuration = strictDuration;
  }
}