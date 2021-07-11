package land.face.strife.data.effects;

import java.util.Random;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.tasks.ParticleTask;
import land.face.strife.util.TargetingUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class StrifeParticle extends LocationEffect {

  private ParticleStyle style = ParticleStyle.NORMAL;
  private Particle particle = Particle.FLAME;
  private int quantity = 1;
  private float spread = 0;
  private float speed = 0;
  private float size = 1;

  private double red;
  private double green;
  private double blue;

  private double arcAngle;
  private double arcOffset;

  private float angleRotation;

  private float orbitSpeed;
  private float radius;
  private float endRadius;

  private boolean lineVertical;
  private float lineOffset;
  private float lineIncrement;

  private int tickDuration;
  private boolean strictDuration;

  private ItemStack itemData = null;
  private BlockData blockData = null;

  private static final Random random = new Random();

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (tickDuration > 0) {
      if (isFriendly() == TargetingUtil.isFriendly(caster, target)) {
        double duration = tickDuration;
        if (!strictDuration) {
          duration *= 1 + caster.getStat(StrifeStat.EFFECT_DURATION) / 100;
        }
        StrifePlugin.getInstance().getParticleTask()
            .addContinuousParticle(target.getEntity(), this, (int) duration);
      }
    } else {
      playAtLocation(caster, getLoc(target.getEntity()),
          target.getEntity().getEyeLocation().getDirection());
    }
  }

  @Override
  public void applyAtLocation(StrifeMob caster, Location location) {
    playAtLocation(caster, location, location.getDirection());
  }

  private void playAtLocation(StrifeMob attacker, Location location, Vector direction) {
    Particle particleInUse = particle;
    if (attacker != null && attacker.hasTrait(StrifeTrait.SOUL_FLAME) && particleInUse == Particle.FLAME) {
      particleInUse = Particle.SOUL_FIRE_FLAME;
    }
    switch (style) {
      case CIRCLE:
        spawnParticleCircle(particleInUse, location, size);
        return;
      case ORBIT:
        spawnParticleOrbit(particleInUse, location, size, orbitSpeed);
        return;
      case LINE:
        if (direction == null) {
          throw new IllegalArgumentException("Cannot use LINE particle without defined direction");
        }
        spawnParticleLine(particleInUse, location, direction, size + lineOffset);
        return;
      case ARC:
        spawnParticleArc(particleInUse, direction, location, size, arcAngle, arcOffset);
        return;
      case CLAW:
        spawnParticleClaw(particleInUse, direction, location, size, arcAngle, arcOffset);
        return;
      case NORMAL:
      default:
        spawnParticle(particleInUse, location);
    }
  }

  public void setTickDuration(int tickDuration) {
    this.tickDuration = tickDuration;
  }

  public void setParticle(Particle particle) {
    this.particle = particle;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  public float getSpeed() {
    return speed;
  }

  public void setSpeed(float speed) {
    this.speed = speed;
  }

  public void setSpread(float spread) {
    this.spread = spread;
  }

  public void setStyle(ParticleStyle style) {
    this.style = style;
  }

  public double getSize() {
    return size;
  }

  public void setSize(float size) {
    this.size = size;
  }

  public Particle getParticle() {
    return particle;
  }

  public void setRed(double red) {
    this.red = red;
  }

  public void setGreen(double green) {
    this.green = green;
  }

  public void setBlue(double blue) {
    this.blue = blue;
  }

  public void setArcAngle(double arcAngle) {
    this.arcAngle = arcAngle;
  }

  public void setArcOffset(double arcOffset) {
    this.arcOffset = arcOffset;
  }

  public void setAngleRotation(float angleRotation) {
    this.angleRotation = angleRotation;
  }

  public void setOrbitSpeed(float orbitSpeed) {
    this.orbitSpeed = orbitSpeed;
  }

  public void setRadius(float radius) {
    this.radius = radius;
  }

  public void setEndRadius(float endRadius) {
    this.endRadius = endRadius;
  }

  public void setLineOffset(float lineOffset) {
    this.lineOffset = lineOffset;
  }

  public void setLineVertical(boolean lineVertical) {
    this.lineVertical = lineVertical;
  }

  public void setLineIncrement(float lineIncrement) {
    this.lineIncrement = lineIncrement;
  }

  public void setStrictDuration(boolean strictDuration) {
    this.strictDuration = strictDuration;
  }

  public Location getLoc(LivingEntity le) {
    return TargetingUtil.getOriginLocation(le, getOrigin());
  }

  public void setItemData(ItemStack itemData) {
    this.itemData = itemData;
    if (itemData.getType().isBlock()) {
      blockData = Bukkit.getServer().createBlockData(itemData.getType());
    }
  }

  private void spawnParticleArc(Particle particle, Vector direction, Location center, double radius, double angle,
      double offset) {
    int segments = (int) (6 * (angle / 90) * (1 + radius / 3));
    double startAngle = -angle * 0.5;
    double segmentAngle = angle / segments;
    double verticalDirection = random.nextDouble() < 0.5 ? 1 : -1;
    double startVerticalOffset = verticalDirection * offset * random.nextDouble() * 0.5;
    double segmentOffset = (2 * startVerticalOffset) / segments;
    for (int i = 0; i <= segments; i++) {
      Vector newDirection = direction.clone();
      newDirection.setX(newDirection.getX() + 0.001);
      newDirection.setY(0.001);
      newDirection.setZ(newDirection.getZ() + 0.001);
      newDirection.normalize().multiply(size);
      double radialAngle = Math.toRadians(startAngle + i * segmentAngle);
      applyRadialAngles(newDirection, radialAngle);
      newDirection.setY(startVerticalOffset - segmentOffset * i);
      spawnParticle(particle, center.clone().add(newDirection));
    }
  }

  private void spawnParticleClaw(Particle particle, Vector direction, Location center,
      double radius, double angle, double offset) {
    int segments = (int) (6 * (angle / 90) * (1 + radius / 3));
    double startAngle = -angle * 0.5;
    double segmentAngle = angle / segments;
    double verticalDirection = random.nextDouble() < 0.5 ? 1 : -1;
    double startVerticalOffset = verticalDirection * offset * random.nextDouble() * 0.5;
    double segmentOffset = (2 * startVerticalOffset) / segments;
    double segmentChunk = ((double) segments) * 0.25D;
    for (int i = 0; i <= segments; i++) {
      Vector newDirection = direction.clone();
      newDirection.setX(newDirection.getX() + 0.001);
      newDirection.setY(0.001);
      newDirection.setZ(newDirection.getZ() + 0.001);
      newDirection.normalize().multiply(size);
      double radialAngle = Math.toRadians(startAngle + i * segmentAngle);
      applyRadialAngles(newDirection, radialAngle);
      newDirection.setY(startVerticalOffset - segmentOffset * i);
      Location newLoc = center.clone().add(newDirection);
      spawnParticle(particle, newLoc);
      if (i > segmentChunk && i < segments - segmentChunk) {
        spawnParticle(particle, newLoc.add(0, 0.4, 0));
        spawnParticle(particle, newLoc.add(0, -0.8, 0));
      }
    }
  }

  private void applyRadialAngles(Vector direction, double angle) {
    double x = direction.getX();
    double z = direction.getZ();
    direction.setZ(z * Math.cos(angle) - x * Math.sin(angle));
    direction.setX(z * Math.sin(angle) + x * Math.cos(angle));
  }

  private void spawnParticleCircle(Particle particle, Location center, double radius) {
    for (double degree = 0; degree < 360; degree += 30 / radius) {
      double radian1 = Math.toRadians(degree);
      Location loc = center.clone();
      loc.add(Math.cos(radian1) * radius, 0, Math.sin(radian1) * radius);
      spawnParticle(particle, loc);
    }
  }

  private void spawnParticleOrbit(Particle particle, Location center, float radius, float orbitSpeed) {
    spawnParticleOrbit(particle, center, radius, 0, orbitSpeed);
  }

  private void spawnParticleOrbit(Particle particle, Location center, float radius, float offset, float orbitSpeed) {
    float step = 360f / quantity;
    for (int i = 0; i <= quantity; i++) {
      double start = orbitSpeed * ParticleTask.getCurrentTick() + offset;
      double radian1 = Math.toRadians(start + step * i);
      Location loc = center.clone();
      loc.add(Math.cos(radian1) * radius, 0, Math.sin(radian1) * radius);
      spawnParticle(particle, loc);
    }
  }

  private void spawnParticleLine(Particle particle, Location center, Vector direction, float length) {
    for (float dist = lineOffset; dist < length; dist += lineIncrement) {
      Location loc = center.clone();
      if (lineVertical) {
        loc.add(new Vector(0, dist, 0));
      } else {
        loc.add(direction.clone().multiply(dist));
      }

      if (loc.getBlock().getType() != Material.AIR) {
        if (loc.getBlock().getType().isSolid()) {
          return;
        }
      }

      if (radius <= 0) {
        spawnParticle(particle, loc);
      } else {
        float percent = dist / length;
        float currentRadius = radius + ((endRadius - radius) * percent);
        spawnParticleOrbit(particle, loc, currentRadius, percent * angleRotation, orbitSpeed);
      }
    }
  }

  private void spawnParticle(Particle particle, Location location) {
    if (particle == Particle.SPELL_MOB || particle == Particle.SPELL_WITCH
        || particle == Particle.SPELL_INSTANT) {
      for (int i = 0; i < quantity; i++) {
        Location newLoc = location.clone().add(-spread + random.nextDouble() * spread * 2, -spread
            + random.nextDouble() * spread * 2, -spread + random.nextDouble() * spread * 2);
        location.getWorld().spawnParticle(Particle.SPELL_MOB, newLoc, 0, red, green, blue, 1);
      }
    } else if (itemData != null) {
      if (particle == Particle.FALLING_DUST) {
        location.getWorld().spawnParticle(particle, location, quantity, spread, spread, spread, speed, blockData);
      } else  {
        location.getWorld().spawnParticle(particle, location, quantity, spread, spread, spread, speed, itemData);
      }
    } else {
      location.getWorld().spawnParticle(particle, location, quantity, spread, spread, spread, speed);
    }
  }

  public int getTickDuration() {
    return tickDuration;
  }

  public enum ParticleStyle {
    NORMAL,
    CIRCLE,
    ORBIT,
    LINE,
    ARC,
    CLAW
  }
}
