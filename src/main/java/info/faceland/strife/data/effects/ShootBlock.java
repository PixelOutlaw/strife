package info.faceland.strife.data.effects;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.timers.FallingBlockTimer;
import info.faceland.strife.util.DamageUtil.OriginLocation;
import info.faceland.strife.util.TargetingUtil;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.FallingBlock;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

public class ShootBlock extends Effect {

  private BlockData blockData;
  private OriginLocation originType;
  private int quantity;
  private double speed;
  private double spread;
  private double verticalBonus;
  private boolean zeroPitch;
  private List<String> hitEffects;

  private final static Set<FallingBlockTimer> FALLING_BLOCKS = new HashSet<>();

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {

    Vector castDirection = target.getEntity().getEyeLocation().getDirection();
    Location originLocation = TargetingUtil.getOriginLocation(target.getEntity(), originType);
    if (zeroPitch) {
      castDirection.setY(0.001);
      castDirection.normalize();
    }
    castDirection.normalize().multiply(speed);

    for (int i = 0; i < quantity; i++) {
      FallingBlock block = originLocation.getWorld().spawnFallingBlock(originLocation, blockData);
      Vector newDirection = castDirection.clone();
      applySpread(newDirection, spread);
      block.setVelocity(newDirection);
      block.getLocation().setDirection(newDirection.normalize());
      block.setDropItem(false);
      block.setHurtEntities(false);

      if (!hitEffects.isEmpty()) {
        StringBuilder hitString = new StringBuilder();
        for (String s : hitEffects) {
          hitString.append(s).append("~");
        }
        block.setMetadata("EFFECT_PROJECTILE",
            new FixedMetadataValue(StrifePlugin.getInstance(), hitString.toString()));
      }
      FALLING_BLOCKS.add(new FallingBlockTimer(caster, block));
    }
  }

  public static void clearTimers() {
    for (FallingBlockTimer timer : FALLING_BLOCKS) {
      timer.cancel();
    }
    FALLING_BLOCKS.clear();
  }

  public void setBlockData(BlockData blockData) {
    this.blockData = blockData;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  public void setSpeed(double speed) {
    this.speed = speed;
  }

  public void setSpread(double spread) {
    this.spread = spread;
  }

  public void setVerticalBonus(double verticalBonus) {
    this.verticalBonus = verticalBonus;
  }

  public void setHitEffects(List<String> hitEffects) {
    this.hitEffects = hitEffects;
  }

  public void setZeroPitch(boolean zeroPitch) {
    this.zeroPitch = zeroPitch;
  }

  public void setOriginType(OriginLocation originType) {
    this.originType = originType;
  }

  private void applySpread(Vector direction, double spread) {
    direction.add(new Vector(
        spread - 2 * spread * Math.random(),
        spread - 2 * spread * Math.random() + verticalBonus,
        spread - 2 * spread * Math.random()));
  }
}