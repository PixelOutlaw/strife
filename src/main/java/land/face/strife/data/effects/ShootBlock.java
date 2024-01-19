package land.face.strife.data.effects;

import com.tealcube.minecraft.bukkit.facecore.utilities.ChunkUtil;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import java.util.List;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.timers.FallingBlockTimer;
import land.face.strife.util.SpecialStatusUtil;
import land.face.strife.util.TargetingUtil;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.FallingBlock;
import org.bukkit.util.Vector;

public class ShootBlock extends LocationEffect {

  private BlockData blockData;
  private int quantity;
  private double speed;
  private double spread;
  private double verticalBonus;
  private boolean zeroPitch;
  private List<String> hitEffects;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    Location originLocation = TargetingUtil.getOriginLocation(target.getEntity(), getOrigin());
    applyAtLocation(caster, originLocation);
  }

  @Override
  public void applyAtLocation(StrifeMob caster, Location location) {
    Vector castDirection = caster.getEntity().getEyeLocation().getDirection();
    if (zeroPitch) {
      castDirection.setY(0.001);
      castDirection.normalize();
    }
    castDirection.normalize().multiply(speed);

    for (int i = 0; i < quantity; i++) {
      FallingBlock block = location.getWorld().spawnFallingBlock(location, blockData);
      ChunkUtil.setDespawnOnUnload(block);
      Vector newDirection = castDirection.clone();
      applySpread(newDirection, spread);
      block.setVelocity(newDirection);
      block.getLocation().setDirection(newDirection.normalize());
      block.setDropItem(false);
      block.setHurtEntities(false);
      new FallingBlockTimer(caster, block);
      SpecialStatusUtil.setHandledBlock(block, StringUtils.join(hitEffects, "~"));
    }
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

  private void applySpread(Vector direction, double spread) {
    direction.add(new Vector(
        spread - 2 * spread * StrifePlugin.RNG.nextFloat(),
        spread - 2 * spread * StrifePlugin.RNG.nextFloat() + verticalBonus,
        spread - 2 * spread * StrifePlugin.RNG.nextFloat()));
  }
}