package land.face.strife.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.effects.AreaEffect;
import land.face.strife.data.effects.TargetingComparators.DistanceComparator;
import land.face.strife.data.effects.TargetingComparators.FlatHealthComparator;
import land.face.strife.data.effects.TargetingComparators.PercentHealthComparator;
import land.face.strife.util.DamageUtil.OriginLocation;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class TargetingUtil {

  public static DistanceComparator DISTANCE_COMPARATOR = new DistanceComparator();
  private static FlatHealthComparator HEALTH_COMPARATOR = new FlatHealthComparator();
  private static PercentHealthComparator PERCENT_HEALTH_COMPARATOR = new PercentHealthComparator();

  public static void filterFriendlyEntities(Set<LivingEntity> targets, StrifeMob caster,
      boolean friendly) {
    Set<LivingEntity> friendlyEntities = getFriendlyEntities(caster, targets);
    if (friendly) {
      targets.clear();
      targets.addAll(friendlyEntities);
    } else {
      targets.removeAll(friendlyEntities);
    }
  }

  public static void filterByTargetPriority(Set<LivingEntity> areaTargets, AreaEffect effect,
      StrifeMob caster, int maxTargets) {
    if (areaTargets.isEmpty()) {
      return;
    }
    List<LivingEntity> targetList = new ArrayList<>(areaTargets);
    switch (effect.getPriority()) {
      case RANDOM:
        Collections.shuffle(targetList);
        areaTargets.retainAll(targetList.subList(0, maxTargets));
        return;
      case CLOSEST:
        DISTANCE_COMPARATOR.setLoc(caster.getEntity().getLocation());
        targetList.sort(DISTANCE_COMPARATOR);
        areaTargets.retainAll(targetList.subList(0, maxTargets));
        return;
      case FARTHEST:
        DISTANCE_COMPARATOR.setLoc(caster.getEntity().getLocation());
        targetList.sort(DISTANCE_COMPARATOR);
        areaTargets.retainAll(
            targetList.subList(targetList.size() - maxTargets, targetList.size()));
        return;
      case LEAST_HEALTH:
        targetList.sort(HEALTH_COMPARATOR);
        areaTargets.retainAll(targetList.subList(0, maxTargets));
        return;
      case MOST_HEALTH:
        targetList.sort(HEALTH_COMPARATOR);
        areaTargets.retainAll(
            targetList.subList(targetList.size() - maxTargets, targetList.size()));
        return;
      case LEAST_PERCENT_HEALTH:
        targetList.sort(PERCENT_HEALTH_COMPARATOR);
        areaTargets.retainAll(targetList.subList(0, maxTargets));
        return;
      case MOST_PERCENT_HEALTH:
        targetList.sort(PERCENT_HEALTH_COMPARATOR);
        areaTargets.retainAll(
            targetList.subList(targetList.size() - maxTargets, targetList.size()));
    }
  }

  public static Set<LivingEntity> getFriendlyEntities(StrifeMob caster, Set<LivingEntity> targets) {
    return targets.stream().filter(target -> isFriendly(caster, target))
        .collect(Collectors.toSet());
  }

  public static boolean isFriendly(StrifeMob mob, LivingEntity target) {
    return isFriendly(mob, StrifePlugin.getInstance().getStrifeMobManager().getStatMob(target));
  }

  public static boolean isFriendly(StrifeMob caster, StrifeMob target) {
    if (caster.getEntity() == target.getEntity()) {
      return true;
    }
    for (String casterFaction : caster.getFactions()) {
      for (String targetFaction: target.getFactions()) {
        if (casterFaction.equalsIgnoreCase(targetFaction)) {
          return true;
        }
      }
    }
    if (target.getMaster() != null) {
      return isFriendly(caster, target.getMaster());
    }
    if (caster.getEntity() instanceof Player && target.getEntity() instanceof Player) {
      return !DamageUtil.canAttack((Player) caster.getEntity(), (Player) target.getEntity());
    }
    for (StrifeMob mob : caster.getMinions()) {
      if (target.getEntity() == mob.getEntity()) {
        return true;
      }
    }
    // for (StrifeMob mob : getPartyMembers {
    // }
    return false;
  }

  public static Set<LivingEntity> getEntitiesInArea(LivingEntity caster, double radius) {
    Collection<Entity> targetList = Objects.requireNonNull(caster.getLocation().getWorld())
        .getNearbyEntities(caster.getEyeLocation(), radius, radius, radius);
    Set<LivingEntity> validTargets = new HashSet<>();
    for (Entity entity : targetList) {
      if (!isInvalidTarget(entity)) {
        validTargets.add((LivingEntity) entity);
      }
    }
    validTargets.removeIf(e -> !caster.hasLineOfSight(e));
    return validTargets;
  }

  public static Set<LivingEntity> getEntitiesInCone(LivingEntity originEntity, Location origin,
      Vector direction, float length, float maxConeRadius) {
    Collection<Entity> targetList = Objects.requireNonNull(origin.getWorld())
        .getNearbyEntities(origin, length, length, length);
    targetList.removeIf(TargetingUtil::isInvalidTarget);
    Set<LivingEntity> validTargets = new HashSet<>();
    for (float incRange = 0; incRange <= length + 0.01; incRange += 0.8) {
      Location loc = origin.clone().add(direction.clone().multiply(incRange));
      for (Entity entity : targetList) {
        if (entityWithinRadius(entity, loc, maxConeRadius * (incRange / length))) {
          validTargets.add((LivingEntity) entity);
        }
      }
      targetList.removeAll(validTargets);
    }
    validTargets.removeIf(e -> !originEntity.hasLineOfSight(e));
    return validTargets;
  }

  public static Set<LivingEntity> getEntitiesInLine(LivingEntity caster, double range) {
    Collection<Entity> targetList = Objects.requireNonNull(caster.getLocation().getWorld())
        .getNearbyEntities(caster.getEyeLocation(), range, range, range);
    targetList.removeIf(TargetingUtil::isInvalidTarget);
    Set<LivingEntity> validTargets = new HashSet<>();
    for (float incRange = 0; incRange <= range + 0.01; incRange += 0.8) {
      Location loc = caster.getEyeLocation().clone()
          .add(caster.getEyeLocation().getDirection().clone().multiply(incRange));
      for (Entity entity : targetList) {
        if (entityWithinRadius(entity, loc, 0)) {
          validTargets.add((LivingEntity) entity);
        }
        if (loc.getBlock().getType().isSolid()) {
          break;
        }
      }
      targetList.removeAll(validTargets);
    }
    validTargets.removeIf(e -> !caster.hasLineOfSight(e));
    return validTargets;
  }

  public static LivingEntity getFirstEntityInLine(LivingEntity caster, double range) {
    RayTraceResult result = caster.getWorld()
        .rayTraceEntities(caster.getEyeLocation(), caster.getEyeLocation().getDirection(), range,
            entity -> isValidRaycastTarget(caster, entity));
    if (result == null || result.getHitEntity() == null) {
      return null;
    }
    return (LivingEntity) result.getHitEntity();
  }

  public static boolean isInvalidTarget(Entity e) {
    if (!e.isValid() || !(e instanceof LivingEntity) || e instanceof ArmorStand) {
      return true;
    }
    if (e.hasMetadata("NPC")) {
      return true;
    }
    if (e instanceof Player) {
      return ((Player) e).getGameMode() == GameMode.CREATIVE || ((Player) e).getGameMode() == GameMode.SPECTATOR;
    }
    return false;
  }

  public static boolean isDetectionStand(LivingEntity le) {
    return le instanceof ArmorStand && le.hasMetadata("STANDO");
  }

  public static ArmorStand buildAndRemoveDetectionStand(Location location) {
    ArmorStand stando = location.getWorld().spawn(location, ArmorStand.class,
        TargetingUtil::applyDetectionStandChanges);
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), stando::remove, 1L);
    return stando;
  }

  private static void applyDetectionStandChanges(ArmorStand stando) {
    stando.setVisible(false);
    stando.setSmall(true);
    stando.setMarker(true);
    stando.setGravity(false);
    stando.setCollidable(false);
    stando.setMetadata("STANDO", new FixedMetadataValue(StrifePlugin.getInstance(), ""));
  }

  public static Set<LivingEntity> getTempStandTargetList(Location loc, float groundCheckRange) {
    Set<LivingEntity> targets = new HashSet<>();
    if (groundCheckRange < 1) {
      targets.add(TargetingUtil.buildAndRemoveDetectionStand(loc));
      return targets;
    } else {
      for (int i = 0; i < groundCheckRange; i++) {
        if (loc.getBlock().getType().isSolid()) {
          loc.setY(loc.getBlockY() + 1.3);
          targets.add(TargetingUtil.buildAndRemoveDetectionStand(loc));
          return targets;
        }
        loc.add(0, -1, 0);
      }
      return targets;
    }
  }

  private static boolean isValidRaycastTarget(LivingEntity caster, Entity entity) {
    return entity instanceof LivingEntity && entity != caster && entity.isValid() && !entity
        .hasMetadata("NPC");
  }

  private static boolean entityWithinRadius(Entity e, Location loc, float radius) {
    double ex = e.getLocation().getX();
    double ey = e.getLocation().getY();
    double ez = e.getLocation().getZ();
    double width = Math.max(e.getWidth(), 0.6);
    return Math.abs(loc.getX() - ex) < width + radius
        && Math.abs(loc.getZ() - ez) < width + radius
        && Math.abs(loc.getY() - ey) < Math.max(e.getHeight(), 1.2) + radius;
  }

  public static LivingEntity selectFirstEntityInSight(LivingEntity caster, double range) {
    LivingEntity mobTarget = TargetingUtil.getMobTarget(caster);
    return mobTarget != null ? mobTarget : getFirstEntityInLine(caster, range);
  }

  public static Location getTargetLocation(LivingEntity caster, LivingEntity target, double range,
      boolean targetEntities) {
    return getTargetLocation(caster, target, range, OriginLocation.CENTER, targetEntities);
  }

  public static Location getTargetLocation(LivingEntity caster, LivingEntity target, double range,
      OriginLocation originLocation, boolean targetEntities) {
    if (target != null && caster.getLocation().distance(target.getLocation()) < range && caster
        .hasLineOfSight(target)) {
      return getOriginLocation(target, originLocation);
    }
    RayTraceResult result;
    if (targetEntities) {
      result = caster.getWorld().rayTrace(caster.getEyeLocation(),
          caster.getEyeLocation().getDirection(), range,
          FluidCollisionMode.NEVER, true, 0.2,
          entity -> isValidRaycastTarget(caster, entity));
    } else {
      result = caster.getWorld()
          .rayTraceBlocks(caster.getEyeLocation(), caster.getEyeLocation().getDirection(), range,
              FluidCollisionMode.NEVER, true);
    }
    if (result == null) {
      LogUtil.printDebug(" - Using MAX RANGE location calculation");
      return caster.getEyeLocation().add(
          caster.getEyeLocation().getDirection().multiply(Math.max(0, range - 1)));
    }
    if (result.getHitEntity() != null) {
      LogUtil.printDebug(" - Using ENTITY location calculation");
      return getOriginLocation((LivingEntity) result.getHitEntity(), originLocation);
    }
    if (result.getHitBlock() != null) {
      LogUtil.printDebug(" - Using BLOCK location calculation");
      return result.getHitBlock().getLocation().add(0.5, 0.8, 0.5)
          .add(result.getHitBlockFace().getDirection());
    }
    LogUtil.printDebug(" - Using HIT RANGE location calculation");
    return new Location(caster.getWorld(), result.getHitPosition().getX(),
        result.getHitPosition().getBlockY(), result.getHitPosition().getZ());
  }

  public static LivingEntity getMobTarget(StrifeMob strifeMob) {
    return getMobTarget(strifeMob.getEntity());
  }

  public static LivingEntity getMobTarget(LivingEntity targeter) {
    if (!(targeter instanceof Mob)) {
      return null;
    }
    LivingEntity target = ((Mob) targeter).getTarget();
    if (target == null || !target.isValid()) {
      return null;
    }
    return target;
  }

  public static Location getOriginLocation(LivingEntity le, OriginLocation origin) {
    switch (origin) {
      case HEAD:
        return le.getEyeLocation();
      case BELOW_HEAD:
        return le.getEyeLocation().clone().add(0, -0.4, 0);
      case ABOVE_HEAD:
        return le.getEyeLocation().clone().add(0, 0.4, 0);
      case CENTER:
        Vector vec = le.getEyeLocation().toVector().subtract(le.getLocation().toVector())
            .multiply(0.5);
        return le.getLocation().clone().add(vec);
      case GROUND:
      default:
        return le.getLocation();
    }
  }
}
