package land.face.strife.data;

import land.face.strife.data.champion.SkillRank;
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
  private SkillRank initialRank;
  private float initialLevel;
  private float initialStealthBonus;
  private boolean canBlock;
}
