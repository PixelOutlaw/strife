package land.face.strife.data;

import land.face.strife.data.effects.StrifeParticle;

public class ContinuousParticle {

  private final StrifeParticle particle;
  private int ticksRemaining;

  public ContinuousParticle(StrifeParticle strifeParticle, int ticksRemaining) {
    this.particle = strifeParticle;
    this.ticksRemaining = ticksRemaining;
  }

  public StrifeParticle getParticle() {
    return particle;
  }

  public int getTicksRemaining() {
    return ticksRemaining;
  }

  public void setTicksRemaining(int ticksRemaining) {
    this.ticksRemaining = ticksRemaining;
  }
}
