package land.face.strife.data;

import land.face.strife.data.effects.StrifeParticle;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContinuousParticle {

  private final StrifeParticle particle;
  private int ticksRemaining;

  public ContinuousParticle(StrifeParticle strifeParticle, int ticksRemaining) {
    this.particle = strifeParticle;
    this.ticksRemaining = ticksRemaining;
  }
}
