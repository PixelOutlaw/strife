package land.face.strife.data.effects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import land.face.strife.data.StrifeMob;
import lombok.Setter;

public class MinionRemove extends Effect {

  @Setter
  private String uniqueEntity;
  @Setter
  private int amount;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    List<StrifeMob> sortedMinions = new ArrayList<>(target.getMinions());
    if (uniqueEntity != null) {
      sortedMinions.removeIf(e -> !uniqueEntity.equals(e.getUniqueEntityId()));
    }
    sortedMinions.sort(Comparator.comparingDouble(StrifeMob::getMinionRating));
    Collections.reverse(sortedMinions);
    int removed = 0;
    while (removed < amount && sortedMinions.size() > 0) {
      sortedMinions.get(removed).getEntity().remove();
      sortedMinions.remove(removed);
      removed++;
    }
  }
}
