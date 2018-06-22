package info.faceland.strife.effects;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.AttributedEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

public class ShootProjectile extends Effect {

  private EntityType projectileEntity;
  private boolean targeted;
  private int quantity;
  private double speed;
  private double spread;
  private double verticalBonus;
  private String hitEffect;

  @Override
  public void apply(AttributedEntity caster, LivingEntity target) {
    double projectiles = quantity * (1 + caster.getAttribute(StrifeAttribute.MULTISHOT) / 100);
    if (projectileEntity == EntityType.FIREBALL) {
      projectiles = 1;
    }
    double adjustedSpread = spread + projectiles * 0.005;
    Vector direction;
    if (targeted) {
      direction = target.getLocation().clone().subtract(caster.getEntity().getLocation()).toVector()
          .normalize();
    } else {
      direction = caster.getEntity().getEyeLocation().getDirection();
    }
    for (int i = 0; i < projectiles; i++) {
      Projectile projectile = (Projectile) caster.getEntity().getWorld()
          .spawnEntity(caster.getEntity().getLocation(), projectileEntity);
      projectile.setShooter(caster.getEntity());
      Vector velocity = direction.multiply(speed);
      velocity.add(new Vector(
          adjustedSpread / 2 + adjustedSpread * Math.random(),
          adjustedSpread / 2 + adjustedSpread * Math.random() + verticalBonus,
          adjustedSpread / 2 + adjustedSpread * Math.random()));
      projectile.setVelocity(velocity);
      projectile.setMetadata("EFFECT_PROJECTILE", new FixedMetadataValue(StrifePlugin.getInstance(), hitEffect));
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

  public void setTargeted(boolean targeted) {
    this.targeted = targeted;
  }

  public void setHitEffect(String hitEffect) {
    this.hitEffect = hitEffect;
  }
}