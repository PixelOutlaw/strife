package land.face.strife.data.pojo;

import com.tealcube.minecraft.bukkit.facecore.utilities.ChunkUtil;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.TargetResponse;
import land.face.strife.data.effects.OrderEntityEffect;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class OrderEntity {

  private final StrifePlugin plugin;
  private final OrderEntityEffect parentEffect;
  private final StrifeMob owner;
  private final ArmorStand stando;
  private final ActiveModel model;
  private final ModeledEntity modeledEntity;

  private Location destination;
  private Vector velocity;
  private int moveTicks;
  private int currentIdleTicks = 0;

  private BukkitTask idleTask;
  private BukkitTask movementTask;

  private static final int IDLE_TICKS = (20/10) * 15;

  public OrderEntity(StrifePlugin plugin, OrderEntityEffect parentEffect, StrifeMob owner, Location location) {
    this.plugin = plugin;
    this.parentEffect = parentEffect;
    this.owner = owner;
    this.stando = location.getWorld().spawn(location, ArmorStand.class, e -> {
      e.setInvisible(true);
      e.setInvulnerable(true);
      e.setCollidable(false);
      e.setAI(false);
      e.setGravity(false);
      e.setSilent(true);
      e.setMarker(true);
      e.setCanTick(false);
      ChunkUtil.setDespawnOnUnload(e);
    });
    this.model = ModelEngineAPI.createActiveModel(parentEffect.getModel());
    if (model == null) {
      Bukkit.getLogger().warning("[Strife] Failed to create model for OrderEntity! " + parentEffect.getId());
    }
    this.modeledEntity = ModelEngineAPI.createModeledEntity(stando);
    if (modeledEntity == null) {
      Bukkit.getLogger().warning("Failed to create modelled entity");
    }
    modeledEntity.addModel(model, true);
    modeledEntity.setBaseEntityVisible(false);
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      runIdleEffects();
      idleTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tickIdle, 0L, 10L);
    }, 31L);
    TargetResponse response = new TargetResponse(stando.getLocation());
    plugin.getEffectManager().executeEffectList(owner, response, parentEffect.getRiseEffects());
    plugin.getEffectManager().executeEffectList(owner, response, parentEffect.getStopEffects());
    model.getAnimationHandler().playAnimation("drop", 0, 0, 1, true);
    Bukkit.getScheduler().runTaskLater(plugin, this::runImpactEffects, 30L);
  }

  public void command(Location location) {
    if (!isIdle()) {
      return;
    }
    currentIdleTicks = 0;
    idleTask.cancel();
    destination = location;
    Vector current = stando.getLocation().toVector();
    Vector newVelocity = destination.toVector().subtract(current);
    double distance = newVelocity.length();
    moveTicks = (int) Math.min(
        parentEffect.getTravelTime(),
        distance / parentEffect.getMinimumSpeed()
    );
    newVelocity.multiply(1D / moveTicks);
    velocity = newVelocity.clone();
    stando.getLocation().setDirection(newVelocity.clone().normalize());
    movementTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tickMovement, 20L, 1L);
    model.getAnimationHandler().stopAnimation("drop");
    model.getAnimationHandler().playAnimation("rise", 0, 0, 1, true);
    TargetResponse response = new TargetResponse(stando.getLocation());
    plugin.getEffectManager().executeEffectList(owner, response, parentEffect.getRiseEffects());
  }

  private void tickMovement() {
    if (moveTicks == 0) {
      TargetResponse response = new TargetResponse(stando.getLocation());
      plugin.getEffectManager().executeEffectList(owner, response, parentEffect.getStopEffects());
      model.getAnimationHandler().stopAnimation("rise");
      model.getAnimationHandler().playAnimation("drop", 0, 0, 1, true);
      movementTask.cancel();
      Bukkit.getScheduler().runTaskLater(plugin, this::runImpactEffects, 30L);
      idleTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tickIdle, 31L, 10L);
    } else {
      Location newLoc = stando.getLocation().clone();
      newLoc.add(velocity);
      newLoc.setDirection(velocity);
      stando.teleport(newLoc, TeleportCause.PLUGIN);
      TargetResponse response = new TargetResponse(stando.getLocation());
      plugin.getEffectManager().executeEffectList(owner, response, parentEffect.getMoveEffects());
      moveTicks--;
    }
  }

  private void tickIdle() {
    currentIdleTicks++;
    if (stando.getWorld() != owner.getEntity().getWorld() ||
        stando.getLocation().distanceSquared(owner.getEntity().getLocation()) > 900) {
      destroy();
      return;
    }
    if (currentIdleTicks >= IDLE_TICKS) {
      destroy();
      return;
    }
    runIdleEffects();
  }

  private void runImpactEffects() {
    model.getAnimationHandler().stopAnimation("drop");
    TargetResponse response = new TargetResponse(stando.getLocation());
    plugin.getEffectManager().executeEffectList(owner, response, parentEffect.getImpactEffects());
  }

  private void runIdleEffects() {
    TargetResponse response = new TargetResponse(stando.getLocation());
    plugin.getEffectManager().executeEffectList(owner, response, parentEffect.getIdleEffects());
  }

  public boolean isIdle() {
    return idleTask != null && !idleTask.isCancelled();
  }

  public void destroy() {
    parentEffect.getActiveEntities().remove(owner.getEntity().getUniqueId());
    if (stando != null && stando.isValid()) {
      stando.remove();
    }
    if (idleTask != null && !idleTask.isCancelled()) {
      idleTask.cancel();
    }
    if (movementTask != null && !movementTask.isCancelled()) {
      movementTask.cancel();
    }
  }

}
