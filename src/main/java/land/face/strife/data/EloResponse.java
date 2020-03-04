package land.face.strife.data;

public class EloResponse {

  private final float newWinnerValue;
  private final float newLoserValue;

  public EloResponse(float newWinnerValue, float newLoserValue) {
    this.newWinnerValue = newWinnerValue;
    this.newLoserValue = newLoserValue;
  }

  public float getNewWinnerValue() {
    return newWinnerValue;
  }

  public float getNewLoserValue() {
    return newLoserValue;
  }
}
