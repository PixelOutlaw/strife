package land.face.strife.data.effects;

import com.tealcube.minecraft.bukkit.facecore.utilities.FireworkUtil;
import land.face.strife.data.StrifeMob;
import lombok.Setter;
import org.bukkit.Color;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;

public class FireworkBurst extends LocationEffect {

  @Setter
  private Type effectType;
  @Setter
  private Color colorOne;
  @Setter
  private Color colorTwo;
  @Setter
  private boolean trail;
  @Setter
  private boolean flicker;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    Location loc = target.getEntity().getLocation().clone();
    applyAtLocation(caster, loc);
  }

  @Override
  public void applyAtLocation(StrifeMob caster, Location location) {
    FireworkUtil.spawnFirework(location, effectType, colorOne, colorTwo, trail, flicker);
  }
}
