package land.face.strife.util;

import static org.bukkit.attribute.Attribute.GENERIC_FOLLOW_RANGE;

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
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class TargetingUtil {

  public static DistanceComparator DISTANCE_COMPARATOR = new DistanceComparator();
  private static FlatHealthComparator HEALTH_COMPARATOR = new FlatHealthComparator();
  private static PercentHealthComparator PERCENT_HEALTH_COMPARATOR = new PercentHealthComparator();

  public static void expandMobRange(LivingEntity attacker, LivingEntity victim) {
    if (!(victim instanceof Mob)) {
      return;
    }
    AttributeInstance attr = victim.getAttribute(GENERIC_FOLLOW_RANGE);
    double newVal = Math.max(Math.max(attr.getBaseValue(), attr.getDefaultValue()), 32);
    victim.getAttribute(GENERIC_FOLLOW_RANGE).setBaseValue(newVal);

    LivingEntity target = ((Mob) victim).getTarget();
    if (target == null || !target.isValid()) {
      ((Mob) victim).setTarget(attacker);
    }
  }

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

  public static boolean isFriendly(LivingEntity attacker, LivingEntity defender) {
    return isFriendly(StrifePlugin.getInstance().getStrifeMobManager().getStatMob(attacker),
        StrifePlugin.getInstance().getStrifeMobManager().getStatMob(defender));
  }

  public static boolean isFriendly(StrifeMob attacker, LivingEntity defender) {
    return isFriendly(attacker,
        StrifePlugin.getInstance().getStrifeMobManager().getStatMob(defender));
  }

  public static boolean isFriendly(LivingEntity attacker, StrifeMob defender) {
    return isFriendly(StrifePlugin.getInstance().getStrifeMobManager().getStatMob(attacker),
        defender);
  }

  public static boolean isFriendly(StrifeMob attacker, StrifeMob defender) {
    if (attacker.getEntity() == defender.getEntity()) {
      return true;
    }
    if (defender.getEntity() instanceof Tameable) {
      return isFriendly(attacker, (LivingEntity) Bukkit.getEntity(((Tameable) attacker.getEntity()).getOwnerUniqueId()));
    }
    for (String casterFaction : attacker.getFactions()) {
      for (String targetFaction : defender.getFactions()) {
        if (casterFaction.equalsIgnoreCase(targetFaction)) {
          return true;
        }
      }
    }
    if (attacker.getMaster() != null) {
      return isFriendly(attacker.getMaster(), defender);
    }
    if (defender.getMaster() != null) {
      return isFriendly(attacker, defender.getMaster());
    }
    if (attacker.getEntity() instanceof Player && defender.getEntity() instanceof Player) {
      return !DamageUtil.canAttack((Player) attacker.getEntity(), (Player) defender.getEntity());
    }
    for (StrifeMob mob : attacker.getMinions()) {
      if (defender.getEntity() == mob.getEntity()) {
        return true;
      }
    }
    return false;
  }

  public static Set<LivingEntity> getEntitiesInArea(Location location, double radius) {
    Collection<Entity> targetList = Objects.requireNonNull(location.getWorld())
        .getNearbyEntities(location, radius, radius, radius);
    Set<LivingEntity> validTargets = new HashSet<>();
    for (Entity entity : targetList) {
      if (!isInvalidTarget(entity)) {
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
    for (float incRange = 0; incRange <= length + 0.01; incRange += 0.8) {
      Location loc = origin.clone().add(direction.clone().multiply(incRange));
      for (Entity entity : targetList) {
        if (entityWithinRadius(entity, loc, maxConeRadius * (incRange / length))) {
          validTargets.add((LivingEntity) entity);
        }
      }
    }
    return validTargets;
  }

  public static Set<LivingEntity> getEntitiesInLine(Location location, float range) {
    Collection<Entity> targetList = Objects.requireNonNull(location.getWorld())
        .getNearbyEntities(location, range, range, range);
    targetList.removeIf(TargetingUtil::isInvalidTarget);
    Set<LivingEntity> validTargets = new HashSet<>();
    for (float incRange = 0; incRange <= range + 0.01; incRange += 0.8) {
      Location loc = location.clone().add(location.getDirection().multiply(incRange));
      for (Entity entity : targetList) {
        if (entityWithinRadius(entity, loc, 0.2f)) {
          validTargets.add((LivingEntity) entity);
        }
      }
    }
    return validTargets;
  }

  public static boolean hasLineOfSight(Location start, Location target, Entity entity) {
    if (start.getWorld() != target.getWorld()) {
      return false;
    }
    Vector direction = target.toVector().subtract(start.toVector());
    if (direction.length() < 0.05) {
      return true;
    }
    direction.normalize();
    RayTraceResult result = start.getWorld()
        .rayTrace(start, direction, 40, FluidCollisionMode.NEVER, true, 0.5, e -> e == entity);
    if (result == null) {
      return false;
    }
    //if (result.getHitBlock() != null) {
    //  Location particleLoc = result.getHitPosition().toLocation(start.getWorld());
    //  particleLoc.getWorld().spawnParticle(
    //      Particle.ITEM_CRACK,
    //      particleLoc,
    //      3,
    //      0.0, 0.0, 0.0,
    //      0.1,
    //      new ItemStack(result.getHitBlock().getType())
    //  );
    //}
    return result.getHitEntity() != null;
  }

  public static LivingEntity getFirstEntityInLine(LivingEntity caster, double range,
      boolean friendly) {
    RayTraceResult result = caster.getWorld().rayTraceEntities(caster.getEyeLocation(),
        caster.getEyeLocation().getDirection(), range, 0.9, entity ->
            isValidRaycastTarget(caster, entity) && friendly == isFriendly(caster,
                (LivingEntity) entity));
    if (result == null || result.getHitEntity() == null) {
      return null;
    }
    return (LivingEntity) result.getHitEntity();
  }

  private static boolean isInvalidTarget(Entity e) {
    if (!e.isValid() || e.isInvulnerable() || !(e instanceof LivingEntity)
        || e instanceof ArmorStand) {
      return true;
    }
    if (e.hasMetadata("NPC") || e.hasMetadata("pet")) {
      return true;
    }
    if (e instanceof Player) {
      return ((Player) e).getGameMode() == GameMode.CREATIVE
          || ((Player) e).getGameMode() == GameMode.SPECTATOR;
    }
    return false;
  }

  public static boolean isDetectionStand(LivingEntity le) {
    return le instanceof ArmorStand && SpecialStatusUtil.isDetectionStand(le);
  }

  public static ArmorStand buildAndRemoveDetectionStand(Location location) {
    Location spawnLoc = location.clone();
    spawnLoc.setY(-1);
    ArmorStand stando = location.getWorld()
        .spawn(spawnLoc, ArmorStand.class, TargetingUtil::applyDetectionStandChanges);
    stando.teleport(location);
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), stando::remove, 1L);
    return stando;
  }

  private static void applyDetectionStandChanges(ArmorStand stando) {
    stando.setVisible(false);
    stando.setSmall(true);
    stando.setMarker(true);
    stando.setGravity(false);
    stando.setCollidable(false);
    SpecialStatusUtil.setDetectionStand(stando);
  }

  public static LivingEntity getTempStand(Location loc, float verticalRange) {
    if (verticalRange == -1) {
      return TargetingUtil.buildAndRemoveDetectionStand(loc);
    }
    Location detectionLocation = loc.toCenterLocation().clone();
    for (int i = 0; i < verticalRange; i++) {
      if (detectionLocation.getBlock().getRelative(BlockFace.DOWN, i).getType().isSolid()) {
        detectionLocation.add(0, -i + 0.6, 0);
        return TargetingUtil.buildAndRemoveDetectionStand(detectionLocation);
      }
    }
    return null;
  }

  private static boolean isValidRaycastTarget(LivingEntity caster, Entity entity) {
    return entity.isValid() && !entity.isInvulnerable() && entity instanceof LivingEntity &&
        entity != caster && !entity.hasMetadata("NPC") && !entity.hasMetadata("pet") &&
        !(entity instanceof Player && (((Player) entity).getGameMode() == GameMode.CREATIVE
            || ((Player) entity).getGameMode() == GameMode.SPECTATOR));
  }

  private static boolean entityWithinRadius(Entity e, Location loc, float radius) {
    Vector centerPoint = e.getLocation().toVector();
    centerPoint.setY(centerPoint.getY() + e.getHeight() / 2);
    double width = e.getWidth() * 0.55 + 0.2;
    double height = e.getHeight() * 0.55 + 0.2;
    return Math.abs(loc.getX() - centerPoint.getX()) < width + radius
        && Math.abs(loc.getZ() - centerPoint.getZ()) < width + radius
        && Math.abs(loc.getY() - centerPoint.getY()) < height + radius;
  }

  public static LivingEntity selectFirstEntityInSight(LivingEntity caster, double range,
      boolean friendly) {
    LivingEntity mobTarget = TargetingUtil.getMobTarget(caster);
    return mobTarget != null ? mobTarget : getFirstEntityInLine(caster, range, friendly);
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
          caster.getEyeLocation().getDirection(), range, FluidCollisionMode.NEVER, true, 0.2,
          entity -> isValidRaycastTarget(caster, entity));
    } else {
      result = caster.getWorld().rayTraceBlocks(caster.getEyeLocation(),
          caster.getEyeLocation().getDirection(), range, FluidCollisionMode.NEVER, true);
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
