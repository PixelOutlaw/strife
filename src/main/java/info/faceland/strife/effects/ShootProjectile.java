package info.faceland.strife.effects;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.util.LogUtil;
import java.util.List;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.SmallFireball;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

public class ShootProjectile extends Effect {

  private EntityType projectileEntity;
  private boolean targeted;
  private int quantity;
  private double speed;
  private double spread;
  private double verticalBonus;
  private boolean bounce;
  private boolean ignite;
  private float yield;
  private List<String> hitEffects;

  @Override
  public void apply(AttributedEntity caster, LivingEntity target) {
    double projectiles = quantity * (1 + caster.getAttribute(StrifeAttribute.MULTISHOT) / 100);
    if (projectileEntity == EntityType.FIREBALL) {
      projectiles = 1;
    }
    double adjustedSpread = spread + (projectiles - 1) * 0.005;
    Vector entityDirection;
    if (caster.getEntity() == target) {
      LogUtil.printWarning("Skipping self targeted projectile launched by " + getName());
      return;
    }
    if (targeted) {
      entityDirection = target.getLocation().clone().toVector()
          .subtract(caster.getEntity().getLocation().clone().toVector().normalize());
    } else {
      entityDirection = caster.getEntity().getEyeLocation().clone().getDirection();
    }
    for (int i = 0; i < projectiles; i++) {
      Projectile projectile = (Projectile) caster.getEntity().getWorld()
          .spawnEntity(caster.getEntity().getEyeLocation(), projectileEntity);
      projectile.setShooter(caster.getEntity());
      Vector direction = entityDirection.clone();
      direction.add(new Vector(
          -adjustedSpread + 2 * adjustedSpread * Math.random(),
          -adjustedSpread + verticalBonus + 2 * adjustedSpread * Math.random(),
          -adjustedSpread + 2 * adjustedSpread * Math.random()));
      direction = direction.normalize();
      projectile.setVelocity(direction.clone().multiply(speed));
      if (projectileEntity == EntityType.FIREBALL) {
        ((Fireball) projectile).setYield(yield);
        ((Fireball) projectile).setIsIncendiary(ignite);
      } else if (projectileEntity == EntityType.SMALL_FIREBALL) {
        ((SmallFireball) projectile).setIsIncendiary(ignite);
        ((SmallFireball) projectile).setDirection(direction);
      } else if (targeted && projectileEntity == EntityType.SHULKER_BULLET) {
        ((ShulkerBullet) projectile).setTarget(target);
      }
      projectile.setBounce(bounce);
      StringBuilder hitString = new StringBuilder();
      for (String s : hitEffects) {
        hitString.append(s).append("~");
      }
      projectile.setMetadata("EFFECT_PROJECTILE",
          new FixedMetadataValue(StrifePlugin.getInstance(), hitString.toString()));
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

  public void setVerticalBonus(double verticalBonus) {
    this.verticalBonus = verticalBonus;
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

  public void setHitEffects(List<String> hitEffects) {
    this.hitEffects = hitEffects;
  }
}