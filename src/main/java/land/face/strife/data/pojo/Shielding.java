package land.face.strife.data.pojo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Shielding {

  private float amount;
  private final float maxAmount;

  public Shielding(float amount) {
    this.amount = amount;
    this.maxAmount = amount;
  }

}
