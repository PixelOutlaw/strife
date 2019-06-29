package info.faceland.strife.data;

public class LastAttackTracker {

  private long lastAttackStamp;
  private long fullAttackMs;

  public LastAttackTracker(long lastAttackStamp, long fullAttackMs) {
    this.fullAttackMs = fullAttackMs;
    this.lastAttackStamp = lastAttackStamp;
  }

  public long getLastAttackStamp() {
    return lastAttackStamp;
  }

  public void setLastAttackStamp(long lastAttackStamp) {
    this.lastAttackStamp = lastAttackStamp;
  }

  public void setFullAttackMs(long fullAttackMs) {
    this.fullAttackMs = fullAttackMs;
  }

  public long getFullAttackMs() {
    return fullAttackMs;
  }
}
