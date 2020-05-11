package land.face.strife.data;

import java.util.Map;
import land.face.strife.stats.StrifeStat;

public class Boost {

  private String boostId;
  private int secondsRemaining;
  private String boosterName;
  private Map<StrifeStat, Float> stats;

  public String getBoostId() {
    return boostId;
  }

  public void setBoostId(String boostId) {
    this.boostId = boostId;
  }

  public int getSecondsRemaining() {
    return secondsRemaining;
  }

  public void setSecondsRemaining(int secondsRemaining) {
    this.secondsRemaining = secondsRemaining;
  }

  public String getBoosterName() {
    return boosterName;
  }

  public void setBoosterName(String boosterName) {
    this.boosterName = boosterName;
  }

  public Map<StrifeStat, Float> getStats() {
    return stats;
  }

  public void setStats(Map<StrifeStat, Float> stats) {
    this.stats = stats;
  }
}
