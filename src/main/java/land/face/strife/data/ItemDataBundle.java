package land.face.strife.data;

import java.util.Map;
import java.util.Set;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemDataBundle {

  private Set<StrifeTrait> traits;
  private Map<StrifeStat, Float> stats;
  private Set<LoreAbility> abilities;

}
