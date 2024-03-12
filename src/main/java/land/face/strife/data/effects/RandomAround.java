package land.face.strife.data.effects;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.List;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.TargetResponse;
import land.face.strife.util.TargetingUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;

@Getter @Setter
public class RandomAround extends LocationEffect {

  private final List<Effect> effects = new ArrayList<>();
  private int quantity;
  private float randomRotate;
  private float minRange;
  private float maxRange;
  private boolean grounded;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    applyAtLocation(caster, TargetingUtil.getOriginLocation(target.getEntity(), getOrigin(), getExtra()));
  }

  @Override
  public void applyAtLocation(StrifeMob caster, Location location) {

    Location aaa =  caster.getEntity().getLocation().clone();
    aaa.setPitch(0);
    Vector vec = aaa.getDirection();
    vec.normalize();

    float baseRadians = (360f / quantity) * 0.01745329f;
    float randomRadians = randomRotate * 0.01745329f;
    float rangeBonus = maxRange - minRange;

    for (int i = 0; i <= quantity; i++) {
      vec.rotateAroundY(baseRadians - randomRadians + (randomRadians * Math.random() * 2));
      vec.normalize();
      vec.multiply(minRange + Math.random() * rangeBonus);

      Location targetLoc = location.clone().add(vec);

      if (grounded) {
        targetLoc.add(0, 1, 0);
        if (targetLoc.getBlock().isSolid()) {
          continue;
        }
        if (targetLoc.getBlock().getRelative(BlockFace.DOWN, 1).isSolid()) {
          targetLoc.setY(Math.floor(targetLoc.getY()) + 0.5);
        } else if (targetLoc.getBlock().getRelative(BlockFace.DOWN, 2).isSolid()) {
          targetLoc.setY(Math.floor(targetLoc.getY()) - 0.5);
        } else if (targetLoc.getBlock().getRelative(BlockFace.DOWN, 3).isSolid()) {
          targetLoc.setY(Math.floor(targetLoc.getY()) - 1.5);
        } else if (targetLoc.getBlock().getRelative(BlockFace.DOWN, 4).isSolid()) {
          targetLoc.setY(Math.floor(targetLoc.getY()) - 2.5);
        } else if (targetLoc.getBlock().getRelative(BlockFace.DOWN, 5).isSolid()) {
          targetLoc.setY(Math.floor(targetLoc.getY()) - 3.5);
        } else {
          continue;
        }
      } else {
        if (targetLoc.getBlock().isSolid()) {
          continue;
        }
      }
      TargetResponse response = new TargetResponse(targetLoc);
      getPlugin().getEffectManager().processEffectList(caster, response, effects);
    }
  }
}
