package land.face.strife.data;

import com.tealcube.minecraft.bukkit.facecore.utilities.ChunkUtil;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang.StringUtils;
import com.ticxo.modelengine.api.model.ActiveModel;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import land.face.strife.StrifePlugin;
import land.face.strife.data.effects.StrifeParticle;
import land.face.strife.util.ProjectileUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.AbstractArrow.PickupStatus;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

@Getter
@Setter
@NoArgsConstructor
public class ChaserEntity {

  private StrifeMob caster;

  private LoadedChaser template;

  private int lifespan;
  private int guideTicks;
  private float speed;
  private float maxSpeed;

  private Vector velocity;
  private Location location;
  private int currentTick;
  private boolean guided;

  private Arrow hitBox;
  private WeakReference<LivingEntity> target;
  private Location targetLocation;
  private ActiveModel model;

  private BukkitTask task;

  public final static List<ChaserEntity> RUNNING_CHASERS = new ArrayList<>();

  public static ChaserEntity create(LoadedChaser template, Location location, Vector direction,
      StrifeMob caster, LivingEntity target, Location targetLocation, float durationMult, float speedMult) {

    ChaserEntity ce = new ChaserEntity();
    ce.template = template;
    if (target != null) {
      ce.target = new WeakReference<>(target);
    } else if (targetLocation != null) {
      ce.targetLocation = targetLocation;
    } else {
      return null;
    }
    ce.lifespan = (int) (template.getLifespan() * durationMult);
    ce.guideTicks = template.getGuideStopTicks() < 0 ?
        Integer.MAX_VALUE : (int) (template.getGuideStopTicks() * durationMult);
    ce.caster = caster;
    ce.location = location.clone();

    ce.speed = template.getSpeed();
    ce.maxSpeed = template.getMaxSpeed();

    ce.speed *= speedMult;
    ce.maxSpeed *= speedMult;

    ce.guided = ce.guideTicks > 0;

    float horizontalFling = template.getStartHorizontalMin() +
        ((float) Math.random() * (template.getStartHorizontalMax() - template.getStartHorizontalMin()));
    float verticalFling = template.getStartVerticalMin() +
        ((float) Math.random() * (template.getStartVerticalMax() - template.getStartVerticalMin()));

    Vector startVelocity = direction.clone().multiply(template.getStartSpeed()).multiply(speedMult);
    startVelocity.setY(startVelocity.getY() + verticalFling);
    if (Math.random() > 0.5) {
      startVelocity.rotateAroundY(horizontalFling);
    } else {
      startVelocity.rotateAroundY(-horizontalFling);
    }
    ce.velocity = startVelocity;
    ce.velocity.multiply(speedMult);

    if (template.isPhysical()) {
      Arrow arrow = ce.location.getWorld().spawn(ce.location, Arrow.class, e -> {
        e.setInvisible(true);
        e.setGravity(false);
        e.setSilent(true);
        e.getLocation().setDirection(ce.velocity);
        e.setVelocity(ce.velocity);
        e.setPickupStatus(PickupStatus.CREATIVE_ONLY);
        ChunkUtil.setDespawnOnUnload(e);
      });
      arrow.setShooter(caster.getEntity());
      arrow.setPickupStatus(PickupStatus.CREATIVE_ONLY);

      ProjectileUtil.setShotId(arrow);
      if (!template.getEffectList().isEmpty()) {
        ProjectileUtil.setHitEffects(arrow, template.getEffectList());
      }

      for (Player p : Bukkit.getOnlinePlayers()) {
        p.hideEntity(StrifePlugin.getInstance(), arrow);
      }
      ce.hitBox = arrow;
      if (StringUtils.isNotBlank(template.getModelId())) {
        var c = template.getModelColors().isEmpty() ? null :
            template.getModelColors().get(StrifePlugin.RNG.nextInt(template.getModelColors().size()));
        var s = template.getModelSizes().isEmpty() ? 1 :
            template.getModelSizes().get(StrifePlugin.RNG.nextInt(template.getModelSizes().size()));
        ProjectileUtil.setupModelledProjectile(template.getModelId(), caster, arrow, ce.location, ce.velocity, c, s,
            false);
      }
      if (template.isBlockContactEffects()) {
        ProjectileUtil.setContactTrigger(arrow);
      }
    }

    ce.task = Bukkit.getScheduler().runTaskTimer(StrifePlugin.getInstance(), ce::tick, 0L, 1L);

    RUNNING_CHASERS.add(ce);
    return ce;
  }

  private void tick() {
    if (currentTick > lifespan) {
      if (template.isExpiryEffects()) {
        runEffects(null, location);
      }
      delete();
      return;
    }

    currentTick++;

    if (guided) {
      if (currentTick > guideTicks) {
        if (targetLocation != null) {
          guided = false;
          //System.out.println("can1");
        } else if (hasValidTarget()) {
          guided = false;
          //System.out.println("can2");
        }
      }
    }

    move();

    if (template.isPhysical() && !hitBox.isValid()) {
      delete();
      return;
    }

    if (template.isDestroyOnBlockContact() && location.getBlock().getType().isSolid()) {
      if (template.isBlockContactEffects()) {
        runEffects(null, location.clone().add(velocity.clone().multiply(-1)));
      }
      delete();
      return;
    }

    if (targetLocation != null) {
      double range = 1 + Math.pow(speed, 2);
      if (location.distanceSquared(targetLocation) < range) {
        runEffects(null, targetLocation);
        delete();
      }
      return;
    }
    if (guided && target.get() != null) {
      if (location.getWorld() != target.get().getLocation().getWorld()) {
        delete();
        return;
      }
      Location targetCoords = target.get().getLocation().clone();
      targetCoords.setY(targetCoords.getY() + target.get().getHeight() / 2 + 0.07);
      double range = 1 + Math.pow(speed + Math.max(target.get().getHeight(), target.get().getWidth()), 2);
      if (location.distanceSquared(targetCoords) < range) {
        runEffects(target.get(), null);
        delete();
      }
    }
  }

  private void move() {
    if (guided) {
      if (targetLocation != null) {
        Vector change = targetLocation.toVector().subtract(location.toVector()).normalize().multiply(speed);
        velocity.add(change);
        //System.out.println("1");
      } else if (hasValidTarget()) {
        Vector targetCoords = target.get().getLocation().toVector();
        targetCoords.setY(targetCoords.getY() + target.get().getHeight() / 2 + 0.07);
        Vector change = targetCoords.subtract(location.toVector()).normalize().multiply(speed);
        velocity.add(change);
        //System.out.println("2");
      }
    } else {
      velocity.add(velocity.clone().normalize().multiply(speed));
      //System.out.println("3");
    }
    velocity.setY(velocity.getY() - template.getGravity());
    if (velocity.length() > maxSpeed) {
      velocity.normalize().multiply(maxSpeed);
    }
    location.add(velocity);
    for (StrifeParticle particle : template.getParticles()) {
      particle.applyAtLocation(caster, location.clone().subtract(velocity.clone().multiply(0.5)));
    }
    if (hitBox != null) {
      hitBox.teleport(location);
      hitBox.setVelocity(velocity);
    }
  }

  private void runEffects(LivingEntity target, Location location) {
    if (target != null) {
      Set<LivingEntity> entities = new HashSet<>();
      entities.add(target);
      TargetResponse response = new TargetResponse(entities);
      StrifePlugin.getInstance().getEffectManager().processEffectList(caster, response, template.getEffectList());
    } else if (location != null) {
      TargetResponse response = new TargetResponse(location);
      StrifePlugin.getInstance().getEffectManager().processEffectList(caster, response, template.getEffectList());
    }
  }

  private boolean hasValidTarget() {
    return target != null && target.get() != null && target.get().isValid() &&
        target.get().getWorld() == location.getWorld();
  }

  private void delete() {
    RUNNING_CHASERS.remove(this);
    if (task != null && !task.isCancelled()) {
      task.cancel();
    }
    if (hitBox != null && hitBox.isValid()) {
      hitBox.remove();
    }
  }
}
