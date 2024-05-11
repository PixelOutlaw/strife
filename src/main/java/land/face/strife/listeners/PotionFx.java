package land.face.strife.listeners;

import land.face.strife.data.effects.CreateModelAnimation;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PotionFx {
  private CreateModelAnimation createModelAnimation;
  private boolean scaleWithEffect;
  private String soundEffect;
  private float soundPitch;
}
