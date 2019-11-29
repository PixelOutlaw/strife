package land.face.strife.data;

import java.util.ArrayList;
import java.util.List;
import land.face.strife.data.effects.Effect;
import land.face.strife.data.effects.StrifeParticle;

public class LoadedChaser {

  private float speed;
  private float maxSpeed;
  private float startSpeed;
  private int lifespan;
  private boolean removeAtSolids;
  private boolean strictDuration;
  private List<Effect> effectList = new ArrayList<>();
  private List<StrifeParticle> particles = new ArrayList<>();

  public float getSpeed() {
    return speed;
  }

  public void setSpeed(float speed) {
    this.speed = speed;
  }

  public float getMaxSpeed() {
    return maxSpeed;
  }

  public void setMaxSpeed(float maxSpeed) {
    this.maxSpeed = maxSpeed;
  }

  public float getStartSpeed() {
    return startSpeed;
  }

  public void setStartSpeed(float startSpeed) {
    this.startSpeed = startSpeed;
  }

  public int getLifespan() {
    return lifespan;
  }

  public void setLifespan(int lifespan) {
    this.lifespan = lifespan;
  }

  public boolean isRemoveAtSolids() {
    return removeAtSolids;
  }

  public void setRemoveAtSolids(boolean removeAtSolids) {
    this.removeAtSolids = removeAtSolids;
  }

  public boolean isStrictDuration() {
    return strictDuration;
  }

  public void setStrictDuration(boolean strictDuration) {
    this.strictDuration = strictDuration;
  }

  public List<Effect> getEffectList() {
    return effectList;
  }

  public List<StrifeParticle> getParticles() {
    return particles;
  }
}
