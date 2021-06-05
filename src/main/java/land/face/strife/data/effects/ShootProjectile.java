package land.face.strife.data.effects;

import java.util.ArrayList;
import java.util.List;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.tasks.ThrownItemTask;
import land.face.strife.util.DamageUtil.OriginLocation;
import land.face.strife.util.ProjectileUtil;
import land.face.strife.util.SpecialStatusUtil;
import land.face.strife.util.TargetingUtil;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.ThrowableProjectile;
import org.bukkit.entity.WitherSkull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class ShootProjectile extends Effect {

  private EntityType projectileEntity;
  private OriginLocation originType;
  private Color arrowColor;
  private double attackMultiplier;
  private boolean targeted;
  private boolean seeking;
  private int quantity;
  private float speed;
  private double spread;
  private double radialAngle;
  private double verticalBonus;
  private boolean ignoreMultishot;
  private boolean bounce;
  private boolean ignite;
  private boolean zeroPitch;
  private boolean blockHitEffects;
  private boolean silent;
  private boolean gravity;
  private float yield;
  private int maxDuration;
  private Disguise disguise = null;
  private ItemStack thrownStack = null;
  private final List<Effect> hitEffects = new ArrayList<>();
  private boolean throwItem;
  private boolean throwSpin;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    int projectiles = getProjectileCount(caster);
    float newSpeed = speed * (1 + caster.getStat(StrifeStat.PROJECTILE_SPEED) / 100);
    Location originLocation = TargetingUtil.getOriginLocation(caster.getEntity(), originType);

    double startAngle = 0;
    if (radialAngle != 0) {
      startAngle = -radialAngle / 2;
    }
    double newSpread = projectiles == 1 ? spread : spread * Math.pow(projectiles, 0.5);
    for (int i = 0; i < projectiles; i++) {
      Vector direction;
      if (targeted && target != null) {
        direction = target.getEntity().getLocation().toVector()
            .subtract(caster.getEntity().getLocation().toVector()).normalize();
      } else {
        direction = caster.getEntity().getEyeLocation().getDirection();
      }
      Vector velocity = ProjectileUtil
          .getProjectileVelocity(direction, newSpeed, newSpread, verticalBonus, zeroPitch);
      if (radialAngle != 0) {
        applyRadialAngles(velocity, startAngle, projectiles, i);
      }

      assert projectileEntity.getEntityClass() != null;
      Projectile projectile = (Projectile) originLocation.getWorld().spawn(originLocation,
          projectileEntity.getEntityClass(), e -> e.setVelocity(velocity));
      projectile.setShooter(caster.getEntity());

      if (silent) {
        projectile.setSilent(true);
      }

      if (projectile instanceof ThrowableProjectile && thrownStack != null) {
        ((ThrowableProjectile) projectile).setItem(thrownStack);
      }
      if (ignite) {
        projectile.setFireTicks(200);
      }
      if (!gravity) {
        projectile.setGravity(false);
      }
      if (projectileEntity == EntityType.ARROW) {
        if (arrowColor != null) {
          ((Arrow) projectile).setColor(arrowColor);
        }
        ((Arrow) projectile).setCritical(attackMultiplier > 0.95);
        ((Arrow) projectile).setPickupStatus(PickupStatus.CREATIVE_ONLY);
        ProjectileUtil.setPierce((Arrow) projectile, caster.getStat(StrifeStat.PIERCE_CHANCE) / 100);
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
      projectile.setBounce(bounce);
      ProjectileUtil.setAttackMult(projectile, (float) attackMultiplier);
      if (blockHitEffects) {
        ProjectileUtil.setContactTrigger(projectile);
      }
      if (!hitEffects.isEmpty()) {
        ProjectileUtil.setHitEffects(projectile, hitEffects);
      }
      if (disguise != null) {
        DisguiseAPI.disguiseToPlayers(projectile, disguise, Bukkit.getOnlinePlayers());
      }

      SpecialStatusUtil.setDespawnOnUnload(projectile);
      ProjectileUtil.setShotId(projectile);

      if (maxDuration != -1) {
        Bukkit.getScheduler().runTaskLater(getPlugin(), () -> {
          if (projectile.isValid()) {
            for (Effect effect : hitEffects) {
              if (effect instanceof LocationEffect) {
                ((LocationEffect) effect).applyAtLocation(caster, projectile.getLocation());
              }
            }
            projectile.remove();
          }
        }, maxDuration);
      }

      if (throwItem) {
        for (Player p : Bukkit.getOnlinePlayers()) {
          getPlugin().getEntityHider().hideEntity(p, projectile);
        }
        ItemStack stack = caster.getEntity().getEquipment().getItemInMainHand();
        if (stack.getType() == Material.AIR) {
          stack = caster.getEntity().getEquipment().getItemInOffHand();
          if (stack.getType() == Material.AIR) {
            stack = new ItemStack(Material.IRON_SWORD);
          }
        }
        new ThrownItemTask(projectile, stack, originLocation, throwSpin).runTaskTimer(getPlugin(), 0L, 1L);
      }
    }
    ProjectileUtil.bumpShotId();
  }

  public void setProjectileEntity(EntityType projectileEntity) {
    this.projectileEntity = projectileEntity;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  public void setSpeed(float speed) {
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

  public void setBounce(boolean bounce) {
    this.bounce = bounce;
  }

  public void setIgnite(boolean ignite) {
    this.ignite = ignite;
  }

  public void setYield(float yield) {
    this.yield = yield;
  }

  public void setMaxDuration(int maxDuration) {
    this.maxDuration = maxDuration;
  }

  public void setTargeted(boolean targeted) {
    this.targeted = targeted;
  }

  public void setSeeking(boolean seeking) {
    this.seeking = seeking;
  }

  public void setThrowItem(boolean throwItem) {
    this.throwItem = throwItem;
  }

  public void setZeroPitch(boolean zeroPitch) {
    this.zeroPitch = zeroPitch;
  }

  public void setBlockHitEffects(boolean blockHitEffects) {
    this.blockHitEffects = blockHitEffects;
  }

  public void setSilent(boolean silent) {
    this.silent = silent;
  }

  public void setGravity(boolean gravity) {
    this.gravity = gravity;
  }

  public void setOriginType(OriginLocation originType) {
    this.originType = originType;
  }

  public void setArrowColor(Color arrowColor) {
    this.arrowColor = arrowColor;
  }

  public void setAttackMultiplier(double attackMultiplier) {
    this.attackMultiplier = attackMultiplier;
  }

  public void setThrowSpin(boolean throwSpin) {
    this.throwSpin = throwSpin;
  }

  private Vector getCastDirection(LivingEntity caster, LivingEntity target) {
    Vector direction;
    if (targeted) {
      direction = target.getLocation().toVector().subtract(
          caster.getLocation().toVector()).normalize();
    } else {
      direction = caster.getEyeLocation().getDirection();
    }
    return direction;
  }

  public void setDisguise(Disguise disguise) {
    this.disguise = disguise;
  }

  public void setThrownStack(ItemStack thrownStack) {
    this.thrownStack = thrownStack;
  }

  public List<Effect> getHitEffects() {
    return hitEffects;
  }

  private int getProjectileCount(StrifeMob caster) {
    if (ignoreMultishot || projectileEntity == EntityType.FIREBALL) {
      return 1;
    }
    return ProjectileUtil.getTotalProjectiles(quantity, caster.getStat(StrifeStat.MULTISHOT));
  }

  private void applyRadialAngles(Vector direction, double angle, int projectiles, int counter) {
    if (projectiles == 1) {
      return;
    }
    angle = Math.toRadians(angle + counter * (radialAngle / (projectiles - 1)));
    double x = direction.getX();
    double z = direction.getZ();
    direction.setZ(z * Math.cos(angle) - x * Math.sin(angle));
    direction.setX(z * Math.sin(angle) + x * Math.cos(angle));
  }
}