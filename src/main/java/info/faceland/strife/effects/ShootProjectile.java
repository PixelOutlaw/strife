package info.faceland.strife.effects;

import static info.faceland.strife.util.ProjectileUtil.getTotalProjectiles;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.util.DamageUtil;
import info.faceland.strife.util.DamageUtil.OriginLocation;
import info.faceland.strife.util.ProjectileUtil;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.WitherSkull;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

public class ShootProjectile extends Effect {

  private EntityType projectileEntity;
  private OriginLocation originType;
  private double attackMultiplier;
  private boolean targeted;
  private boolean seeking;
  private int quantity;
  private double speed;
  private double spread;
  private double radialAngle;
  private double verticalBonus;
  private boolean ignoreMultishot;
  private boolean gravity;
  private boolean bounce;
  private boolean ignite;
  private boolean zeroPitch;
  private float yield;
  private List<String> hitEffects;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    int projectiles = getProjectileCount(caster);
    double newSpeed = speed * (1 + caster.getStat(StrifeStat.PROJECTILE_SPEED) / 100);
    Vector castDirection = getCastDirection(caster.getEntity(), target.getEntity());
    Location originLocation = DamageUtil.getOriginLocation(caster.getEntity(), originType);

    double adjustedSpread = (projectiles - 1) * spread;
    double startAngle = 0;
    if (radialAngle != 0) {
      startAngle = -radialAngle / 2;
    }
    for (int i = 0; i < projectiles; i++) {
      Vector direction = castDirection.clone();
      if (radialAngle != 0) {
        applyRadialAngles(direction, startAngle, projectiles, i);
      }
      applySpread(direction, adjustedSpread);
      direction = direction.normalize().multiply(newSpeed);

      final Vector finalDirection = direction.clone().multiply(newSpeed);

      Projectile projectile = (Projectile) originLocation.getWorld().spawn(originLocation,
          projectileEntity.getEntityClass(), e -> e.setVelocity(finalDirection));
      projectile.setShooter(caster.getEntity());

      if (projectileEntity == EntityType.FIREBALL) {
        ((Fireball) projectile).setYield(yield);
        ((Fireball) projectile).setIsIncendiary(ignite);
      } else if (projectileEntity == EntityType.ARROW) {
        if (ignite) {
          projectile.setFireTicks(20);
        }
      } else if (projectileEntity == EntityType.WITHER_SKULL) {
        ((WitherSkull) projectile).setYield(yield);
      } else if (projectileEntity == EntityType.SMALL_FIREBALL) {
        ((SmallFireball) projectile).setIsIncendiary(ignite);
        ((SmallFireball) projectile).setDirection(finalDirection);
      } else if (seeking && projectileEntity == EntityType.SHULKER_BULLET) {
        ((ShulkerBullet) projectile).setTarget(target.getEntity());
      }
      projectile.setBounce(bounce);
      projectile.setGravity(gravity);
      ProjectileUtil.setProjctileAttackSpeedMeta(projectile, attackMultiplier);

      if (!hitEffects.isEmpty()) {
        StringBuilder hitString = new StringBuilder();
        for (String s : hitEffects) {
          hitString.append(s).append("~");
        }
        projectile.setMetadata("EFFECT_PROJECTILE",
            new FixedMetadataValue(StrifePlugin.getInstance(), hitString.toString()));
      }
    }
  }

  public void setProjectileEntity(EntityType projectileEntity) {
    this.projectileEntity = projectileEntity;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  public void setSpeed(double speed) {
    this.speed = speed;
  }

  public void setSpread(double spread) {
    this.spread = spread;
  }

  public void setRadialAngle(double radialAngle) {
    this.radialAngle = radialAngle;
  }

  public void setVerticalBonus(double verticalBonus) {
    this.verticalBonus = verticalBonus;
  }

  public void setIgnoreMultishot(boolean ignoreMultishot) {
    this.ignoreMultishot = ignoreMultishot;
  }

  public void setGravity(boolean gravity) {
    this.gravity = gravity;
  }

  public void setBounce(boolean bounce) {
    this.bounce = bounce;
  }

  public void setIgnite(boolean ignite) {
    this.ignite = ignite;
  }

  public void setYield(float yield) {
    this.yield = yield;
  }

  public void setTargeted(boolean targeted) {
    this.targeted = targeted;
  }

  public void setSeeking(boolean seeking) {
    this.seeking = seeking;
  }

  public void setHitEffects(List<String> hitEffects) {
    this.hitEffects = hitEffects;
  }

  public void setZeroPitch(boolean zeroPitch) {
    this.zeroPitch = zeroPitch;
  }

  public void setOriginType(OriginLocation originType) {
    this.originType = originType;
  }

  public void setAttackMultiplier(double attackMultiplier) {
    this.attackMultiplier = attackMultiplier;
  }

  private Vector getCastDirection(LivingEntity caster, LivingEntity target) {
    Vector direction;
    if (targeted) {
      direction = target.getLocation().toVector().subtract(
          caster.getLocation().toVector()).normalize();
    } else {
      direction = caster.getEyeLocation().getDirection();
    }
    if (zeroPitch) {
      direction.setY(0);
      direction.normalize();
    }
    return direction;
  }

  private int getProjectileCount(StrifeMob caster) {
    if (ignoreMultishot || projectileEntity == EntityType.FIREBALL) {
      return 1;
    }
    return getTotalProjectiles(quantity, caster.getStat(StrifeStat.MULTISHOT));
  }

  private void applyRadialAngles(Vector direction, double angle, int projectiles, int counter) {
    if (projectiles == 1) {
      return;
    }
    angle = Math.toRadians(angle + counter * (radialAngle / (projectiles-1)));
    double x = direction.getX();
    double z = direction.getZ();
    direction.setZ(z * Math.cos(angle) - x * Math.sin(angle));
    direction.setX(z * Math.sin(angle) + x * Math.cos(angle));
  }

  private void applySpread(Vector direction, double spread) {
    direction.add(new Vector(
        spread - 2 * spread * Math.random(),
        spread - 2 * spread * Math.random() + verticalBonus,
        spread - 2 * spread * Math.random()));
  }
}