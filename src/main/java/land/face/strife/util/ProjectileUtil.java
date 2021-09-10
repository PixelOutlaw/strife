package land.face.strife.util;

import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.effects.Effect;
import land.face.strife.stats.StrifeStat;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Trident;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class ProjectileUtil {

  private static int shotId = 1;

  private static final Map<Projectile, Boolean> CONTACT_TRIGGER = new WeakHashMap<>();
  private static final Map<Projectile, Float> ATTACK_MULT = new WeakHashMap<>();
  private static final Map<Projectile, List<Effect>> HIT_EFFECTS = new WeakHashMap<>();
  private static final Map<Projectile, Integer> SHOT_ID = new WeakHashMap<>();

  private static final ItemStack wandProjectile = buildWandProjectile();

  private static final Random RANDOM = new Random(System.currentTimeMillis());

  public static void setContactTrigger(Projectile projectile) {
    CONTACT_TRIGGER.put(projectile, true);
  }

  public static boolean isContactTrigger(Projectile projectile) {
    return CONTACT_TRIGGER.containsKey(projectile);
  }

  public static void setAttackMult(Projectile projectile, float mult) {
    ATTACK_MULT.put(projectile, mult);
  }

  public static float getAttackMult(Projectile projectile) {
    return ATTACK_MULT.getOrDefault(projectile, 1f);
  }

  public static void setPierce(Arrow arrow, float chance) {
    if (chance > 0) {
      int maxPuncture = 0;
      while (maxPuncture < 3) {
        if (RANDOM.nextDouble() > chance) {
          break;
        }
        maxPuncture++;
      }
      if (maxPuncture > 0) {
        arrow.setShotFromCrossbow(true);
        arrow.setPierceLevel(maxPuncture);
      }
    }
  }

  public static void setShotId(Projectile projectile) {
    SHOT_ID.put(projectile, shotId);
  }

  public static void bumpShotId() {
    shotId++;
  }

  public static int getShotId(Projectile projectile) {
    return SHOT_ID.getOrDefault(projectile, -1);
  }

  public static void setHitEffects(Projectile projectile, List<Effect> effects) {
    HIT_EFFECTS.put(projectile, effects);
  }

  public static List<Effect> getHitEffects(Projectile projectile) {
    return HIT_EFFECTS.getOrDefault(projectile, null);
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
    float projectileSpeed = 0.85f + mob.getStat(StrifeStat.PROJECTILE_SPEED) / 100;
    int projectiles = ProjectileUtil.getTotalProjectiles(1, mob.getStat(StrifeStat.MULTISHOT));

    ProjectileUtil.createMagicMissile(mob.getEntity(), attackMult, projectileSpeed, 0.03, 0.22, true);
    projectiles--;

    for (int i = projectiles; i > 0; i--) {
      ProjectileUtil.createMagicMissile(mob.getEntity(), attackMult, projectileSpeed,
          randomWandOffset(projectiles), 0.22, true);
    }

    mob.getEntity().getWorld().playSound(mob.getEntity().getLocation(), Sound.ENTITY_BLAZE_HURT, 0.7f, 2f);
    shotId++;
  }

  public static void shootArrow(StrifeMob mob, float attackMult) {
    float projectileSpeed = 1.65f * (1 + (mob.getStat(StrifeStat.PROJECTILE_SPEED) / 100));
    float pierceChance = mob.getStat(StrifeStat.PIERCE_CHANCE) / 100;
    int projectiles = ProjectileUtil.getTotalProjectiles(1, mob.getStat(StrifeStat.MULTISHOT));

    ProjectileUtil.createArrow(mob.getEntity(),
        attackMult, projectileSpeed, pierceChance, 0.01, 0.2);
    projectiles--;

    for (int i = projectiles; i > 0; i--) {
      ProjectileUtil.createArrow(mob.getEntity(),
          attackMult, projectileSpeed, pierceChance, randomOffset(projectiles), 0.2);
    }
    shotId++;
  }

  public static void createArrow(LivingEntity shooter, double attackMult, float power,
      float pierceChance, double spread, double vertBonus) {
    Vector velocity = getProjectileVelocity(shooter, power, spread, vertBonus);
    Arrow arrow = shooter.getWorld().spawn(shooter.getEyeLocation().clone().add(0, -0.35, 0),
        Arrow.class, e -> e.setVelocity(velocity));
    arrow.setShooter(shooter);
    arrow.setPickupStatus(PickupStatus.CREATIVE_ONLY);

    setPierce(arrow, pierceChance);
    setAttackMult(arrow, (float) attackMult);
    setShotId(arrow);
  }

  public static void createMagicMissile(LivingEntity shooter, double attackMult, float power,
      double spread, double vertBonus, boolean gravity) {
    Vector velocity = getProjectileVelocity(shooter, power, spread, vertBonus);
    Snowball bullet = shooter.getWorld()
        .spawn(shooter.getEyeLocation().clone().add(0, -0.35, 0), Snowball.class, e -> {
          e.setVelocity(velocity);
          e.setItem(wandProjectile);
        });
    bullet.setShooter(shooter);
    bullet.setGravity(gravity);

    setAttackMult(bullet, (float) attackMult);
    setShotId(bullet);
  }

  public static Vector getProjectileVelocity(LivingEntity shooter, float speed, double spread,
      double verticalBonus) {
    return getProjectileVelocity(shooter.getEyeLocation().getDirection(), speed, spread,
        verticalBonus, false);
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
    setAttackMult(newTrident, attackMult);
  }

  public static boolean isProjectile(EntityType entityType) {
    return switch (entityType) {
      case ARROW, THROWN_EXP_BOTTLE, SPLASH_POTION, WITHER_SKULL, SHULKER_BULLET, PRIMED_TNT, SMALL_FIREBALL, LLAMA_SPIT, SPECTRAL_ARROW, TRIDENT, FIREBALL, DRAGON_FIREBALL, EGG, SNOWBALL -> true;
      default -> false;
    };
  }

  private static float randomOffset(float magnitude) {
    return 0.08f + magnitude * 0.007f;
  }

  private static float randomWandOffset(float magnitude) {
    return 0.09f + magnitude * 0.008f;
  }

  private static ItemStack buildWandProjectile() {
    ItemStack stack = new ItemStack(Material.NETHER_STAR);
    ItemStackExtensionsKt.setCustomModelData(stack, 100);
    return stack;
  }
}
