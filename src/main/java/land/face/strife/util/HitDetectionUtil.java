package land.face.strife.util;

import static land.face.strife.util.TargetingUtil.isFriendly;
import static land.face.strife.util.TargetingUtil.isInvalidTarget;
import static land.face.strife.util.TargetingUtil.isValidRaycastTarget;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class HitDetectionUtil {

  public static Set<LivingEntity> getEntitiesInArea(Location loc, float size) {
    float searchSize = size + 3.5f;
    Collection<Entity> targetList = loc.getWorld().getNearbyEntities(loc, searchSize, searchSize, searchSize);
    Set<LivingEntity> validTargets = new HashSet<>();
    for (Entity entity : targetList) {
      if (!isInvalidTarget(entity) && collides(loc.toVector(), size, entity)) {
        validTargets.add((LivingEntity) entity);
      }
    }
    return validTargets;
  }

  public static Set<LivingEntity> getEntitiesInCone(Location origin, Vector direction, float length,
      float maxConeRadius) {
    Collection<Entity> targetList = Objects.requireNonNull(origin.getWorld())
        .getNearbyEntities(origin, length, length, length);
    targetList.removeIf(TargetingUtil::isInvalidTarget);
    Set<LivingEntity> validTargets = new HashSet<>();
    for (float incRange = 0; incRange < length; incRange += 0.4F) {
      Location loc = origin.clone().add(direction.clone().multiply(incRange));
      for (Entity entity : targetList) {
        if (collides(loc.toVector(), 0.2f + (maxConeRadius * (incRange / length)), entity)) {
          validTargets.add((LivingEntity) entity);
        }
      }
    }
    return validTargets;
  }

  public static Set<LivingEntity> getEntitiesInLine(Location location, float range, float radius) {
    Collection<Entity> targetList = Objects.requireNonNull(location.getWorld())
        .getNearbyEntities(location, range, range, range);
    targetList.removeIf(TargetingUtil::isInvalidTarget);

    Map<BoundingBox, Entity> boundingBoxes = new HashMap<>();
    for (Entity entity : targetList) {
      boundingBoxes.put(entity.getBoundingBox().clone().expand(0.1 + radius), entity);
    }

    Location checkLocation = location.clone();
    Vector incrementVector = location.getDirection().clone().multiply(0.3);

    Set<LivingEntity> validTargets = new HashSet<>();
    for (float incRange = 0; incRange <= range; incRange += 0.3F) {
      checkLocation.add(incrementVector);
      Iterator<BoundingBox> iterator = boundingBoxes.keySet().iterator();
      while (iterator.hasNext()) {
        BoundingBox box = iterator.next();
        if (box.contains(checkLocation.toVector())) {
          validTargets.add((LivingEntity) boundingBoxes.get(box));
          iterator.remove();
        }
      }
    }
    return validTargets;
  }

  public static LivingEntity getFirstEntityInLine(LivingEntity caster, double range, boolean friendly) {
    RayTraceResult result = caster.getWorld().rayTrace(caster.getEyeLocation(), caster.getEyeLocation().getDirection(),
        range, FluidCollisionMode.NEVER, true, 0.55f, entity -> isValidRaycastTarget(caster, entity)
            && friendly == isFriendly(caster, (LivingEntity) entity));
    if (result == null || result.getHitEntity() == null) {
      return null;
    }
    if (result.getHitBlock() == null) {
      return (LivingEntity) result.getHitEntity();
    }
    if (result.getHitBlock().getLocation().clone().add(0.5, 0.5, 0.5).distanceSquared(caster.getEyeLocation()) < result
        .getHitEntity().getLocation().distanceSquared(caster.getEyeLocation())) {
      return null;
    }
    return (LivingEntity) result.getHitEntity();
  }

  public static boolean collides(Vector centerpoint, float radius, Entity e) {
    ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(e);
    if (modeledEntity == null) {
      return collides(centerpoint, radius, e.getBoundingBox());
    }
    Optional<ActiveModel> activeModel = modeledEntity.getModels().values().stream().findAny();
    if (activeModel.isPresent()) {
      return collides(centerpoint, radius, activeModel.get().getBlueprint().getMainHitbox()
          .createBoundingBox(e.getLocation().toVector()));
    }
    return collides(centerpoint, radius, e.getBoundingBox());
  }

  public static boolean collides(Vector centerpoint, float radius, BoundingBox box) {
    Vector vec = centerpoint.clone().subtract(box.getCenter());
    if (vec.getX() == 0 && vec.getY() == 0 && vec.getZ() == 0) {
      return true;
    }
    return box.rayTrace(centerpoint, vec.normalize().multiply(-1), radius) != null;
  }


  public static boolean intersects(Vector centerpoint, float radius, BoundingBox box) {
    return intersects(
        new Vector(box.getMaxX(), box.getMaxY(), box.getMaxZ()),
        new Vector(box.getMinX(), box.getMinY(), box.getMinZ()),
        centerpoint,
        radius
    );
  }

  private static boolean intersects(Vector minPoint, Vector maxPoint, Vector center, float radius) {
    double distance = Math.pow(radius, 2);
    if (center.getX() < maxPoint.getX()) {
      distance -= Math.sqrt(center.getX() - maxPoint.getX());
    } else if (center.getX() > minPoint.getX()) {
      distance -= Math.sqrt(center.getX() - minPoint.getX());
    }
    if (center.getY() < maxPoint.getY()) {
      distance -= Math.sqrt(center.getY() - maxPoint.getY());
    } else if (center.getY() > minPoint.getY()) {
      distance -= Math.sqrt(center.getY() - minPoint.getY());
    }
    if (center.getZ() < maxPoint.getZ()) {
      distance -= Math.sqrt(center.getZ() - maxPoint.getZ());
    } else if (center.getZ() > minPoint.getZ()) {
      distance -= Math.sqrt(center.getZ() - minPoint.getZ());
    }
    return distance > 0;
  }
}
