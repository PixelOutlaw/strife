package info.faceland.strife.effects;

import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.util.TargetingUtil;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ShulkerBullet;

public class ModifyProjectile extends Effect {

  private boolean remove;
  private double speedMult;
  private double range;
  private boolean friendlyProjectiles;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    Collection<Entity> entities = target.getEntity().getWorld()
        .getNearbyEntities(target.getEntity().getLocation(), range, range, range)
        .stream().filter(this::isValidProjectile)
        .collect(Collectors.toList());

    if (entities.isEmpty()) {
      return;
    }

    Set<LivingEntity> projectileSources = new HashSet<>();
    for (Entity entity : entities) {
      projectileSources.add((LivingEntity) ((Projectile) entity).getShooter());
    }

    TargetingUtil.filterFriendlyEntities(projectileSources, caster, friendlyProjectiles);

    for (Entity entity : entities) {
      Projectile projectile = (Projectile) entity;
      if (!projectileSources.contains(projectile.getShooter())) {
        continue;
      }
      if (remove || entity instanceof Fireball) {
        projectile.getLocation().getWorld().spawnParticle(
            Particle.CRIT, projectile.getLocation(), 10, 0.2, 0.2, 0.2);
        projectile.remove();
      } else {
        if (entity instanceof ShulkerBullet) {
          ((ShulkerBullet) entity).setTarget(null);
        }
        projectile.setVelocity(projectile.getVelocity().clone().multiply(speedMult));
      }
    }
  }

  private boolean isValidProjectile(Entity e) {
    return e instanceof Projectile && ((Projectile) e).getShooter() instanceof LivingEntity;
  }

  public void setRemove(boolean remove) {
    this.remove = remove;
  }

  public void setSpeedMult(double speedMult) {
    this.speedMult = speedMult;
  }

  public void setRange(double range) {
    this.range = range;
  }

  public void setFriendlyProjectiles(boolean friendlyProjectiles) {
    this.friendlyProjectiles = friendlyProjectiles;
  }

}
