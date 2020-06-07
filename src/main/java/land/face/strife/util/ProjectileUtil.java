package land.face.strife.util;

import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;
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
import org.bukkit.util.Vector;

public class ProjectileUtil {

  private static int shotId = 1;

  private static final Map<Projectile, Boolean> GROUND_TRIGGER = new WeakHashMap<>();
  private static final Map<Projectile, Float> ATTACK_MULT = new WeakHashMap<>();
  private static final Map<Projectile, String> HIT_EFFECTS = new WeakHashMap<>();
  private static final Map<Projectile, Integer> SHOT_ID = new WeakHashMap<>();

  private static final Random RANDOM = new Random(System.currentTimeMillis());

  public static void setGroundTrigger(Projectile projectile) {
    GROUND_TRIGGER.put(projectile, true);
  }

  public static boolean isGroundTrigger(Projectile projectile) {
    return GROUND_TRIGGER.containsKey(projectile);
  }

  public static void setAttackMult(Projectile projectile, float mult) {
    ATTACK_MULT.put(projectile, mult);
  }

  public static float getAttackMult(Projectile projectile) {
    return ATTACK_MULT.getOrDefault(projectile, 1f);
  }

  public static void setShotId(Projectile projectile) {
    SHOT_ID.put(projectile, shotId);
  }

  public static int getShotId(Projectile projectile) {
      return SHOT_ID.getOrDefault(projectile, 0);
  }

  public static void setHitEffects(Projectile projectile, String effectString) {
    HIT_EFFECTS.put(projectile, effectString);
  }

  public static String getHitEffects(Projectile projectile) {
    return HIT_EFFECTS.get(projectile);
  }

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
    float projectileSpeed = 1.0f * (1 + mob.getStat(StrifeStat.PROJECTILE_SPEED) / 100);
    int projectiles = ProjectileUtil.getTotalProjectiles(1, mob.getStat(StrifeStat.MULTISHOT));

    ProjectileUtil.createMagicMissile(mob.getEntity(), attackMult, projectileSpeed, 0, 0.19, true);
    projectiles--;

    for (int i = projectiles; i > 0; i--) {
      ProjectileUtil.createMagicMissile(mob.getEntity(), attackMult, projectileSpeed,
          randomWandOffset(projectiles), 0.24, true);
    }

    mob.getEntity().getWorld()
        .playSound(mob.getEntity().getLocation(), Sound.ENTITY_BLAZE_HURT, 0.7f, 2f);
    shotId++;
  }

  public static void shootArrow(StrifeMob mob, float attackMult) {
    float projectileSpeed = 1.75f * (1 + (mob.getStat(StrifeStat.PROJECTILE_SPEED) / 100));
    int projectiles = ProjectileUtil.getTotalProjectiles(1, mob.getStat(StrifeStat.MULTISHOT));

    ProjectileUtil.createArrow(mob.getEntity(), attackMult, projectileSpeed, 0, 0.165);
    projectiles--;

    for (int i = projectiles; i > 0; i--) {
      ProjectileUtil.createArrow(mob.getEntity(), attackMult, projectileSpeed,
          randomOffset(projectiles), 0.185);
    }
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

    setAttackMult(arrow, (float) attackMult);
    setShotId(arrow);

    if (shooter instanceof Player) {
      if (attackMult > 0.95) {
        arrow.setCritical(true);
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

    setAttackMult(bullet, (float) attackMult);
    setShotId(bullet);
  }

  public static Vector getProjectileVelocity(LivingEntity shooter, float speed, double spread,
      double verticalBonus) {
    return getProjectileVelocity(shooter.getEyeLocation().getDirection(), speed, spread, verticalBonus, false);
  }

  public static Vector getProjectileVelocity(Vector direction, float speed, double spread,
      double verticalBonus, boolean zeroPitch) {
    if (zeroPitch) {
      direction.setY(0);
      direction.normalize();
    }
    direction.multiply(speed);
    if (spread == 0) {
      return direction.add(new Vector(0, verticalBonus, 0));
    }
    double xOff = -spread + spread * 2 * Math.random();
    double yOff = -spread + spread * 2 * Math.random();
    double zOff = -spread + spread * 2 * Math.random();
    return direction.add(new Vector(xOff, verticalBonus + yOff, zOff));
  }

  public static void createTrident(Player shooter, Trident trident, float attackMult,
      double power) {
    Vector vector = trident.getVelocity().multiply(power);
    Trident newTrident = shooter.getWorld()
        .spawn(trident.getLocation(), Trident.class, e -> e.setVelocity(vector));
    newTrident.setShooter(shooter);
    newTrident.setPickupStatus(PickupStatus.CREATIVE_ONLY);
    setAttackMult(trident, attackMult);
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
