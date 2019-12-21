package land.face.strife.data.effects;

import java.util.Random;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import org.bukkit.Location;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.metadata.FixedMetadataValue;

public class EvokerFangEffect extends LocationEffect {

  private int quantity;
  private float spread;
  private String hitEffects;

  private static final int MAX_GROUND_CHECK = 9;
  private static final Random RANDOM = new Random();
  public static final String FANG_META = "EFFECT_FANGS";

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
      EvokerFangs fangs = fangLoc.getWorld().spawn(fangLoc, EvokerFangs.class);
      fangs.setOwner(caster.getEntity());
      fangs.setMetadata(FANG_META, new FixedMetadataValue(StrifePlugin.getInstance(), hitEffects));
    }
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  public void setSpread(float spread) {
    this.spread = spread;
  }

  public void setHitEffects(String hitEffects) {
    this.hitEffects = hitEffects;
  }
}