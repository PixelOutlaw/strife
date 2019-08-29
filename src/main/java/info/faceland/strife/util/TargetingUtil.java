package info.faceland.strife.util;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.util.DamageUtil.OriginLocation;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

public class TargetingUtil {

  public static void filterFriendlyEntities(Set<LivingEntity> targets, StrifeMob caster, boolean friendly) {
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

  public static ArmorStand buildAndRemoveDetectionStand(Location location) {
    ArmorStand stando = location.getWorld().spawn(location, ArmorStand.class,
        e -> e.setVisible(false));
    stando.setSmall(true);
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), stando::remove, 1L);
    return stando;
  }

  public static LivingEntity getFirstEntityInLOS(LivingEntity le, int range) {
    List<Entity> targetList = le.getNearbyEntities(range + 1, range + 1, range + 1);
    BlockIterator bi = new BlockIterator(le.getEyeLocation(), 0, range);
    while (bi.hasNext()) {
      Block b = bi.next();
      double bx = b.getX() + 0.5;
      double by = b.getY() + 0.5;
      double bz = b.getZ() + 0.5;
      if (b.getType().isSolid()) {
        break;
      }
      for (Entity e : targetList) {
        if (!(e instanceof LivingEntity)) {
          continue;
        }
        if (!e.isValid()) {
          continue;
        }
        Location l = e.getLocation();
        double ex = l.getX();
        double ey = l.getY();
        double ez = l.getZ();
        if (Math.abs(bx - ex) < 0.5 && Math.abs(bz - ez) < 0.5 && Math.abs(by - ey) < 2.5) {
          return (LivingEntity) e;
        }
      }
    }
    return null;
  }

  public static LivingEntity selectFirstEntityInSight(LivingEntity caster, double range) {
    if (caster instanceof Mob && ((Mob) caster).getTarget() != null) {
      return ((Mob) caster).getTarget();
    }
    return getFirstEntityInLOS(caster, (int) range);
  }

  public static Location getTargetArea(LivingEntity caster, LivingEntity target, double range,
      OriginLocation originLocation, boolean targetEntities) {
    if (!targetEntities) {
      return getTargetLocation(caster, range);
    }
    if (target == null) {
      target = selectFirstEntityInSight(caster, range);
    }
    if (target != null) {
      return getOriginLocation(target, originLocation);
    }
    return getTargetLocation(caster, range);
  }

  public static Location getTargetArea(LivingEntity caster, LivingEntity target, double range, boolean targetEntities) {
    return getTargetArea(caster, target, range, OriginLocation.CENTER, targetEntities);
  }

  private static Location getTargetLocation(LivingEntity caster, double range) {
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
