package info.faceland.strife.effects;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import java.util.ArrayList;
import java.util.List;

public class AreaEffect extends Effect {

  private List<Effect> cachedEffects = new ArrayList<>();
  private List<String> effects = new ArrayList<>();
  private double range;
  private boolean isLineOfSight;
  private boolean canBeEvaded;
  private boolean canBeBlocked;

  public void apply(StrifeMob caster, StrifeMob target) {
    if (canBeBlocked) {

    }
    if (canBeEvaded) {

    }
    for (Effect effect : getEffects()) {
      StrifePlugin.getInstance().getEffectManager().execute(effect, caster, target.getEntity());
    }
  }

  public List<Effect> getEffects() {
    if (cachedEffects.isEmpty()) {
      for (String effect : effects) {
        cachedEffects.add(StrifePlugin.getInstance().getEffectManager().getEffect(effect));
      }
    }
    return cachedEffects;
  }

  public void setCanBeEvaded(boolean canBeEvaded) {
    this.canBeEvaded = canBeEvaded;
  }

  public void setCanBeBlocked(boolean canBeBlocked) {
    this.canBeBlocked = canBeBlocked;
  }

  public void setEffects(List<String> effects) {
    this.effects = effects;
  }

  public boolean isLineOfSight() {
    return isLineOfSight;
  }

  public void setLineOfSight(boolean lineOfSight) {
    isLineOfSight = lineOfSight;
  }


  public double getRange() {
    return range;
  }

  public void setRange(double range) {
    this.range = range;
  }
}
