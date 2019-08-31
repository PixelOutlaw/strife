package info.faceland.strife.data;

import java.util.UUID;

public class HitData {

  private UUID target;
  private long timeStamp;

  public HitData(UUID target, long cooldown) {
    this.target = target;
    this.timeStamp = System.currentTimeMillis() + cooldown;
  }

  public UUID getTarget() {
    return target;
  }

  public long getTimeStamp() {
    return timeStamp;
  }

  public void setTimeStamp(long timeStamp) {
    this.timeStamp = timeStamp;
  }
}
