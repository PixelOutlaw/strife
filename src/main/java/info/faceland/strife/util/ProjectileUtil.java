package info.faceland.strife.util;

import info.faceland.strife.StrifePlugin;
import java.util.Random;
import org.bukkit.Sound;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.Trident;
import org.bukkit.entity.WitherSkull;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

public class ProjectileUtil {

  public final static String ATTACK_SPEED_META = "AS_MULT";
  public final static String SNEAK_ATTACK_META = "SNEAK_SHOT";

  private static final Random RANDOM = new Random(System.currentTimeMillis());

  public static int getTotalProjectiles(double initialProjectiles, double multiShot) {
    double projectiles = initialProjectiles;
    if (multiShot > 0) {
      projectiles *= 1 + (multiShot / 100);
      if (projectiles % 1 >= RANDOM.nextDouble()) {
        projectiles++;
      }
      return (int) Math.floor(projectiles);
    }
    return (int) initialProjectiles;
  }

  public static void createMagicMissile(Player shooter, double attackMult, double power,
      double xOff, double yOff, double zOff) {
    createMagicMissile(shooter, attackMult, power, xOff, yOff, zOff, true);
  }

  public static void createMagicMissile(Player shooter, double attackMult, double power,
      double xOff, double yOff, double zOff, boolean gravity) {
    ShulkerBullet magicProj = shooter.getWorld().spawn(
        shooter.getEyeLocation().clone().add(0, -0.35, 0), ShulkerBullet.class);
    magicProj.setShooter(shooter);
    magicProj.setGravity(gravity);

    Vector vec = shooter.getEyeLocation().getDirection();
    xOff = vec.getX() * power + xOff;
    yOff = vec.getY() * power + yOff;
    zOff = vec.getZ() * power + zOff;
    if (gravity) {
      yOff += 0.23;
    }
    magicProj.setVelocity(new Vector(xOff, yOff, zOff));
    setProjctileAttackSpeedMeta(magicProj, attackMult);
    if (shooter.isSneaking()) {
      setProjectileSneakMeta(magicProj);
    }
  }

  public static void createGhastBall(LivingEntity shooter, double attackMult, double power,
      double radius) {
    shooter.getWorld().playSound(shooter.getLocation(), Sound.ENTITY_GHAST_SHOOT, 0.7f, 1.1f);
    Fireball fireball = shooter.getWorld()
        .spawn(shooter.getEyeLocation().clone().add(0, -0.35, 0), Fireball.class);
    fireball.setShooter(shooter);
    fireball.setBounce(false);
    fireball.setIsIncendiary(false);
    fireball.setYield((float) (2 + radius * 0.5));

    Vector vec = shooter.getLocation().getDirection().multiply(0.05 * power);
    fireball.setVelocity(vec);
    setProjctileAttackSpeedMeta(fireball, attackMult);
  }

  public static void createWitherSkull(LivingEntity shooter, double attackMult, double power,
      double radius) {
    shooter.getWorld().playSound(shooter.getLocation(), Sound.ENTITY_WITHER_SHOOT, 0.7f, 1.1f);
    WitherSkull skull = shooter.getWorld().spawn(
        shooter.getEyeLocation().clone().add(0, -0.35, 0), WitherSkull.class);
    skull.setShooter(shooter);
    skull.setYield((float) (2 + radius * 0.5));

    Vector vec = shooter.getLocation().getDirection().multiply(0.05 * power);
    skull.setVelocity(vec);
    setProjctileAttackSpeedMeta(skull, attackMult);
  }

  public static void createArrow(LivingEntity shooter, double attackMult, float power, double spread,
      double vertBonus) {
    Vector velocity = getProjectileVelocity(shooter, power, spread, vertBonus);
    Arrow arrow = shooter.getWorld().spawn(shooter.getEyeLocation().clone().add(0, -0.35, 0),
        Arrow.class, e -> e.setVelocity(velocity));
    arrow.setShooter(shooter);
    arrow.setPickupStatus(PickupStatus.CREATIVE_ONLY);

    ProjectileUtil.setProjctileAttackSpeedMeta(arrow, attackMult);
    if (shooter instanceof Player) {
      if (attackMult > 0.95) {
        arrow.setCritical(true);
      }
      if (((Player)shooter).isSneaking()) {
        ProjectileUtil.setProjectileSneakMeta(arrow);
      }
    }
  }

  public static Vector getProjectileVelocity(LivingEntity shooter, float speed, double spread, double verticalBonus) {
    Vector vector = shooter.getEyeLocation().getDirection();
    vector.multiply(speed);
    if (spread == 0) {
      return vector.add(new Vector(0, verticalBonus, 0));
    }
    double xOff = -spread + spread * 2 * Math.random();
    double yOff = -spread + spread * 2 * Math.random();
    double zOff = -spread + spread * 2 * Math.random();
    return vector.add(new Vector(xOff, verticalBonus + yOff, zOff));
  }

  public static void createTrident(Player shooter, Trident trident, float attackMult,
      double power) {
    Vector vector = trident.getVelocity().multiply(power);
    Trident newTrident = shooter.getWorld()
        .spawn(trident.getLocation(), Trident.class, e -> e.setVelocity(vector));
    newTrident.setShooter(shooter);
    newTrident.setPickupStatus(PickupStatus.CREATIVE_ONLY);
    ProjectileUtil.setProjctileAttackSpeedMeta(trident, attackMult);
    if (shooter.isSneaking()) {
      ProjectileUtil.setProjectileSneakMeta(trident);
    }
  }

  public static void setProjctileAttackSpeedMeta(Projectile proj, double attackMult) {
    proj.setMetadata(ATTACK_SPEED_META,
        new FixedMetadataValue(StrifePlugin.getInstance(), attackMult));
  }

  public static void setProjectileSneakMeta(Projectile projectile) {
    projectile.setMetadata(SNEAK_ATTACK_META,
        new FixedMetadataValue(StrifePlugin.getInstance(), true));
  }

  public static boolean isProjectile(EntityType entityType) {
    switch (entityType) {
      case ARROW:
      case THROWN_EXP_BOTTLE:
      case SPLASH_POTION:
      case WITHER_SKULL:
      case SHULKER_BULLET:
      case PRIMED_TNT:
      case SMALL_FIREBALL:
      case LLAMA_SPIT:
      case SPECTRAL_ARROW:
      case TRIDENT:
      case FIREBALL:
      case DRAGON_FIREBALL:
      case EGG:
      case SNOWBALL:
        return true;
      default:
        return false;
    }
  }
}
