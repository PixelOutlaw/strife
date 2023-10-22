package land.face.strife.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import land.face.strife.stats.StrifeStat;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LoadedStatBoost {

  private final String creator;
  private final int duration;
  private final int announceInterval;
  private final String topBarText;
  private final Map<StrifeStat, Float> stats;
  private final List<String> announceStart;
  private final List<String> announceRun;
  private final List<String> announceEnd;

  public LoadedStatBoost(String creator, String topBarText, int announceInterval, int duration) {
    this.creator = creator;
    this.duration = duration;
    this.announceInterval = announceInterval;
    this.topBarText = topBarText;
    this.stats = new HashMap<>();
    this.announceStart = new ArrayList<>();
    this.announceRun = new ArrayList<>();
    this.announceEnd = new ArrayList<>();
  }
}
