package land.face.strife.data.effects;

import com.tealcube.minecraft.bukkit.facecore.utilities.MoveUtil;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import land.face.strife.data.StrifeMob;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

public class Push extends Effect {

  @Setter
  private PushType pushType;
  @Setter
  private double power;
  @Setter
  private double height;
  @Setter
  private boolean zeroFall;
  @Setter
  private boolean horizontalClamp;
  @Setter
  private boolean verticalClamp;
  @Setter
  private Location tempOrigin;
  @Setter
  private Vector tempDirection;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (target.getEntity().getType() == EntityType.SHULKER || !target.getEntity().hasGravity()) {
      return;
    }
    if (!isFriendly() && StringUtils.isNotBlank(target.getUniqueEntityId())) {
      if (getPlugin().getUniqueEntityManager().getUnique(target.getUniqueEntityId()).isPushImmune()) {
        return;
      }
    }
    Vector direction;
    switch (pushType) {
      case AWAY_FROM_CASTER ->
          direction = getLocationDiff(caster.getEntity().getLocation().toVector(), target.getEntity());
      case TARGET_DIRECTION ->
          direction = target.getEntity().getEyeLocation().getDirection();
      case AWAY_FROM_CENTER ->
          direction = getLocationDiff(tempOrigin.toVector(), target.getEntity());
      case CASTER_DIRECTION ->
          direction = caster.getEntity().getEyeLocation().getDirection();
      case TEMP_DIRECTION ->
          direction = getLocationDiff(tempDirection, target.getEntity());
      default -> { return; }
    }

    Vector currentVelocity = MoveUtil.getVelocity(target.getEntity());

    Vector horizontalPush = direction.clone();
    horizontalPush.setY(0.0001);
    horizontalPush.normalize().multiply(power / 10);

    if (zeroFall) {
      if (currentVelocity.getY() < 0) {
        currentVelocity.setY(0.0001);
        currentVelocity.normalize();
      }
      target.getEntity().setFallDistance(0);
    }

    Vector newVelocity = currentVelocity.clone();

    if (verticalClamp) {
      newVelocity.setY(clampRay(currentVelocity.getY(), height / 10));
    } else {
      newVelocity.setY(newVelocity.getY() + height / 10);
    }

    if (horizontalClamp) {
      newVelocity.setX(clampRay(currentVelocity.getX(), horizontalPush.getX()));
      newVelocity.setZ(clampRay(currentVelocity.getZ(), horizontalPush.getZ()));
    } else {
      newVelocity.add(horizontalPush);
    }

    target.getEntity().setVelocity(newVelocity);
  }

  private Vector getLocationDiff(Vector originLocation, Entity to) {
    if (originLocation.equals(to.getLocation().toVector())) {
      return new Vector(0, 1, 0);
    }
    return to.getLocation().toVector().subtract(originLocation).normalize();
  }

  private double clampRay(double old, double change) {
    if (change == 0) {
      return old;
    }
    if (change > 0) {
      return Math.min(old + change, change);
    } else {
      return Math.max(old + change, change);
    }
  }

  public enum PushType {
    AWAY_FROM_CASTER,
    TARGET_DIRECTION,
    AWAY_FROM_CENTER,
    CASTER_DIRECTION,
    TEMP_DIRECTION
  }
}