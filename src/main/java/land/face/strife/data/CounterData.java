package land.face.strife.data;

import java.util.List;
import land.face.strife.data.effects.Effect;

public class CounterData {

  private final long endTime;
  private final List<Effect> effects;
  private boolean triggered = false;
  private boolean removeOnTrigger = false;

  public CounterData(long endTime, List<Effect> effects) {
    this.endTime = endTime;
    this.effects = effects;
  }

  public long getEndTime() {
    return endTime;
  }

  public List<Effect> getEffects() {
    return effects;
  }

  public boolean isTriggered() {
    return triggered;
  }

  public void setTriggered(boolean triggered) {
    this.triggered = triggered;
  }

  public boolean isRemoveOnTrigger() {
    return removeOnTrigger;
  }

  public void setRemoveOnTrigger(boolean removeOnTrigger) {
    this.removeOnTrigger = removeOnTrigger;
  }
}
