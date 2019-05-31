package info.faceland.strife.data;

import info.faceland.strife.attributes.StrifeAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoadedStatBoost {

  private final String creator;
  private final int duration;
  private final int announceInterval;
  private final Map<StrifeAttribute, Double> stats;
  private final List<String> announceStart;
  private final List<String> announceRun;
  private final List<String> announceEnd;

  public LoadedStatBoost(String creator, int announceInterval, int duration) {
    this.creator = creator;
    this.duration = duration;
    this.announceInterval = announceInterval;
    this.stats = new HashMap<>();
    this.announceStart = new ArrayList<>();
    this.announceRun = new ArrayList<>();
    this.announceEnd = new ArrayList<>();
  }

  public Map<StrifeAttribute, Double> getStats() {
    return stats;
  }

  public List<String> getAnnounceStart() {
    return announceStart;
  }

  public List<String> getAnnounceRun() {
    return announceRun;
  }

  public List<String> getAnnounceEnd() {
    return announceEnd;
  }

  public String getCreator() {
    return creator;
  }

  public int getDuration() {
    return duration;
  }

  public int getAnnounceInterval() {
    return announceInterval;
  }
}
