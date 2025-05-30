package land.face.strife.data;

import java.util.Map;
import land.face.strife.stats.StrifeStat;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Boost {

  private String boostId;
  private int secondsRemaining;
  private String boosterName;
  private Map<StrifeStat, Float> stats;
}
