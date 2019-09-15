package info.faceland.strife.util;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.util.DamageUtil.OriginLocation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class TargetingUtil {

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

  public static Set<LivingEntity> getFriendlyEntities(StrifeMob caster, Set<LivingEntity> targets) {
    return targets.stream().filter(target -> isFriendly(caster, target)).collect(Collectors.toSet());
  }

  public static boolean isFriendly(StrifeMob caster, LivingEntity target) {
    if (caster.getEntity() == target) {
      return true;
    }
    if (caster.getEntity() instanceof Player && target instanceof Player) {
      return !DamageUtil.canAttack((Player) caster.getEntity(), (Player) target);
    }
    for (StrifeMob mob : caster.getMinions()) {
      if (target == mob.getEntity()) {
        return true;
      }
    }
    // for (StrifeMob mob : getPartyMembers {
    // }
    return false;
  }

  public static Set<LivingEntity> getEntitiesInArea(LivingEntity caster, double radius) {
    Collection<Entity> targetList = caster.getWorld()
        .getNearbyEntities(caster.getLocation(), radius, radius, radius);
    Set<LivingEntity> validTargets = new HashSet<>();
    for (Entity e : targetList) {
      if (!e.isValid() || e instanceof ArmorStand || !(e instanceof LivingEntity)) {
        continue;
      }
      if (caster.hasLineOfSight(e)) {
        validTargets.add((LivingEntity) e);
      }
    }
    return validTargets;
  }

  public static boolean isDetectionStand(LivingEntity le) {
    return le instanceof ArmorStand && le.hasMetadata("STANDO");
  }

  public static ArmorStand buildAndRemoveDetectionStand(Location location) {
    ArmorStand stando = location.getWorld().spawn(location, ArmorStand.class,
        e -> e.setVisible(false));
    stando.setSmall(true);
    stando.setMetadata("STANDO", new FixedMetadataValue(StrifePlugin.getInstance(), ""));
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), stando::remove, 1L);
    return stando;
  }

  public static Set<LivingEntity> getTempStandTargetList(Location loc, float groundCheckRange) {
    Set<LivingEntity> targets = new HashSet<>();
    if (groundCheckRange < 1) {
      targets.add(TargetingUtil.buildAndRemoveDetectionStand(loc));
      return targets;
    } else {
      for (int i = 0; i < groundCheckRange; i++) {
        if (loc.getBlock().getType().isSolid()) {
          loc.setY(loc.getBlockY() + 1.1);
          targets.add(TargetingUtil.buildAndRemoveDetectionStand(loc));
          return targets;
        }
        loc.add(0, -1, 0);
      }
      return targets;
    }
  }

  public static Set<LivingEntity> getEntitiesInLine(LivingEntity caster, double range) {
    Set<LivingEntity> targets = new HashSet<>();
    Location eyeLoc = caster.getEyeLocation();
    Vector direction = caster.getEyeLocation().getDirection();
    ArrayList<Entity> entities = (ArrayList<Entity>) caster.getNearbyEntities(range, range, range);
    for (double incRange = 0; incRange <= range; incRange += 1) {
      Location loc = eyeLoc.clone().add(direction.clone().multiply(incRange));
      if (loc.getBlock().getType() != Material.AIR) {
        if (!loc.getBlock().getType().isTransparent()) {
          break;
        }
      }
      for (Entity entity : entities) {
        if (entityWithinBounds(entity, loc)) {
          targets.add((LivingEntity) entity);
        }
      }
    }
    return targets;
  }

  public static LivingEntity getFirstEntityInLine(LivingEntity caster, double range) {
    RayTraceResult result = caster.getWorld()
        .rayTraceEntities(caster.getEyeLocation(), caster.getEyeLocation().getDirection(), range,
            entity -> entity instanceof LivingEntity && entity != caster);
    if (result == null || result.getHitEntity() == null) {
      return null;
    }
    return (LivingEntity) result.getHitEntity();
  }

  private static boolean entityWithinBounds(Entity entity, Location loc) {
    if (!(entity instanceof LivingEntity) || !entity.isValid()) {
      return false;
    }
    double ex = entity.getLocation().getX();
    double ey = entity.getLocation().getY() + ((LivingEntity) entity).getEyeHeight() / 2;
    double ez = entity.getLocation().getZ();
    return Math.abs(loc.getX() - ex) < 0.85 && Math.abs(loc.getZ() - ez) < 0.85
        && Math.abs(loc.getY() - ey) < 3;
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
    if (target != null) {
      return getOriginLocation(target, originLocation);
    }
    RayTraceResult result;
    if (targetEntities) {
      result = caster.getWorld()
          .rayTrace(caster.getEyeLocation(), caster.getEyeLocation().getDirection(), range,
              FluidCollisionMode.NEVER, true, 0.2,
              entity -> entity instanceof LivingEntity && entity != caster);
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

  public static LivingEntity getMobTarget(LivingEntity livingEntity) {
    if (!(livingEntity instanceof Mob)) {
      return null;
    }
    if (((Mob) livingEntity).getTarget() == null || !((Mob) livingEntity).getTarget().isValid()) {
      return null;
    }
    return ((Mob) livingEntity).getTarget();
  }

  public static Location getOriginLocation(LivingEntity le, OriginLocation origin) {
    switch (origin) {
      case HEAD:
        return le.getEyeLocation();
      case BELOW_HEAD:
        return le.getEyeLocation().clone().add(0, -0.35, 0);
      case CENTER:
        Location location = le.getEyeLocation().clone();
        location.setY(location.getY() - le.getEyeHeight() / 2);
        return location;
      case GROUND:
      default:
        return le.getLocation();
    }
  }
}
