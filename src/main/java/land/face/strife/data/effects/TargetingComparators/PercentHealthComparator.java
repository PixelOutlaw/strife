package land.face.strife.data.effects.TargetingComparators;

import java.util.Comparator;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;

public class PercentHealthComparator implements Comparator<LivingEntity> {

  public int compare(LivingEntity le1, LivingEntity le2) {
    double max1 = le1.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
    double max2 = le1.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
    return Double.compare(le1.getHealth() / max1, le2.getHealth() / max2);
  }
}
