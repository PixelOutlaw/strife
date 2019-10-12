package land.face.strife.data;

import java.util.Map;
import land.face.strife.stats.StrifeStat;

public class GlobalStatBoost {

  private final String boostId;
  private final String creator;
  private final Map<StrifeStat, Float> attributes;

  private int minutesRemaining;

  public GlobalStatBoost(String boostId, String creator, Map<StrifeStat, Float> attrs,
      int minutesRemaining) {
    this.boostId = boostId;
    this.creator = creator;
    this.minutesRemaining = minutesRemaining;
    this.attributes = attrs;
  }

  public String getBoostId() {
    return boostId;
  }

  public String getCreator() {
    return creator;
  }

  public Map<StrifeStat, Float> getAttributes() {
    return attributes;
  }

  public double getAttribute(StrifeStat attribute) {
    return attributes.getOrDefault(attribute, 0f);
  }

  public int getMinutesRemaining() {
    return minutesRemaining;
  }

  public void setMinutesRemaining(int minutesRemaining) {
    this.minutesRemaining = minutesRemaining;
  }
}
