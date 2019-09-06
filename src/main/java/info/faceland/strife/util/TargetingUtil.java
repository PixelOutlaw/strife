package info.faceland.strife.util;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.util.DamageUtil.OriginLocation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

public class TargetingUtil {

  public static void filterFriendlyEntities(Set<LivingEntity> targets, StrifeMob caster,
      boolean friendly) {
    Set<LivingEntity> friendlyEntities = getFriendlyEntities(caster, targets);
    if (friendly) {
      targets.retainAll(friendlyEntities);
    } else {
      targets.removeAll(friendlyEntities);
    }
  }

  public static Set<LivingEntity> getFriendlyEntities(StrifeMob caster, Set<LivingEntity> targets) {
    Set<LivingEntity> friendlyEntities = new HashSet<>();
    friendlyEntities.add(caster.getEntity());
    for (StrifeMob mob : caster.getMinions()) {
      friendlyEntities.add(mob.getEntity());
    }
    // for (StrifeMob mob : getPartyMembers {
    // }
    for (LivingEntity target : targets) {
      if (caster.getEntity() == target) {
        continue;
      }
      if (caster.getEntity() instanceof Player && target instanceof Player) {
        if (DamageUtil.canAttack((Player) caster.getEntity(), (Player) target)) {
          continue;
        }
        friendlyEntities.add(target);
      }
    }
    return friendlyEntities;
  }

  public static Set<LivingEntity> getLOSEntitiesAroundLocation(Location loc, double radius) {
    ArmorStand stando = buildAndRemoveDetectionStand(loc);
    Collection<Entity> targetList = loc.getWorld().getNearbyEntities(loc, radius, radius, radius);
    Set<LivingEntity> validTargets = new HashSet<>();
    for (Entity e : targetList) {
      if (e instanceof LivingEntity && stando.hasLineOfSight(e)) {
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

  public static Set<LivingEntity> getTempStandTargetList(Location loc, boolean grounded) {
    Set<LivingEntity> targets = new HashSet<>();
    if (!grounded) {
      targets.add(TargetingUtil.buildAndRemoveDetectionStand(loc));
      return targets;
    } else {
      for (int i = 0; i < 24; i++) {
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
      if (loc.getBlock() != null && loc.getBlock().getType() != Material.AIR) {
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
    Location eyeLoc = caster.getEyeLocation();
    Vector direction = caster.getEyeLocation().getDirection();
    ArrayList<Entity> entities = (ArrayList<Entity>) caster.getNearbyEntities(range, range, range);
    for (double incRange = 0; incRange <= range; incRange += 1) {
      Location loc = eyeLoc.clone().add(direction.clone().multiply(incRange));
      if (loc.getBlock() != null && loc.getBlock().getType() != Material.AIR) {
        if (!loc.getBlock().getType().isTransparent()) {
          break;
        }
      }
      for (Entity entity : entities) {
        if (entityWithinBounds(entity, loc)) {
          return (LivingEntity) entity;
        }
      }
    }
    return null;
  }

  private static boolean entityWithinBounds(Entity entity, Location loc) {
    if (!(entity instanceof LivingEntity) || !entity.isValid()) {
      return false;
    }
    double ex = entity.getLocation().getX();
    double ey = entity.getLocation().getY() + ((LivingEntity) entity).getEyeHeight() / 2;
    double ez = entity.getLocation().getZ();
    return Math.abs(loc.getX() - ex) < 0.7 && Math.abs(loc.getZ() - ez) < 0.7
        && Math.abs(loc.getY() - ey) < 3;
  }

  public static LivingEntity selectFirstEntityInSight(LivingEntity caster, double range) {
    if (caster instanceof Mob && ((Mob) caster).getTarget() != null) {
      return ((Mob) caster).getTarget();
    }
    return getFirstEntityInLine(caster, range);
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
    if (targetEntities) {
      target = selectFirstEntityInSight(caster, range);
    }
    if (target != null) {
      return getOriginLocation(target, originLocation);
    }
    return getLocationFromRaycast(caster, range);
  }

  private static Location getLocationFromRaycast(LivingEntity caster, double range) {
    BlockIterator bi = new BlockIterator(caster.getEyeLocation(), 0, (int) range + 1);
    Block sightBlock = null;
    while (bi.hasNext()) {
      Block b = bi.next();
      if (b.getType().isSolid()) {
        sightBlock = b;
        break;
      }
    }
    if (sightBlock == null) {
      LogUtil.printDebug(" - Using MAX DISTANCE target location calculation");
      return caster.getEyeLocation().clone().add(
          caster.getEyeLocation().getDirection().multiply(range));
    }
    LogUtil.printDebug(" - Using TARGET BLOCK target location calculation");
    double dist = sightBlock.getLocation().add(0.5, 0.5, 0.5).distance(caster.getEyeLocation());
    return caster.getEyeLocation().add(
        caster.getEyeLocation().getDirection().multiply(Math.max(0, dist - 1)));
  }

  public static Location getOriginLocation(LivingEntity le, OriginLocation origin) {
    switch (origin) {
      case HEAD:
        return le.getEyeLocation();
      case CENTER:
        return le.getEyeLocation().clone()
            .subtract(le.getEyeLocation().clone().subtract(le.getLocation()).multiply(0.5));
      case GROUND:
      default:
        return le.getLocation();
    }
  }
}
