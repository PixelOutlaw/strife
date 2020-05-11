package land.face.strife.data.conditions;

import java.util.HashSet;
import java.util.Set;
import land.face.strife.data.StrifeMob;
import land.face.strife.util.LogUtil;
import land.face.strife.util.PlayerDataUtil;
import land.face.strife.util.TargetingUtil;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class NearbyEntitiesCondition extends Condition {

  private final boolean friendly;
  private final int range;

  public NearbyEntitiesCondition(boolean friendly, int range) {
    this.friendly = friendly;
    this.range = range;
  }

  public boolean isMet(StrifeMob attacker, StrifeMob target) {

    LogUtil.printDebug("Nearby condition, type=" + target.getEntity().getType());

    Set<LivingEntity> entities = new HashSet<>();
    for (Entity e : target.getEntity().getWorld().getNearbyEntities(
        target.getEntity().getLocation(), range, range, range, this::isValid)) {
      entities.add((LivingEntity) e);
    }

    TargetingUtil.filterFriendlyEntities(entities, target, friendly);

    return PlayerDataUtil.conditionCompare(getComparison(), entities.size(), getValue());
  }

  private boolean isValid(Entity entity) {
    return entity.isValid() && entity instanceof LivingEntity && !(entity instanceof ArmorStand);
  }
}
