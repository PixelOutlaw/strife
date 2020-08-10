package land.face.strife.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import land.face.strife.StrifePlugin;
import land.face.strife.data.ChaserEntity;
import land.face.strife.data.LoadedChaser;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.TargetResponse;
import land.face.strife.data.effects.Effect;
import land.face.strife.data.effects.StrifeParticle;
import land.face.strife.util.DamageUtil.OriginLocation;
import land.face.strife.util.TargetingUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

public class ChaserManager {

  private StrifePlugin plugin;
  private HashMap<String, LoadedChaser> chaserData = new HashMap<>();
  private Set<ChaserEntity> chasers;

  public ChaserManager(StrifePlugin plugin) {
    this.plugin = plugin;
    this.chasers = new HashSet<>();
  }

  public void createChaser(StrifeMob caster, String id, Vector velocity, Location spawnLocation,
      LivingEntity target) {
    LoadedChaser data = chaserData.get(id);
    ChaserEntity chaser = new ChaserEntity(caster, id, spawnLocation, target, data.getSpeed(),
        velocity, data.getLifespan());
    chasers.add(chaser);
  }

  public void tickChasers() {
    Iterator<ChaserEntity> iterator = chasers.iterator();
    while (iterator.hasNext()) {
      ChaserEntity chaser = iterator.next();
      if (chaser.getCurrentTick() > chaser.getLifespan()) {
        iterator.remove();
        continue;
      }
      chaser.setCurrentTick(chaser.getCurrentTick() + 1);

      if (chaser.getTarget() == null || !chaser.getTarget().isValid() ||
          !chaser.getLocation().getWorld().equals(chaser.getTarget().getWorld())) {
        iterator.remove();
        continue;
      }

      LoadedChaser data = chaserData.get(chaser.getChaserId());
      if (data.isRemoveAtSolids() && chaser.getLocation().getBlock().getType().isSolid()) {
        iterator.remove();
        continue;
      }
      boolean hitTarget = executeChaserMovement(chaser, data);
      if (hitTarget) {
        iterator.remove();
      }
    }
  }

  private boolean executeChaserMovement(ChaserEntity chaser, LoadedChaser data) {
    Location targetLocation = TargetingUtil
        .getOriginLocation(chaser.getTarget(), OriginLocation.CENTER);
    Vector change = targetLocation.toVector()
        .subtract(chaser.getLocation().toVector()).normalize().multiply(data.getSpeed());
    Vector velocity = chaser.getVelocity();
    velocity.add(change);
    if (velocity.length() > data.getMaxSpeed()) {
      velocity.normalize().multiply(data.getMaxSpeed());
    }
    chaser.getLocation().add(velocity);
    for (StrifeParticle particle : data.getParticles()) {
      particle.applyAtLocation(chaser.getCaster(),
          chaser.getLocation().clone().subtract(chaser.getVelocity().clone().multiply(0.5)));
    }
    if (isChaserCloseEnough(chaser, data, targetLocation)) {
      Set<LivingEntity> entities = new HashSet<>();
      entities.add(chaser.getTarget());
      TargetResponse response = new TargetResponse(entities);
      plugin.getEffectManager().processEffectList(chaser.getCaster(), response, data.getEffectList());
      return true;
    }
    chaser.setVelocity(velocity);
    return false;
  }

  private boolean isChaserCloseEnough(ChaserEntity chaser, LoadedChaser data, Location targetLoc) {
    float hitRange = data.getMaxSpeed() + (float) chaser.getTarget().getWidth() / 2;
    return Math.abs(chaser.getLocation().getX() - targetLoc.getX()) < hitRange &&
        Math.abs(chaser.getLocation().getY() - targetLoc.getY()) < hitRange &&
        Math.abs(chaser.getLocation().getZ() - targetLoc.getZ()) < hitRange;
  }

  public LoadedChaser loadChaser(String id, ConfigurationSection cs) {
    LoadedChaser data = new LoadedChaser();
    data.setLifespan(cs.getInt("lifespan", 200));
    data.setMaxSpeed((float) cs.getDouble("max-speed", 1));
    data.setStartSpeed((float) cs.getDouble("start-speed", 1));
    data.setSpeed((float) cs.getDouble("speed", 0.1));
    data.setRemoveAtSolids(cs.getBoolean("remove-on-block-contact", true));
    data.setStrictDuration(cs.getBoolean("strict-duration", false));

    List<String> effects = cs.getStringList("effects");
    List<String> particles = cs.getStringList("particles");

    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> {
      for (String s : effects) {
        data.getEffectList().add(getEffect(s));
      }
      for (String s : particles) {
        data.getParticles().add((StrifeParticle) getEffect(s));
      }
    }, 5L);
    chaserData.put(id, data);
    return data;
  }

  public LoadedChaser loadChaser(String id, LoadedChaser data) {
    chaserData.put(id, data);
    return data;
  }

  private Effect getEffect(String s) {
    return plugin.getEffectManager().getEffect(s);
  }
}
