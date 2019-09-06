package info.faceland.strife.effects;

import static info.faceland.strife.stats.StrifeStat.EFFECT_DURATION;

import info.faceland.strife.conditions.Condition;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.timers.EndlessEffectTimer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EndlessEffect extends Effect {

  private final List<Effect> runEffects = new ArrayList<>();
  private final List<Effect> expiryEffects = new ArrayList<>();
  private final List<Effect> cancelEffects = new ArrayList<>();
  private final Set<Condition> cancelConditions = new HashSet<>();
  private int tickRate;
  private int maxDuration;
  private boolean strictDuration;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    float newDuration = maxDuration;
    newDuration = (newDuration * 20) / tickRate;
    if (!strictDuration) {
      newDuration = maxDuration * (1 + caster.getStat(EFFECT_DURATION) / 100);
    }
    new EndlessEffectTimer(this, caster, tickRate, (int) newDuration);
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