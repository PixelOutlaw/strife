package land.face.strife.util;

import java.util.Random;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import org.bukkit.Sound;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.Trident;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

public class ProjectileUtil {

  public final static String ATTACK_SPEED_META = "AS_MULT";
  public final static String SNEAK_ATTACK_META = "SNEAK_SHOT";
  public final static String SHOT_ID_META = "SHOT_ID";
  private static int shotId = 1;

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

  public static void shootWand(StrifeMob mob, double attackMult) {
    float projectileSpeed = 1 + (mob.getStat(StrifeStat.PROJECTILE_SPEED) / 100);
    int projectiles = ProjectileUtil.getTotalProjectiles(1, mob.getStat(StrifeStat.MULTISHOT));

    ProjectileUtil.createMagicMissile(mob.getEntity(), attackMult, projectileSpeed, 0, 0.23, true);
    projectiles--;

    for (int i = projectiles; i > 0; i--) {
      ProjectileUtil.createMagicMissile(mob.getEntity(), attackMult, projectileSpeed,
          randomWandOffset(projectiles), 0.23, true);
    }

    mob.getEntity().getWorld()
        .playSound(mob.getEntity().getLocation(), Sound.ENTITY_BLAZE_HURT, 0.7f, 2f);
    StrifePlugin.getInstance().getSneakManager().tempDisableSneak(mob.getEntity());
    shotId++;
  }

  public static void shootArrow(StrifeMob mob, float attackMult) {
    float projectileSpeed = 2.5f * (1 + (mob.getStat(StrifeStat.PROJECTILE_SPEED) / 100));
    int projectiles = ProjectileUtil.getTotalProjectiles(1, mob.getStat(StrifeStat.MULTISHOT));

    ProjectileUtil.createArrow(mob.getEntity(), attackMult, projectileSpeed, 0, 0.17);
    projectiles--;

    for (int i = projectiles; i > 0; i--) {
      ProjectileUtil.createArrow(mob.getEntity(), attackMult, projectileSpeed,
          randomOffset(projectiles), 0.17);
    }
    StrifePlugin.getInstance().getSneakManager().tempDisableSneak(mob.getEntity());
    shotId++;
  }

  public static void createArrow(LivingEntity shooter, double attackMult, float power,
      double spread,
      double vertBonus) {
    Vector velocity = getProjectileVelocity(shooter, power, spread, vertBonus);
    Arrow arrow = shooter.getWorld().spawn(shooter.getEyeLocation().clone().add(0, -0.35, 0),
        Arrow.class, e -> e.setVelocity(velocity));
    arrow.setShooter(shooter);
    arrow.setPickupStatus(PickupStatus.CREATIVE_ONLY);

    setProjctileAttackSpeedMeta(arrow, attackMult);
    setProjectileShotIdMeta(arrow);

    if (shooter instanceof Player) {
      if (attackMult > 0.95) {
        arrow.setCritical(true);
      }
      if (((Player) shooter).isSneaking()) {
        ProjectileUtil.setProjectileSneakMeta(arrow);
      }
    }
  }

  public static void createMagicMissile(LivingEntity shooter, double attackMult, float power,
      double spread, double vertBonus, boolean gravity) {
    Vector velocity = getProjectileVelocity(shooter, power, spread, vertBonus);
    ShulkerBullet bullet = shooter.getWorld()
        .spawn(shooter.getEyeLocation().clone().add(0, -0.35, 0),
            ShulkerBullet.class, e -> e.setVelocity(velocity));
    bullet.setShooter(shooter);
    bullet.setGravity(gravity);

    setProjctileAttackSpeedMeta(bullet, attackMult);
    setProjectileShotIdMeta(bullet);

    if (shooter instanceof Player && ((Player) shooter).isSneaking()) {
      setProjectileSneakMeta(bullet);
    }
  }

  public static Vector getProjectileVelocity(LivingEntity shooter, float speed, double spread,
      double verticalBonus) {
    return getProjectileVelocity(shooter, speed, spread, verticalBonus, false);
  }

  public static Vector getProjectileVelocity(LivingEntity shooter, float speed, double spread,
      double verticalBonus, boolean zeroPitch) {
    Vector vector = shooter.getEyeLocation().getDirection();
    if (zeroPitch) {
      vector.setY(0);
      vector.normalize();
    }
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

  public static void setProjectileShotIdMeta(Projectile proj) {
    proj.setMetadata(SHOT_ID_META, new FixedMetadataValue(StrifePlugin.getInstance(), shotId));
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

  private static double randomOffset(double magnitude) {
    return 0.11 + magnitude * 0.005;
  }

  private static double randomWandOffset(double magnitude) {
    return 0.12 + magnitude * 0.005;
  }
}
