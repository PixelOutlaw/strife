package land.face.strife.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class StealthData {

  private int ticks;
  private final int maxTicks;
  private final Long readyTime;

}
