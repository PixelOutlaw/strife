package land.face.strife.data.effects;

import java.util.Random;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.tasks.ParticleTask;
import land.face.strife.util.DamageUtil.OriginLocation;
import land.face.strife.util.TargetingUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class StrifeParticle extends Effect {

  private Particle particle;
  private int quantity;
  private float spread;
  private float speed;
  private double size;
  private double red;
  private double green;
  private double blue;
  private double arcAngle;
  private double arcOffset;
  private double orbitSpeed;
  private double lineOffset;
  private int tickDuration;
  private boolean strictDuration;
  private ParticleStyle style;
  private OriginLocation particleOriginLocation = OriginLocation.CENTER;
  private ItemStack blockData;

  private static Random random = new Random();

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (tickDuration > 0) {
      double duration = tickDuration;
      if (!strictDuration) {
        duration *= 1 + caster.getStat(StrifeStat.EFFECT_DURATION) / 100;
      }
      StrifePlugin.getInstance().getParticleTask()
          .addContinuousParticle(target.getEntity(), this, (int) duration);
      return;
    }
    playAtLocation(getLoc(target.getEntity()), target.getEntity().getEyeLocation().getDirection());
  }

  public void playAtLocation(LivingEntity livingEntity) {
    playAtLocation(getLoc(livingEntity), livingEntity.getEyeLocation().getDirection());
  }

  public void playAtLocation(Location location) {
    playAtLocation(location, location.getDirection());
  }

  private void playAtLocation(Location location, Vector direction) {
    switch (style) {
      case CIRCLE:
        spawnParticleCircle(location, size);
        return;
      case ORBIT:
        spawnParticleOrbit(location, size);
        return;
      case LINE:
        if (direction == null) {
          throw new IllegalArgumentException("Cannot use LINE particle without defined direction");
        }
        spawnParticleLine(location, direction, size + lineOffset);
        return;
      case PILLAR:
        spawnParticlePillar(location, size);
        return;
      case ARC:
        spawnParticleArc(direction, location, size, arcAngle, arcOffset);
        return;
      case NORMAL:
      default:
        spawnParticle(location);
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

  public void setParticleOriginLocation(OriginLocation particleOriginLocation) {
    this.particleOriginLocation = particleOriginLocation;
  }

  public OriginLocation getOrigin() {
    return particleOriginLocation;
  }

  public void setStyle(ParticleStyle style) {
    this.style = style;
  }

  public double getSize() {
    return size;
  }

  public void setSize(double size) {
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

  public void setOrbitSpeed(double orbitSpeed) {
    this.orbitSpeed = orbitSpeed;
  }

  public double getLineOffset() {
    return lineOffset;
  }

  public void setLineOffset(double lineOffset) {
    this.lineOffset = lineOffset;
  }

  public void setStrictDuration(boolean strictDuration) {
    this.strictDuration = strictDuration;
  }

  public Location getLoc(LivingEntity le) {
    return TargetingUtil.getOriginLocation(le, particleOriginLocation);
  }

  public void setBlockData(ItemStack blockData) {
    this.blockData = blockData;
  }

  private void spawnParticleArc(Vector direction, Location center, double radius, double angle,
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
      spawnParticle(center.clone().add(newDirection));
    }
  }

  private void applyRadialAngles(Vector direction, double angle) {
    double x = direction.getX();
    double z = direction.getZ();
    direction.setZ(z * Math.cos(angle) - x * Math.sin(angle));
    direction.setX(z * Math.sin(angle) + x * Math.cos(angle));
  }

  private void spawnParticleCircle(Location center, double radius) {
    for (double degree = 0; degree < 360; degree += 30 / radius) {
      double radian1 = Math.toRadians(degree);
      Location loc = center.clone();
      loc.add(Math.cos(radian1) * radius, 0, Math.sin(radian1) * radius);
      spawnParticle(loc);
    }
  }

  private void spawnParticleOrbit(Location center, double radius) {
    double step = 360d / quantity;
    for (int i = 0; i <= quantity; i++) {
      double start = orbitSpeed * ParticleTask.getCurrentTick();
      double radian1 = Math.toRadians(start + step * i);
      Location loc = center.clone();
      loc.add(Math.cos(radian1) * radius, 0, Math.sin(radian1) * radius);
      spawnParticle(loc);
    }
  }

  private void spawnParticlePillar(Location center, double size) {
    for (double i = 0; i < size; i += 0.25) {
      Location loc = center.clone();
      loc.add(0, i, 0);
      loc.getWorld().spawnParticle(particle, loc, quantity, spread, spread, spread, speed);
    }
  }

  private void spawnParticleLine(Location center, Vector direction, double length) {
    for (double dist = lineOffset; dist < length ; dist += 0.25) {
      Location loc = center.clone();
      loc.add(direction.clone().multiply(dist));
      if (loc.getBlock().getType() != Material.AIR) {
        if (!loc.getBlock().getType().isTransparent()) {
          return;
        }
      }
      spawnParticle(loc);
    }
  }

  private void spawnParticle(Location location) {
    if (particle == Particle.SPELL_MOB || particle == Particle.SPELL_WITCH
        || particle == Particle.SPELL_INSTANT) {
      for (int i = 0; i < quantity; i++) {
        Location newLoc = location.clone().add(-spread + random.nextDouble() * spread * 2,
            -spread + random.nextDouble() * spread * 2, -spread + random.nextDouble() * spread * 2);
        location.getWorld().spawnParticle(Particle.SPELL_MOB, newLoc, 0, red, green, blue, 1);
      }
    } else if (blockData != null) {
      location.getWorld()
          .spawnParticle(particle, location, quantity, spread, spread, spread, speed, blockData);
    } else {
      location.getWorld().spawnParticle(particle, location, quantity, spread, spread, spread, speed);
    }
  }

  public enum ParticleStyle {
    NORMAL,
    CIRCLE,
    ORBIT,
    LINE,
    ARC,
    PILLAR
  }
}
