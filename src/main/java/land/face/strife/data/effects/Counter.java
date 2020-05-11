package land.face.strife.data.effects;

import java.util.ArrayList;
import java.util.List;
import land.face.strife.StrifePlugin;
import land.face.strife.data.CounterData;
import land.face.strife.data.StrifeMob;

public class Counter extends Effect {

  private int duration;
  private List<Effect> effects = new ArrayList<>();
  private boolean removeOnTrigger;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    long endStamp = System.currentTimeMillis() + duration;
    CounterData counterData = new CounterData(endStamp, new ArrayList<>(effects));
    counterData.setRemoveOnTrigger(removeOnTrigger);
    StrifePlugin.getInstance().getCounterManager().addCounter(caster.getEntity(), counterData);
  }

  public void setDuration(int duration) {
    this.duration = duration;
  }

  public List<Effect> getEffects() {
    return effects;
  }

  public void setRemoveOnTrigger(boolean removeOnTrigger) {
    this.removeOnTrigger = removeOnTrigger;
  }
}