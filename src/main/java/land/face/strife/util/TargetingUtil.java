package land.face.strife.util;

import static land.face.strife.util.HitDetectionUtil.getFirstEntityInLine;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.Union;
import land.face.strife.data.effects.AreaEffect.TargetingPriority;
import land.face.strife.data.effects.TargetingComparators.DistanceComparator;
import land.face.strife.data.effects.TargetingComparators.FlatHealthComparator;
import land.face.strife.data.effects.TargetingComparators.PercentHealthComparator;
import land.face.strife.util.DamageUtil.OriginLocation;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
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
  private static final FlatHealthComparator HEALTH_COMPARATOR = new FlatHealthComparator();
  private static final PercentHealthComparator PERCENT_HEALTH_COMPARATOR = new PercentHealthComparator();

  public static void expandMobRange(LivingEntity attacker, LivingEntity victim) {
    if (!(victim instanceof Mob)) {
      return;
    }
    victim.getAttribute(Attribute.FOLLOW_RANGE).setBaseValue(80);
    LivingEntity target = ((Mob) victim).getTarget();
    if (target == null || !target.isValid()) {
      ((Mob) victim).setTarget(attacker);
    }
  }

  public static void filterFriendlyEntities(Set<LivingEntity> targets, StrifeMob caster, boolean friendly) {
    Set<LivingEntity> friendlyEntities = getFriendlyEntities(caster, targets);
    if (friendly) {
      targets.clear();
      targets.addAll(friendlyEntities);
    } else {
      targets.removeAll(friendlyEntities);
    }
  }

  public static void filterByTargetPriority(Set<LivingEntity> areaTargets,
      TargetingPriority priority, StrifeMob caster, int maxTargets) {
    if (areaTargets.isEmpty()) {
      return;
    }
    List<LivingEntity> targetList = new ArrayList<>(areaTargets);
    switch (priority) {
      case RANDOM -> {
        Collections.shuffle(targetList);
        areaTargets.retainAll(targetList.subList(0, maxTargets));
      }
      case CLOSEST -> {
        DISTANCE_COMPARATOR.setLoc(caster.getEntity().getLocation());
        targetList.sort(DISTANCE_COMPARATOR);
        areaTargets.retainAll(targetList.subList(0, maxTargets));
      }
      case FARTHEST -> {
        DISTANCE_COMPARATOR.setLoc(caster.getEntity().getLocation());
        targetList.sort(DISTANCE_COMPARATOR);
        areaTargets.retainAll(
            targetList.subList(targetList.size() - maxTargets, targetList.size()));
      }
      case LEAST_HEALTH -> {
        targetList.sort(HEALTH_COMPARATOR);
        areaTargets.retainAll(targetList.subList(0, maxTargets));
      }
      case MOST_HEALTH -> {
        targetList.sort(HEALTH_COMPARATOR);
        areaTargets.retainAll(
            targetList.subList(targetList.size() - maxTargets, targetList.size()));
      }
      case LEAST_PERCENT_HEALTH -> {
        targetList.sort(PERCENT_HEALTH_COMPARATOR);
        areaTargets.retainAll(targetList.subList(0, maxTargets));
      }
      case MOST_PERCENT_HEALTH -> {
        targetList.sort(PERCENT_HEALTH_COMPARATOR);
        areaTargets.retainAll(
            targetList.subList(targetList.size() - maxTargets, targetList.size()));
      }
    }
  }

  public static Set<LivingEntity> getFriendlyEntities(StrifeMob caster, Set<LivingEntity> targets) {
    return targets.stream().filter(target -> isFriendly(caster, target)).collect(Collectors.toSet());
  }

  public static boolean isFriendly(LivingEntity attacker, LivingEntity defender) {
    return isFriendly(StrifePlugin.getInstance().getStrifeMobManager().getStatMob(attacker),
        StrifePlugin.getInstance().getStrifeMobManager().getStatMob(defender));
  }

  public static boolean isFriendly(StrifeMob attacker, LivingEntity defender) {
    return isFriendly(attacker, StrifePlugin.getInstance().getStrifeMobManager().getStatMob(defender));
  }

  public static boolean isFriendly(LivingEntity attacker, StrifeMob defender) {
    return isFriendly(StrifePlugin.getInstance().getStrifeMobManager().getStatMob(attacker), defender);
  }

  public static boolean isFriendly(StrifeMob attacker, StrifeMob defender) {
    if (attacker.getEntity() == null) {
      return false;
    }
    if (defender == null) {
      return true;
    }
    if (defender.getOwner() != null) {
      return isFriendly(attacker, defender.getOwner());
    }
    if (attacker.getEntity() == defender.getEntity()) {
      return true;
    }
    if (defender.getEntity() instanceof Tameable) {
      UUID uuid = ((Tameable) defender.getEntity()).getOwnerUniqueId();
      if (uuid != null) {
        return isFriendly(attacker, (LivingEntity) Bukkit.getEntity(uuid));
      }
    }
    for (String casterFaction : attacker.getFactions()) {
      for (String targetFaction : defender.getFactions()) {
        if (casterFaction.equalsIgnoreCase(targetFaction)) {
          return true;
        }
      }
    }
    if (attacker.getMaster() != null) {
      if (attacker.getMaster() == defender.getMaster()) {
        return true;
      }
      return isFriendly(attacker.getMaster(), defender);
    }
    if (defender.getMaster() != null) {
      return isFriendly(attacker, defender.getMaster());
    }
    if (attacker.getMaster() == defender || attacker.getMinions().contains(defender)) {
      return true;
    }
    if (DamageUtil.isGuildAlly(attacker, defender)) {
      return true;
    }
    if (attacker.getEntity() instanceof Player && defender.getEntity() instanceof Player) {
      return !DamageUtil.canAttack((Player) attacker.getEntity(), (Player) defender.getEntity());
    }
    return false;
  }

  public static boolean hasLineOfSight(Location start, Location target, Entity entity) {
    if (start.getWorld() != target.getWorld()) {
      return false;
    }
    Vector direction = target.toVector().subtract(start.toVector());
    if (direction.length() < 0.15) {
      return true;
    }
    direction.normalize();
    RayTraceResult result = start.getWorld().rayTrace(start, direction, 40,
        FluidCollisionMode.NEVER, true, 0.04, e -> e == entity);
    if (result == null) {
      return false;
    }
    return result.getHitEntity() != null;
  }



  public static boolean isInvalidTarget(Entity e) {
    if (!e.isValid() || e.isInvulnerable() || !(e instanceof LivingEntity) || e instanceof ArmorStand) {
      return true;
    }
    if (e.hasMetadata("NPC") || e.hasMetadata("pet")) {
      return true;
    }
    if (e instanceof Player) {
      return ((Player) e).getGameMode() == GameMode.CREATIVE || ((Player) e).getGameMode() == GameMode.SPECTATOR;
    }
    return false;
  }

  public static Location modifyLocation(Location loc, float verticalRange) {
    if (loc == null) {
      return null;
    }
    Location detectionLocation = loc.toCenterLocation().clone();
    for (int i = 0; i < verticalRange; i++) {
      if (detectionLocation.getBlock().getRelative(BlockFace.DOWN, i).getType().isSolid()) {
        detectionLocation.add(0, -i + 0.6, 0);
        return detectionLocation;
      }
    }
    return null;
  }

  public static boolean isValidRaycastTarget(LivingEntity caster, Entity entity) {
    return entity.isValid() && !entity.isInvulnerable() && entity instanceof LivingEntity &&
        entity != caster && !entity.hasMetadata("NPC") && !entity.hasMetadata("pet") &&
        !(entity instanceof Player && (((Player) entity).getGameMode() == GameMode.CREATIVE
            || ((Player) entity).getGameMode() == GameMode.SPECTATOR));
  }

  public static LivingEntity selectFirstEntityInSight(LivingEntity caster, double range, boolean friendly) {
    LivingEntity mobTarget = TargetingUtil.getMobTarget(caster);
    return mobTarget != null ? mobTarget : getFirstEntityInLine(caster, range, friendly);
  }

  public static Location getTargetLocation(LivingEntity caster, LivingEntity target, double range,
      boolean targetEntities, boolean friendly) {
    return getTargetLocation(caster, target, range, OriginLocation.CENTER, "", targetEntities, friendly);
  }

  public static Location getTargetLocation(LivingEntity caster, LivingEntity target, double range,
      OriginLocation originLocation, String extra, boolean targetEntities, boolean friendly) {
    if (target != null && caster.getLocation().distance(target.getLocation()) < range &&
        caster.hasLineOfSight(target)) {
      return getOriginLocation(target, originLocation, extra);
    }
    RayTraceResult result;
    if (targetEntities) {
      result = caster.getWorld().rayTrace(
          caster.getEyeLocation(),
          caster.getEyeLocation().getDirection(),
          range,
          FluidCollisionMode.NEVER,
          true,
          0.2,
          entity -> isValidRaycastTarget(caster, entity) && friendly == isFriendly(caster, (LivingEntity) entity));
    } else {
      result = caster.getWorld().rayTraceBlocks(caster.getEyeLocation(),
          caster.getEyeLocation().getDirection(), range, FluidCollisionMode.NEVER, true);
    }
    if (result == null) {
      LogUtil.printDebug(" - Using MAX RANGE location calculation");
      return caster.getEyeLocation().add(caster.getEyeLocation().getDirection().multiply(Math.max(0, range - 0.35)));
    }
    if (result.getHitEntity() != null) {
      LogUtil.printDebug(" - Using ENTITY location calculation");
      return getOriginLocation((LivingEntity) result.getHitEntity(), originLocation, extra);
    }
    if (result.getHitBlock() != null) {
      LogUtil.printDebug(" - Using BLOCK location calculation");
      switch (Objects.requireNonNull(result.getHitBlockFace())) {
        case DOWN -> {
          Location loc = caster.getLocation().clone();
          loc.setX(result.getHitPosition().getX());
          loc.setZ(result.getHitPosition().getZ());
          loc.setY(result.getHitBlock().getLocation().getY() - 1);
          return loc;
        }
        case UP -> {
          Location loc = caster.getLocation().clone();
          loc.setX(result.getHitPosition().getX());
          loc.setZ(result.getHitPosition().getZ());
          loc.setY(result.getHitBlock().getLocation().getY() + 1.5);
          return loc;
        }
        default -> {
          Block targetBlock = result.getHitBlock();
          Block hitBlockSpace = result.getHitBlock().getRelative(result.getHitBlockFace());
          if (canPortTo(targetBlock.getRelative(BlockFace.UP, 1).getType()) &&
              canPortTo(targetBlock.getRelative(BlockFace.UP, 2).getType()) &&
              canPortTo(hitBlockSpace.getRelative(BlockFace.UP, 1).getType()) &&
              canPortTo(hitBlockSpace.getRelative(BlockFace.UP, 2).getType())) {
            return result.getHitBlock().getLocation().add(0.5, 1.5, 0.5);
          }
          return result.getHitBlock().getLocation().add(0.5, 0.8, 0.5)
              .add(result.getHitBlockFace().getDirection());
        }
      }
    }
    LogUtil.printDebug(" - Using HIT RANGE location calculation");
    return new Location(caster.getWorld(),
        result.getHitPosition().getX(),
        result.getHitPosition().getBlockY() + 0.25,
        result.getHitPosition().getZ()
    );
  }

  public static boolean canPortTo(Material material) {
    return switch (material) {
      case AIR, CAVE_AIR, CAVE_VINES, VINE, LADDER, SHORT_GRASS, TALL_GRASS, MOSS_CARPET -> true;
      default -> false;
    };
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
    return getOriginLocation(le, origin, "");
  }

  public static Location getOriginLocation(LivingEntity le, OriginLocation origin, String extra) {
    if (le == null) {
      return null;
    }
    if (origin == OriginLocation.MODEL_BONE && StringUtils.isBlank(extra)) {
      Bukkit.getLogger().info("[Strife] Tried to get model bone but extra not provided");
      origin = OriginLocation.CENTER;
    }
    switch (origin) {
      case HEAD -> {
        return le.getEyeLocation().clone();
      }
      case BELOW_HEAD -> {
        return le.getEyeLocation().clone().add(0, -0.4, 0);
      }
      case ABOVE_HEAD -> {
        return le.getEyeLocation().clone().add(0, 0.4, 0);
      }
      case CENTER -> {
        Vector vec = le.getEyeLocation().toVector().subtract(le.getLocation().toVector()).multiply(0.5);
        return le.getLocation().clone().add(vec);
      }
      case MODEL_BONE -> {
        if (StringUtils.isBlank(extra)) {
          return le.getLocation().clone();
        }
        ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(le);
        if (modeledEntity == null) {
          return le.getLocation().clone();
        }
        Optional<ActiveModel> activeModel = modeledEntity.getModels().values().stream().findFirst();
        if (activeModel.isEmpty()) {
          return le.getLocation().clone();
        }
        Optional<ModelBone> bone = activeModel.get().getBone(extra);
        if (bone.isPresent()) {
          return bone.get().getLocation().clone();
        } else {
          Bukkit.getLogger().info("[Strife] Bone ID " + extra + " not found for origin detection!");
          return le.getLocation().clone();
        }
      }
      case UNION_BONE -> {
        if (StringUtils.isBlank(extra)) {
          Bukkit.getLogger().info("[Strife] Bone ID " + extra + " not found for origin detection!");
          return le.getLocation().clone();
        }
        StrifeMob mob = StrifePlugin.getInstance().getStrifeMobManager().getStatMob(le);
        Union union = StrifePlugin.getInstance().getUnionManager().getUnion(mob);
        if (union == null) {
          return le.getLocation().clone();
        }
        ActiveModel activeModel = union.getActiveModel();
        Optional<ModelBone> bone = activeModel.getBone(extra);
        if (bone.isPresent()) {
          return bone.get().getLocation().clone();
        } else {
          Bukkit.getLogger().info("[Strife] Bone ID " + extra + " not found for origin detection!");
          return le.getLocation().clone();
        }
      }
      default -> {
        return le.getLocation().clone();
      }
    }
  }
}
