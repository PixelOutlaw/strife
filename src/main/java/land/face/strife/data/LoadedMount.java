package land.face.strife.data;

import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import java.util.List;
import lombok.Getter;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;

public class LoadedMount {

  @Getter
  private final String id;
  @Getter
  private final String name;
  @Getter
  private final List<String> lore;
  @Getter
  private final String modelId;
  @Getter
  private final Color color;
  @Getter
  private final Style style;
  @Getter
  private final int customModelData;
  @Getter
  private final float speed;
  @Getter
  private final float walkAnimationSpeed;
  @Getter
  private final boolean flying;
  @Getter
  private final String flyAnimation;
  @Getter
  private final String launchAnimation;
  @Getter
  private final String landAnimation;
  @Getter
  private final boolean canBeTraded;

  public LoadedMount(String id, int customModelData, String name, List<String> lore,
      String modelId, Color color, Style style, float speed, float walkAnimationSpeed,
      boolean flying, String flyAnimation, String launchAnimation, String landAnimation,
      boolean canBeTraded) {
    this.id = id;
    this.name= name;
    this.lore = TextUtils.color(lore);
    this.modelId = modelId;
    this.color = color;
    this.style = style;
    this.customModelData = customModelData;
    this.speed = speed;
    this.walkAnimationSpeed = walkAnimationSpeed;
    this.flying = flying;
    this.flyAnimation = flyAnimation;
    this.launchAnimation = launchAnimation;
    this.landAnimation = landAnimation;
    this.canBeTraded = canBeTraded;
  }

}
