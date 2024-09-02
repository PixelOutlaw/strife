package land.face.strife.data;

import lombok.Builder;
import lombok.Getter;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;

@Getter
@Builder
public class LoadedMount {

  private final String id;
  private final String modelId;
  private final Color color;
  private final Style style;
  private final float speed;
  private final float walkAnimationSpeed;
  private final boolean flying;
  private final String flyAnimation;
  private final String launchAnimation;
  private final String landAnimation;

}
