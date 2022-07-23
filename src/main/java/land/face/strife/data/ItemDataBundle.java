package land.face.strife.data;

import java.util.Map;
import java.util.Set;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import lombok.Data;

@Data
public class ItemDataBundle {

  private Set<StrifeTrait> traits;
  private Map<StrifeStat, Float> stats;
  private Set<LoreAbility> abilities;

}
