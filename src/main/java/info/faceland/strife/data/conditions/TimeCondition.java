package info.faceland.strife.data.conditions;

import info.faceland.strife.data.StrifeMob;

public class TimeCondition extends Condition {

  private long minTime;
  private long maxTime;

  public TimeCondition(long minTime, long maxTime) {
    this.minTime = minTime;
    this.maxTime = maxTime;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    long worldTime = target.getEntity().getWorld().getTime();
    if (minTime <= maxTime) {
      return worldTime >= minTime && worldTime <= maxTime;
    } else {
      return worldTime >= minTime || worldTime <= maxTime;
    }
  }

  public CompareTarget getCompareTarget() {
    return CompareTarget.SELF;
  }
}
