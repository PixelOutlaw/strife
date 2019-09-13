package info.faceland.strife.data;

import info.faceland.strife.data.effects.SpawnParticle;

public class ContinuousParticle {

  private final SpawnParticle particle;
  private int ticksRemaining;

  public ContinuousParticle(SpawnParticle spawnParticle, int ticksRemaining) {
    this.particle = spawnParticle;
    this.ticksRemaining = ticksRemaining;
  }

  public SpawnParticle getParticle() {
    return particle;
  }

  public int getTicksRemaining() {
    return ticksRemaining;
  }

  public void tickDown() {
    ticksRemaining--;
  }
}
