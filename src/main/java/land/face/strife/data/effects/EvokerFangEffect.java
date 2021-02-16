package land.face.strife.data.effects;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import land.face.strife.data.StrifeMob;
import land.face.strife.util.FangUtil;
import org.bukkit.Location;

public class EvokerFangEffect extends LocationEffect {

  private int quantity;
  private float spread;

  private final List<Effect> hitEffects = new ArrayList<>();

  private static final int MAX_GROUND_CHECK = 9;
  private static final Random RANDOM = new Random();

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    applyAtLocation(caster, target.getEntity().getLocation());
  }

  @Override
  public void applyAtLocation(StrifeMob caster, Location location) {
    for (int i = 0; i < quantity; i++) {
      Location fangLoc = location.clone();
      fangLoc.add(0, 2, 0);
      fangLoc.setX(fangLoc.getX() - spread + spread * 2 * RANDOM.nextFloat());
      fangLoc.setZ(fangLoc.getZ() - spread + spread * 2 * RANDOM.nextFloat());
      if (fangLoc.getBlock().getType().isSolid()) {
        continue;
      }
      boolean groundFound = false;
      for (int j = 0; j < MAX_GROUND_CHECK; j++) {
        if (fangLoc.getBlock().getType().isSolid()) {
          fangLoc.setY(fangLoc.getBlockY() + 1.1);
          groundFound = true;
          break;
        }
        fangLoc.add(0, -1, 0);
      }
      if (!groundFound) {
        continue;
      }
      FangUtil.createFang(caster, fangLoc, hitEffects, getId());
    }
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  public void setSpread(float spread) {
    this.spread = spread;
  }

  public List<Effect> getHitEffects() {
    return hitEffects;
  }
}