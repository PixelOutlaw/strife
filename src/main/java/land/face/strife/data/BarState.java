package land.face.strife.data;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.bossbar.BossBar;

@Getter @Setter
public class BarState {

  private BossBar bar;
  private int priority = 0;
  private int ticks = 0;

}
