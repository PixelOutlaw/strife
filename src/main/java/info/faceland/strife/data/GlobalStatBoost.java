package info.faceland.strife.data;

import info.faceland.strife.attributes.StrifeAttribute;
import java.util.Map;

public class GlobalStatBoost {

  private final String boostId;
  private final String creator;
  private final Map<StrifeAttribute, Double> attributes;

  private int minutesRemaining;

  public GlobalStatBoost(String boostId, String creator, Map<StrifeAttribute, Double> attrs,
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

  public Map<StrifeAttribute, Double> getAttributes() {
    return attributes;
  }

  public double getAttribute(StrifeAttribute attribute) {
    return attributes.getOrDefault(attribute, 0D);
  }

  public int getMinutesRemaining() {
    return minutesRemaining;
  }

  public void setMinutesRemaining(int minutesRemaining) {
    this.minutesRemaining = minutesRemaining;
  }
}
