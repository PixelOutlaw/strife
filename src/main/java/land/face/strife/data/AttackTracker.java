package land.face.strife.data;

import lombok.Data;

@Data
public class AttackTracker {

  private long attackRechargedStamp;
  private long attackDuration;

  public AttackTracker(long attackDuration, long delay) {
    this.attackRechargedStamp = System.currentTimeMillis() + attackDuration + delay;
    this.attackDuration = attackDuration;
  }

  public void reset(long attackDuration, long delay, boolean hardReset) {
    long newStamp = System.currentTimeMillis() + attackDuration + delay;
    if (hardReset || newStamp > attackRechargedStamp) {
      attackRechargedStamp = newStamp;
      this.attackDuration = attackDuration;
    }
  }

  public float getRechargePercent() {
    long timeDiff = attackRechargedStamp - System.currentTimeMillis();
    if (timeDiff > attackDuration) {
      // This occurs only when the attack is still in the delay period
      return 0;
    }
    if (timeDiff <= 0) {
      // This occurs only when the attack is fully recharged
      return 1f;
    }
    long msIntoRecharge = attackDuration - timeDiff;
    // Don't need to care about below 0 or above 1, since they're covered
    return ((float) msIntoRecharge) / attackDuration;
  }
}
