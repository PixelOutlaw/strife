package land.face.strife.util;

import com.tealcube.minecraft.bukkit.facecore.utilities.ItemUtils;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.Dummy;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.effects.CreateModelAnimation;
import land.face.strife.data.effects.Effect;
import land.face.strife.stats.StrifeStat;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class ProjectileUtil {

  private static int shotId = 1;

  public static final Set<UUID> HANDLED_PROJECTILE = new HashSet<>();

  private static final Map<Projectile, Boolean> CONTACT_TRIGGER = new WeakHashMap<>();
  private static final Map<Projectile, Float> ATTACK_MULT = new WeakHashMap<>();
  private static final Map<Projectile, Boolean> APPLY_ON_HIT = new WeakHashMap<>();
  private static final Map<Projectile, List<Effect>> HIT_EFFECTS = new WeakHashMap<>();
  private static final Map<Projectile, Integer> SHOT_ID = new WeakHashMap<>();
  private static final Map<Projectile, Integer> PEIRCE_LEVEL = new WeakHashMap<>();
  private static final Map<Projectile, Boolean> ABILITY_PROJECTILE = new WeakHashMap<>();
  private static final Map<Projectile, Boolean> DESPAWN_ON_CONTACT = new WeakHashMap<>();

  private static final ItemStack wandProjectile = buildWandProjectile();
  private static final ItemStack bulletProjectile = buildBulletProjectile();

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

  public static void setPierce(Projectile projectile, float chance) {
    setPierce(projectile, chance, 0);
  }

  public static void setPierce(Projectile projectile, float chance, int flatBonus) {
    if (chance < 0.1 && flatBonus <= 0) {
      return;
    }
    int maxPuncture = 0;
    while (maxPuncture < 3) {
      if (StrifePlugin.RNG.nextDouble() > chance) {
        break;
      }
      maxPuncture++;
    }
    maxPuncture += flatBonus;
    if (maxPuncture < 1) {
      return;
    }
    PEIRCE_LEVEL.put(projectile, maxPuncture);
    if (projectile instanceof AbstractArrow a) {
      //a.setShotFromCrossbow(true);
      a.setPierceLevel(100);
    }
  }

  public static int getPierce(Projectile projectile) {
    return PEIRCE_LEVEL.getOrDefault(projectile, 0);
  }

  public static void reducePierce(Projectile projectile) {
    int amount = PEIRCE_LEVEL.getOrDefault(projectile, 0);
    if (amount > 0) {
      PEIRCE_LEVEL.put(projectile, amount - 1);
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

  public static void disableCollision(Projectile projectile, LivingEntity entity) {
    entity.getCollidableExemptions().add(projectile.getUniqueId());
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () ->
        entity.getCollidableExemptions().remove(projectile.getUniqueId()), 400L);
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
    int projectiles = ProjectileUtil.getTotalProjectiles(1, mob.getStat(StrifeStat.MULTISHOT) * attackMult,
        mob.getStat(StrifeStat.EXTRA_PROJECTILES));
    float pierceChance = mob.getStat(StrifeStat.PIERCE_CHANCE) / 100;

    ProjectileUtil.createMagicMissile(mob.getEntity(), attackMult, projectileSpeed, pierceChance, 0.03);
    projectiles--;

    for (int i = projectiles; i > 0; i--) {
      ProjectileUtil.createMagicMissile(mob.getEntity(), attackMult, projectileSpeed, pierceChance,
          randomWandOffset(projectiles));
    }

    mob.getEntity().getWorld().playSound(mob.getEntity().getLocation(), Sound.ENTITY_BLAZE_HURT, 1f, 2f);
    shotId++;
  }

  public static void shootBullet(StrifeMob mob, float attackMult) {
    if (mob.getEntity() instanceof Player) {
      if (((Player) mob.getEntity()).getCooldown(Material.BOW) > 0) {
        return;
      }
      ((Player) mob.getEntity()).setCooldown(Material.BOW, (int) (StatUtil.getAttackTime(mob) * 20));
    }
    float speedMult = 1 + mob.getStat(StrifeStat.PROJECTILE_SPEED) / 100;
    float projectileSpeed = 1.25f + 3.00f * speedMult;
    int projectiles = ProjectileUtil.getTotalProjectiles(
        1, mob.getStat(StrifeStat.MULTISHOT) * attackMult, mob.getStat(StrifeStat.EXTRA_PROJECTILES));
    float pierceChance = mob.getStat(StrifeStat.PIERCE_CHANCE) / 100;

    ProjectileUtil.createBullet(mob.getEntity(), attackMult, projectileSpeed, speedMult, pierceChance, 0.03);
    projectiles--;

    for (int i = projectiles; i > 0; i--) {
      ProjectileUtil.createBullet(mob.getEntity(), attackMult, projectileSpeed, speedMult, pierceChance,
          randomBulletOffset(projectiles));
    }
    mob.getEntity().getWorld().playSound(mob.getEntity().getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 2.5f, 0.6f);
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
    float pitch = 0.95f + (float) Math.random() * 0.1f;
    mob.getEntity().getWorld().playSound(mob.getEntity().getLocation(), Sound.ITEM_CROSSBOW_SHOOT, 1f, pitch);
    shotId++;
  }

  public static void createArrow(LivingEntity shooter, double attackMult, float power,
      float pierceChance, double spread, double vertBonus) {
    Vector velocity = getProjectileVelocity(shooter, power, spread, vertBonus);
    Location location = shooter.getEyeLocation().clone().add(0, -0.35, 0);
    location.setDirection(velocity);
    HANDLED_PROJECTILE.add(shooter.getUniqueId());
    Arrow arrow = location.getWorld().spawn(location, Arrow.class, e -> {
      e.setShooter(shooter);
      e.setHasLeftShooter(false);
      e.getLocation().setDirection(shooter.getEyeLocation().getDirection());
      e.getBoundingBox().expand(1);
      e.setPickupStatus(PickupStatus.CREATIVE_ONLY);
      e.teleport(location, TeleportCause.PLUGIN);
      e.setVelocity(velocity);

      var direction = velocity.normalize();
      double hLen = Math.sqrt(direction.getX() * direction.getX() + direction.getZ() * direction.getZ());
      e.setRotation(
          (float) Math.toDegrees(Math.atan2(direction.getX(), direction.getZ())),
          (float) Math.toDegrees(Math.atan2(direction.getY(), hLen)));
    });
    HANDLED_PROJECTILE.remove(shooter.getUniqueId());

    setPierce(arrow, pierceChance);
    setAttackMult(arrow, (float) attackMult);
    setShotId(arrow);
  }

  public static void createMagicMissile(LivingEntity shooter, double attackMult, float power,
      float pierceChance, double spread) {
    Vector velocity = getProjectileVelocity(shooter, power, spread, 0);
    HANDLED_PROJECTILE.add(shooter.getUniqueId());
    Snowball arrow = shooter.getWorld().spawn(shooter.getEyeLocation().clone().add(0, -0.35, 0), Snowball.class, e -> {
      e.setShooter(shooter);
      e.setVelocity(velocity);
      //e.setPickupStatus(PickupStatus.CREATIVE_ONLY);
      e.setItem(wandProjectile);
      e.setGravity(false);
      e.setSilent(true);
    });
    HANDLED_PROJECTILE.remove(shooter.getUniqueId());
    //DisguiseAPI.disguiseToAll(arrow, wandDisguise);
    arrow.getBoundingBox().expand(1);

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

  public static void createBullet(LivingEntity shooter, double attackMult, float power, float speedMult, float pierce,
      double spread) {
    Vector velocity = getProjectileVelocity(shooter, power, spread, 0.075);
    Location location = shooter.getEyeLocation().clone().add(0, -0.20, 0);
    location.setDirection(velocity);
    HANDLED_PROJECTILE.add(shooter.getUniqueId());
    Arrow arrow = shooter.getWorld().spawn(location, Arrow.class, e -> {
      e.setShooter(shooter);
      e.setVelocity(velocity);
      e.setHasLeftShooter(false);
      e.setSilent(true);
      e.setVisibleByDefault(false);
      e.setPickupStatus(PickupStatus.CREATIVE_ONLY);
    });
    HANDLED_PROJECTILE.remove(shooter.getUniqueId());
    arrow.setPickupStatus(PickupStatus.CREATIVE_ONLY);
    arrow.getBoundingBox().expand(1);

    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> {
      if (arrow.isValid()) {
        arrow.remove();
      }
    }, 7);

    setAttackMult(arrow, (float) attackMult);
    setPierce(arrow, pierce);
    setShotId(arrow);
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
      case ARROW, EXPERIENCE_BOTTLE, POTION, WITHER_SKULL, SHULKER_BULLET, TNT, SMALL_FIREBALL,
           LLAMA_SPIT, SPECTRAL_ARROW, TRIDENT, FIREBALL, DRAGON_FIREBALL, EGG, SNOWBALL -> true;
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
    return stack;
  }

  private static ItemStack buildBulletProjectile() {
    ItemStack stack = new ItemStack(Material.POLISHED_BLACKSTONE_BUTTON);
    ItemUtils.setItemModel(stack, "faceland:icons/empty");
    return stack;
  }

  public static void setupModelledProjectile(String modelId, StrifeMob caster, Projectile proj,
      Location loc, Vector direction, Color color, double size, boolean thrownItem) {
    ActiveModel model = ModelEngineAPI.createActiveModel(modelId);
    if (model == null) {
      Bukkit.getLogger().warning("[Strife] Failed to create model animation! No model!" + modelId);
    } else {
      loc = loc.clone();
      loc.setDirection(direction);
      Dummy<?> dummy = new Dummy<>();
      dummy.setLocation(loc);
      ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(dummy);
      if (modeledEntity == null) {
        dummy.setRemoved(true);
        Bukkit.getLogger().warning("Failed to create modelled entity");
      } else {
        if (color != null) {
          model.setDefaultTint(color);
        }
        model.setScale(size);
        modeledEntity.addModel(model, true);
        //modeledEntity.detectPlayers();
        dummy.getBodyRotationController().setRotationDuration(0);
        dummy.getBodyRotationController().setRotationDelay(0);
        dummy.setYBodyRot(loc.getYaw());
        dummy.setXHeadRot(loc.getPitch());
        dummy.setYHeadRot(loc.getYaw());
        dummy.setWalking(true);

        Bukkit.getOnlinePlayers().forEach(p -> p.hideEntity(StrifePlugin.getInstance(), proj));

        boolean arrowBase = proj instanceof AbstractArrow;
        Bukkit.getScheduler().runTaskTimer(StrifePlugin.getInstance(), (task) -> {
          if (!proj.isValid()) {
            dummy.setRemoved(true);
            task.cancel();
            return;
          }
          if (arrowBase && ((AbstractArrow) proj).isInBlock()) {
            if (dummy.isWalking()) {
              dummy.setWalking(false);
              dummy.setLocation(proj.getLocation());
            }
            return;
          }
          dummy.setWalking(true);
          Location projLoc = proj.getLocation().clone();
          projLoc.setDirection(proj.getVelocity());
          projLoc.add(proj.getVelocity());
          dummy.setLocation(projLoc);
          dummy.setYBodyRot(projLoc.getYaw());
          dummy.setXHeadRot(projLoc.getPitch());
          dummy.setYHeadRot(projLoc.getYaw());
        }, 0L, 1L);
      }

      if (thrownItem) {
        CreateModelAnimation.equipModel(caster, model);
      }
    }
  }
}
