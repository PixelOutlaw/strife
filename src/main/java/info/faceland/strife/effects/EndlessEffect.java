package info.faceland.strife.effects;

import static info.faceland.strife.stats.StrifeStat.EFFECT_DURATION;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.conditions.Condition;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.timers.EndlessEffectTimer;
import info.faceland.strife.util.LogUtil;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EndlessEffect extends Effect {

  private final List<String> effects = new ArrayList<>();
  private final List<Effect> cachedEffects = new ArrayList<>();
  private final Set<Condition> failConditions = new HashSet<>();
  private int tickRate;
  private int maxDuration;
  private boolean strictDuration;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    cacheEffects();
    float newDuration = maxDuration;
    newDuration = (newDuration * 20) / tickRate;
    if (!strictDuration) {
      newDuration = maxDuration * (1 + caster.getStat(EFFECT_DURATION) / 100);
    }
    new EndlessEffectTimer(this, caster, tickRate, (int) newDuration);
  }

  private void cacheEffects() {
    if (!cachedEffects.isEmpty()) {
      return;
    }
    for (String s : effects) {
      Effect effect = StrifePlugin.getInstance().getEffectManager().getEffect(s);
      if (effect == null) {
        LogUtil.printDebug("Null effect " + s + " cannot be added to EndlessEffect " + getId());
        continue;
      }
      cachedEffects.add(StrifePlugin.getInstance().getEffectManager().getEffect(s));
    }
  }

  public Set<Condition> getFailConditions() {
    return failConditions;
  }

  public List<String> getEffects() {
    return effects;
  }

  public List<Effect> getCachedEffects() {
    return cachedEffects;
  }

  public int getMaxDuration() {
    return maxDuration;
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