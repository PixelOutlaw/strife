package land.face.strife.util;

import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.effects.Effect;
import land.face.strife.stats.StrifeStat;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MiscDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.SnowballWatcher;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.AbstractArrow;
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
  private static final Map<Projectile, Boolean> APPLY_ON_HIT = new WeakHashMap<>();
  private static final Map<Projectile, List<Effect>> HIT_EFFECTS = new WeakHashMap<>();
  private static final Map<Projectile, Integer> SHOT_ID = new WeakHashMap<>();
  private static final Map<Projectile, Boolean> ABILITY_PROJECTILE = new WeakHashMap<>();
  private static final Map<Projectile, Boolean> DESPAWN_ON_CONTACT = new WeakHashMap<>();

  private static final ItemStack wandProjectile = buildWandProjectile();
  private static final ItemStack bulletProjectile = buildBulletProjectile();
  private static final Disguise wandDisguise = buildWandDisguise();
  private static final Disguise bulletDisguise = buildBulletDisguise();

  public static void setContactTrigger(Projectile projectile) {
    CONTACT_TRIGGER.put(projectile, true);
  }

  public static boolean isContactTrigger(Projectile projectile) {
    return CONTACT_TRIGGER.containsKey(projectile);
  }

  public static void setAbilityProjectile(Projectile projectile) {
    ABILITY_PROJECTILE.put(projectile, true);
  }

  public static boolean isAbilityProjectile(Projectile projectile) {
    return ABILITY_PROJECTILE.containsKey(projectile);
  }

  public static void setAttackMult(Projectile projectile, float mult) {
    ATTACK_MULT.put(projectile, mult);
  }

  public static float getAttackMult(Projectile projectile) {
    return ATTACK_MULT.getOrDefault(projectile, 1f);
  }

  public static void setApplyOnHit(Projectile projectile, boolean applyOnHit) {
    APPLY_ON_HIT.put(projectile, applyOnHit);
  }

  public static boolean getApplyOnHit(Projectile projectile) {
    return APPLY_ON_HIT.getOrDefault(projectile, false);
  }

  public static boolean isDespawnOnContact(Projectile projectile) {
    return DESPAWN_ON_CONTACT.getOrDefault(projectile, false);
  }

  public static boolean removeDespawnOnContact(Projectile projectile) {
    return DESPAWN_ON_CONTACT.remove(projectile);
  }

  public static void setPierce(AbstractArrow arrow, float chance) {
    setPierce(arrow, chance, 0);
  }

  public static void setPierce(AbstractArrow arrow, float chance, int flatBonus) {
    if (chance > 0) {
      int maxPuncture = 0;
      while (maxPuncture < 3) {
        if (StrifePlugin.RNG.nextDouble() > chance) {
          break;
        }
        maxPuncture++;
      }
      maxPuncture += flatBonus;
      if (maxPuncture > 0) {
        arrow.setShotFromCrossbow(true);
        arrow.setPierceLevel(maxPuncture);
      }
    }
  }

  private static Disguise buildWandDisguise() {
    MiscDisguise miscDisguise = new MiscDisguise(DisguiseType.SNOWBALL);
    miscDisguise.setReplaceSounds(true);
    miscDisguise.setVelocitySent(true);
    FlagWatcher watcher = miscDisguise.getWatcher();
    ((SnowballWatcher) watcher).setItemStack(wandProjectile);
    return miscDisguise;
  }

  private static Disguise buildBulletDisguise() {
    MiscDisguise miscDisguise = new MiscDisguise(DisguiseType.SNOWBALL);
    miscDisguise.setReplaceSounds(true);
    miscDisguise.setVelocitySent(true);
    FlagWatcher watcher = miscDisguise.getWatcher();
    ((SnowballWatcher) watcher).setItemStack(bulletProjectile);
    return miscDisguise;
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

  public static void disableCollision(Projectile projectile, LivingEntity entity) {
    entity.getCollidableExemptions().add(projectile.getUniqueId());
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () ->
        entity.getCollidableExemptions().remove(projectile.getUniqueId()),200L);
  }

  public static int getTotalProjectiles(double initialProjectiles, float multiShot, float extraProjectiles) {
    double projectiles = initialProjectiles;
    if (multiShot > 0) {
      projectiles *= 1 + (multiShot / 100);
      if (projectiles % 1 >= StrifePlugin.RNG.nextDouble()) {
        projectiles++;
      }
      projectiles += extraProjectiles;
      return (int) projectiles;
    }
    return (int) initialProjectiles + (int) extraProjectiles;
  }

  public static void shootWand(StrifeMob mob, float attackMult) {
    if (attackMult < 0.15f) {
      return;
    }
    // x / 117.647 = 0.85
    float projectileSpeed = 1.02f + mob.getStat(StrifeStat.PROJECTILE_SPEED) / 100f;
    int projectiles = ProjectileUtil.getTotalProjectiles(1, mob.getStat(StrifeStat.MULTISHOT) * attackMult, mob.getStat(StrifeStat.EXTRA_PROJECTILES));
    float pierceChance = mob.getStat(StrifeStat.PIERCE_CHANCE) / 100;

    ProjectileUtil.createMagicMissile(mob.getEntity(), attackMult, projectileSpeed, pierceChance, 0.03);
    projectiles--;

    for (int i = projectiles; i > 0; i--) {
      ProjectileUtil.createMagicMissile(mob.getEntity(), attackMult, projectileSpeed, pierceChance, randomWandOffset(projectiles));
    }

    mob.getEntity().getWorld().playSound(mob.getEntity().getLocation(), Sound.ENTITY_BLAZE_HURT, 1f, 2f);
    shotId++;
  }

  public static void shootBullet(StrifeMob mob, float attackMult) {
    if (mob.getEntity() instanceof Player) {
      if (((Player) mob.getEntity()).getCooldown(Material.BOW) > 0) {
        return;
      }
      ((Player) mob.getEntity()).setCooldown(Material.BOW,
          (int) (StatUtil.getAttackTime(mob) * 20));
    }
    // Divided by 50 instead of 4.5f*X/100, because TOO MUCH SPEED
    float projectileSpeed = 4.5f + mob.getStat(StrifeStat.PROJECTILE_SPEED) / 50;
    int projectiles = ProjectileUtil.getTotalProjectiles(
        1, mob.getStat(StrifeStat.MULTISHOT) * attackMult, mob.getStat(StrifeStat.EXTRA_PROJECTILES));

    ProjectileUtil.createBullet(mob.getEntity(), attackMult, projectileSpeed, 0.03);
    projectiles--;

    for (int i = projectiles; i > 0; i--) {
      ProjectileUtil.createBullet(mob.getEntity(), attackMult, projectileSpeed,
          randomBulletOffset(projectiles));
    }
    mob.getEntity().getWorld().playSound(mob.getEntity().getLocation(),
        Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 0.6f);
    shotId++;
  }

  public static void shootArrow(StrifeMob mob, float attackMult) {
    float projectileSpeed = 1.65f * (1 + (mob.getStat(StrifeStat.PROJECTILE_SPEED) / 100));
    float pierceChance = mob.getStat(StrifeStat.PIERCE_CHANCE) / 100;
    int projectiles = ProjectileUtil.getTotalProjectiles(
        1, mob.getStat(StrifeStat.MULTISHOT) * attackMult, mob.getStat(StrifeStat.EXTRA_PROJECTILES));

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
    Arrow arrow = shooter.getWorld().spawn(shooter.getEyeLocation().clone().add(0, -0.35, 0), Arrow.class, e -> {
      e.setVelocity(velocity);
      e.setPickupStatus(PickupStatus.CREATIVE_ONLY);
    });
    arrow.setShooter(shooter);
    arrow.setPickupStatus(PickupStatus.CREATIVE_ONLY);
    arrow.getBoundingBox().expand(0.35);

    setPierce(arrow, pierceChance);
    setAttackMult(arrow, (float) attackMult);
    setShotId(arrow);
  }

  public static void createMagicMissile(LivingEntity shooter, double attackMult, float power,
      float pierceChance, double spread) {
    Vector velocity = getProjectileVelocity(shooter, power, spread, 0);
    Snowball arrow = shooter.getWorld().spawn(shooter.getEyeLocation().clone().add(0, -0.35, 0), Snowball.class, e -> {
      e.setVelocity(velocity);
      //e.setPickupStatus(PickupStatus.CREATIVE_ONLY);
      e.setItem(wandProjectile);
      e.setGravity(false);
      e.setSilent(true);
    });
    //DisguiseAPI.disguiseToAll(arrow, wandDisguise);

    arrow.setShooter(shooter);
    arrow.getBoundingBox().expand(0.35);

    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> {
      if (arrow.isValid()) {
        arrow.remove();
      }
    }, 10 + (int) (14f * attackMult));

    // DESPAWN_ON_CONTACT.put(arrow, true);
    // setPierce(arrow, pierceChance);
    setAttackMult(arrow, (float) attackMult);
    setShotId(arrow);
  }

  public static void createBullet(LivingEntity shooter, double attackMult, float power, double spread) {
    Vector velocity = getProjectileVelocity(shooter, power, spread, 0);
    Snowball bullet = shooter.getWorld().spawn(shooter.getEyeLocation().clone(), Snowball.class, e -> {
      e.setVelocity(velocity);
      e.setItem(bulletProjectile);
      e.setGravity(false);
    });
    bullet.setShooter(shooter);
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> {
      if (bullet.isValid()) {
        bullet.remove();
      }
    }, 6);
    bullet.getBoundingBox().expand(0.35);

    setAttackMult(bullet, (float) attackMult);
    setShotId(bullet);
  }

  public static Vector getProjectileVelocity(LivingEntity shooter, float speed, double spread, double verticalBonus) {
    return getProjectileVelocity(shooter.getLocation().getDirection(), speed, spread, verticalBonus, false);
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
    double xOff = -spread + spread * 2 * StrifePlugin.RNG.nextFloat();
    double yOff = -spread + spread * 2 * StrifePlugin.RNG.nextFloat();
    double zOff = -spread + spread * 2 * StrifePlugin.RNG.nextFloat();
    return direction.add(new Vector(xOff, verticalBonus + yOff, zOff));
  }

  public static void createTrident(Player shooter, Trident trident, float attackMult,
      double power) {
    Vector vector = trident.getVelocity().multiply(power);
    Trident newTrident = shooter.getWorld().spawn(trident.getLocation(),
        Trident.class, e -> e.setVelocity(vector));
    newTrident.setShooter(shooter);
    newTrident.setPickupStatus(PickupStatus.CREATIVE_ONLY);
    setAttackMult(newTrident, attackMult);
    setShotId(newTrident);
  }

  public static boolean isProjectile(EntityType entityType) {
    return switch (entityType) {
      case ARROW, THROWN_EXP_BOTTLE, SPLASH_POTION, WITHER_SKULL, SHULKER_BULLET, PRIMED_TNT, SMALL_FIREBALL, LLAMA_SPIT, SPECTRAL_ARROW, TRIDENT, FIREBALL, DRAGON_FIREBALL, EGG, SNOWBALL -> true;
      default -> false;
    };
  }

  private static float randomOffset(float magnitude) {
    return 0.14f + magnitude * 0.01f;
  }

  private static float randomWandOffset(float magnitude) {
    return 0.12f + magnitude * 0.01f;
  }

  private static float randomBulletOffset(float magnitude) {
    return 0.25f + magnitude * 0.035f;
  }

  private static ItemStack buildWandProjectile() {
    ItemStack stack = new ItemStack(Material.NETHER_STAR);
    ItemStackExtensionsKt.setCustomModelData(stack, 100);
    return stack;
  }

  private static ItemStack buildBulletProjectile() {
    ItemStack stack = new ItemStack(Material.POLISHED_BLACKSTONE_BUTTON);
    ItemStackExtensionsKt.setCustomModelData(stack, 100);
    return stack;
  }
}
