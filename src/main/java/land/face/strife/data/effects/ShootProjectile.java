package land.face.strife.data.effects;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.tealcube.minecraft.bukkit.facecore.utilities.ChunkUtil;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.BoneBehaviorTypes;
import com.ticxo.modelengine.api.model.bone.type.HeldItem;
import java.util.ArrayList;
import java.util.List;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.DamageUtil.OriginLocation;
import land.face.strife.util.ProjectileUtil;
import land.face.strife.util.TargetingUtil;
import lombok.Getter;
import lombok.Setter;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.WitherSkull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

@Getter @Setter
public class ShootProjectile extends Effect {

  private EntityType projectileEntity;
  private OriginLocation originType;
  private Color arrowColor;
  private String modelId;
  private double attackMultiplier;
  private boolean targeted;
  private boolean seeking;
  private int quantity;
  private int pierceTargets;
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
  @Getter
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
    double newSpread = projectiles == 1 ? 0 : spread * Math.pow(projectiles, 0.5);
    for (int i = 0; i < projectiles; i++) {
      Vector direction;
      if (targeted && target != null) {
        direction = target.getEntity().getLocation().toVector()
            .subtract(caster.getEntity().getLocation().toVector()).normalize();
      } else {
        direction = caster.getEntity().getEyeLocation().getDirection();
      }
      Vector velocity = ProjectileUtil.getProjectileVelocity(direction, newSpeed, newSpread, verticalBonus, zeroPitch);
      if (radialAngle != 0) {
        applyRadialAngles(velocity, startAngle, projectiles, i);
      }

      Location location = originLocation.clone();
      location.setDirection(direction);
      assert projectileEntity.getEntityClass() != null;
      Projectile projectile = (Projectile) location.getWorld().spawn(location, projectileEntity.getEntityClass(), e -> {
        caster.getEntity().getCollidableExemptions().add(e.getUniqueId());
        if (projectileEntity == EntityType.SNOWBALL) {
          ((Snowball) e).setItem(thrownStack);
        }
        e.setVelocity(velocity);
        if (!gravity) {
          e.setGravity(false);
        }
      });
      projectile.setShooter(caster.getEntity());
      Bukkit.getScheduler().runTaskLater(getPlugin(), () ->
          caster.getEntity().getCollidableExemptions().remove(projectile.getUniqueId()), 20L * 30);

      if (silent) {
        projectile.setSilent(true);
      }
      if (ignite) {
        projectile.setFireTicks(200);
      }
      if (projectileEntity == EntityType.ARROW || projectileEntity == EntityType.SPECTRAL_ARROW) {
        if (projectileEntity != EntityType.SPECTRAL_ARROW && arrowColor != null) {
          ((Arrow) projectile).setColor(arrowColor);
        }
        ((AbstractArrow) projectile).setPickupStatus(PickupStatus.CREATIVE_ONLY);
        ProjectileUtil.setPierce((AbstractArrow) projectile,
            caster.getStat(StrifeStat.PIERCE_CHANCE) / 100, pierceTargets);
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
      ProjectileUtil.setApplyOnHit(projectile, true);
      ProjectileUtil.setAbilityProjectile(projectile);
      if (blockHitEffects) {
        ProjectileUtil.setContactTrigger(projectile);
      }
      if (!hitEffects.isEmpty()) {
        ProjectileUtil.setHitEffects(projectile, hitEffects);
      }
      if (disguise != null) {
        DisguiseAPI.disguiseToPlayers(projectile, disguise, Bukkit.getOnlinePlayers());
      }

      ChunkUtil.setDespawnOnUnload(projectile);
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

      if (StringUtils.isNotBlank(modelId)) {
        applyModelStuff(modelId, caster, projectile, location, direction);
      }
    }
    ProjectileUtil.bumpShotId();
  }

  private void applyModelStuff(String modelId, StrifeMob caster, Projectile proj, Location loc, Vector direction) {
    ActiveModel model = ModelEngineAPI.createActiveModel(modelId);
    if (model == null) {
      Bukkit.getLogger().warning("[Strife] Failed to create model animation! No model!" + getId());
    } else {
      ArmorStand stand = loc.getWorld().spawn(loc, ArmorStand.class, e -> {
        e.setInvisible(true);
        e.setInvulnerable(true);
        e.setCollidable(false);
        e.setAI(false);
        e.setGravity(false);
        e.setSilent(true);
        e.setMarker(true);
        e.setCanTick(false);
        e.setVelocity(loc.getDirection());
        e.getLocation().setDirection(direction);
        e.getEyeLocation().setDirection(direction);
        ChunkUtil.setDespawnOnUnload(e);
      });
      stand.getEyeLocation().setDirection(loc.getDirection());
      proj.addPassenger(stand);
      ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(stand);
      if (modeledEntity == null) {
        stand.remove();
        Bukkit.getLogger().warning("Failed to create modelled entity");
      } else {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);
        ArrayList<Integer> list = new ArrayList<>();
        list.add(proj.getEntityId());
        packet.getIntLists().write(0, list);
        ProtocolLibrary.getProtocolManager().broadcastServerPacket(packet);
        modeledEntity.getBase().getBodyRotationController().setYBodyRot(stand.getEyeLocation().getYaw());
        modeledEntity.getBase().getBodyRotationController().setYHeadRot(stand.getEyeLocation().getYaw());
        modeledEntity.getBase().getBodyRotationController().setXHeadRot(stand.getEyeLocation().getPitch());
        Bukkit.getScheduler().runTaskTimer(getPlugin(), (task) -> {
          if (!proj.isValid()) {
            stand.remove();
            task.cancel();
          } else {
            stand.getLocation().setDirection(proj.getVelocity());
            stand.getEyeLocation().setDirection(proj.getVelocity());
            modeledEntity.getBase().getBodyRotationController().setYBodyRot(stand.getEyeLocation().getYaw());
            modeledEntity.getBase().getBodyRotationController().setYHeadRot(stand.getEyeLocation().getYaw());
            modeledEntity.getBase().getBodyRotationController().setXHeadRot(stand.getEyeLocation().getPitch());
          }
        }, 0L, 1L);
        modeledEntity.addModel(model, false);
        modeledEntity.setModelRotationLocked(false);
        modeledEntity.setBaseEntityVisible(false);

        if (throwItem) {
          ItemStack stack = caster.getEntity().getEquipment().getItemInMainHand();
          if (stack.getType() == Material.AIR) {
            stack = caster.getEntity().getEquipment().getItemInOffHand();
            if (stack.getType() == Material.AIR) {
              stack = new ItemStack(Material.IRON_SWORD);
            }
          }
          var provider = new HeldItem.StaticItemStackSupplier(stack);
          model.getBones().forEach((s, modelBone) ->
              modelBone.getBoneBehavior(BoneBehaviorTypes.ITEM).ifPresent(heldItem ->
                  heldItem.setItemProvider(provider)
              )
          );
        }
      }
    }
  }

  private Vector getCastDirection(LivingEntity caster, LivingEntity target) {
    Vector direction;
    if (targeted) {
      direction = target.getLocation().toVector().subtract(caster.getLocation().toVector()).normalize();
    } else {
      direction = caster.getEyeLocation().getDirection();
    }
    return direction;
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
    direction.rotateAroundY(angle);
  }
}