package land.face.strife.data.effects.TargetingComparators;

import java.util.Comparator;
import org.bukkit.entity.LivingEntity;

public class FlatHealthComparator implements Comparator<LivingEntity> {

  public int compare(LivingEntity le1, LivingEntity le2) {
    return Double.compare(le1.getHealth(), le2.getHealth());
  }
}
