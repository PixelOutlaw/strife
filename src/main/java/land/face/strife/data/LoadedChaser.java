package land.face.strife.data;

import java.util.ArrayList;
import java.util.List;
import land.face.strife.data.effects.Effect;
import land.face.strife.data.effects.StrifeParticle;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoadedChaser {

  private float speed;
  private float maxSpeed;
  private float startSpeed;
  private int lifespan;
  private boolean removeAtSolids;
  private boolean strictDuration;
  private final List<Effect> effectList = new ArrayList<>();
  private final List<StrifeParticle> particles = new ArrayList<>();

}
