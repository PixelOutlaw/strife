package land.face.strife.data.effects;

import static land.face.strife.util.ProjectileUtil.HANDLED_PROJECTILE;

import com.tealcube.minecraft.bukkit.facecore.utilities.ChunkUtil;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.TargetResponse;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.DamageUtil.OriginLocation;
import land.face.strife.util.ProjectileUtil;
import land.face.strife.util.TargetingUtil;
import lombok.Getter;
import lombok.Setter;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.ThrowableProjectile;
import org.bukkit.entity.WitherSkull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

@Getter
@Setter
public class ShootProjectile extends Effect {

  private EntityType projectileEntity;
  private OriginLocation originType;
  private Color arrowColor;
  private List<Color> modelColors;
  private List<ItemStack> itemModels = new ArrayList<>();
  private String modelId;

  private boolean ignoreMultishot, ignite, zeroPitch, blockHitEffects, silent, gravity, throwItem, targeted, seeking,
      scaleModelWithMultishot;
  private int quantity, pierceTargets, maxDuration;
  private float attackMultiplier, yield, speed, spread, radialAngle, verticalBonus, minVerticalSpeed, modelScale;

  private Disguise disguise = null;

  private final List<Effect> trailEffects = new ArrayList<>();
  private final List<Effect> hitEffects = new ArrayList<>();

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    int projectiles = getProjectileCount(caster);
    float newSpeed = speed * (1 + caster.getStat(StrifeStat.PROJECTILE_SPEED) / 100);
    Location originLocation = TargetingUtil.getOriginLocation(caster.getEntity(), originType);

    double startAngle = 0;
    if (radialAngle != 0) {
      startAngle = -radialAngle / 2;
    }
    double newSpread = projectiles == 1 ? 0 : spread * Math.pow(projectiles, 0.5);
    for (int i = 0; i < projectiles; i++) {
      Vector direction;
      if (targeted && target != null) {
        direction = target.getEntity().getLocation().toVector()
            .subtract(caster.getEntity().getLocation().toVector()).normalize();
      } else {
        direction = caster.getEntity().getEyeLocation().getDirection();
      }
      Vector velocity = ProjectileUtil.getProjectileVelocity(direction, newSpeed, newSpread, verticalBonus, zeroPitch);
      if (radialAngle != 0) {
        applyRadialAngles(velocity, startAngle, projectiles, i);
      }
      if (minVerticalSpeed != -1) {
        velocity.setY(Math.max(minVerticalSpeed, velocity.getY()));
      }

      assert projectileEntity.getEntityClass() != null;

      Location location = originLocation.clone();
      location.setDirection(direction);
      LivingEntity shooter = caster.getEntity();

      HANDLED_PROJECTILE.add(caster.getEntity().getUniqueId());
      Projectile projectile = (Projectile) location.getWorld().spawn(location, projectileEntity.getEntityClass(), e -> {
        ((Projectile) e).setShooter(shooter);
        ((Projectile) e).setHasLeftShooter(false);
        e.setVelocity(velocity);
        e.getLocation().setDirection(direction);
        e.setGravity(gravity);
        e.setSilent(silent);
        if (e instanceof ThrowableProjectile tp && itemModels.isEmpty()) {
          Collections.shuffle(itemModels);
          tp.setItem(itemModels.getFirst().clone());
        }
        e.getBoundingBox().expand(1);

        double hLen = Math.sqrt(direction.getX() * direction.getX() + direction.getZ() * direction.getZ());
        e.setRotation(
            (float) Math.toDegrees(Math.atan2(direction.getX(), direction.getZ())),
            (float) Math.toDegrees(Math.atan2(direction.getY(), hLen)));
      });
      HANDLED_PROJECTILE.remove(caster.getEntity().getUniqueId());

      if (!blockHitEffects && pierceTargets > -3) {
        ProjectileUtil.setPierce(projectile, caster.getStat(StrifeStat.PIERCE_CHANCE) / 100, pierceTargets);
      }
      if (ignite) {
        projectile.setFireTicks(200);
      }
      if (projectile instanceof AbstractArrow aa) {
        if (projectileEntity != EntityType.SPECTRAL_ARROW && arrowColor != null) {
          ((Arrow) aa).setColor(arrowColor);
        }
        aa.setPickupStatus(PickupStatus.CREATIVE_ONLY);
      } else if (projectileEntity == EntityType.FIREBALL || projectileEntity == EntityType.DRAGON_FIREBALL) {
        ((Fireball) projectile).setYield(yield);
        ((Fireball) projectile).setIsIncendiary(ignite);
      } else if (projectileEntity == EntityType.WITHER_SKULL) {
        ((WitherSkull) projectile).setYield(yield);
      } else if (projectileEntity == EntityType.SMALL_FIREBALL) {
        ((SmallFireball) projectile).setIsIncendiary(ignite);
        ((SmallFireball) projectile).setDirection(velocity);
      } else if (seeking && projectileEntity == EntityType.SHULKER_BULLET) {
        ((ShulkerBullet) projectile).setTarget(target.getEntity());
      }
      ProjectileUtil.setAttackMult(projectile, attackMultiplier);
      ProjectileUtil.setApplyOnHit(projectile, true);
      ProjectileUtil.setAbilityProjectile(projectile);
      if (blockHitEffects) {
        ProjectileUtil.setContactTrigger(projectile);
      }
      if (!hitEffects.isEmpty()) {
        ProjectileUtil.setHitEffects(projectile, hitEffects);
      }
      if (disguise != null) {
        DisguiseAPI.disguiseToPlayers(projectile, disguise, Bukkit.getOnlinePlayers());
      }

      ChunkUtil.setDespawnOnUnload(projectile);
      ProjectileUtil.setShotId(projectile);

      if (maxDuration != -1) {
        int tick = (int) (((float) maxDuration) * (1 + caster.getStat(StrifeStat.CAST_RANGE) / 100));
        Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
          if (projectile.isValid()) {
            for (Effect effect : hitEffects) {
              if (effect instanceof LocationEffect) {
                ((LocationEffect) effect).applyAtLocation(caster, projectile.getLocation());
              }
            }
            projectile.remove();
          }
        }, tick);
      }

      if (StringUtils.isNotBlank(modelId)) {
        float scale = modelScale;
        if (scaleModelWithMultishot) {
          scale += caster.getStat(StrifeStat.MULTISHOT) / 600;
          scale += caster.getStat(StrifeStat.EXTRA_PROJECTILES) * 0.05f;
        }
        var c = modelColors.isEmpty() ? null : modelColors.get(StrifePlugin.RNG.nextInt(modelColors.size()));
        ProjectileUtil.setupModelledProjectile(modelId, caster, projectile, location, direction, c, scale, throwItem);
      }

      if (!trailEffects.isEmpty()) {
        final Projectile proj = projectile;
        Bukkit.getScheduler().runTaskTimer(getPlugin(), (task) -> {
          if (!proj.isValid() || (proj instanceof AbstractArrow && ((AbstractArrow) proj).isInBlock())) {
            task.cancel();
            return;
          }
          getPlugin().getEffectManager()
              .executeEffectList(caster, new TargetResponse(proj.getLocation()), trailEffects);
        }, 1L, 1L);
      }
    }
    ProjectileUtil.bumpShotId();
  }

  private Vector getCastDirection(LivingEntity caster, LivingEntity target) {
    Vector direction;
    if (targeted) {
      direction = target.getLocation().toVector().subtract(caster.getLocation().toVector()).normalize();
    } else {
      direction = caster.getEyeLocation().getDirection();
    }
    return direction;
  }

  private int getProjectileCount(StrifeMob caster) {
    if (ignoreMultishot || projectileEntity == EntityType.FIREBALL) {
      return 1;
    }
    return ProjectileUtil.getTotalProjectiles(quantity, caster.getStat(StrifeStat.MULTISHOT),
        caster.getStat(StrifeStat.EXTRA_PROJECTILES));
  }

  private void applyRadialAngles(Vector direction, double angle, int projectiles, int counter) {
    if (projectiles == 1) {
      return;
    }
    angle = Math.toRadians(angle + counter * (radialAngle / (projectiles - 1)));
    direction.rotateAroundY(angle);
  }
}