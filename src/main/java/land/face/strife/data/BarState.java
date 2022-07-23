package land.face.strife.data;

import lombok.Data;
import org.bukkit.boss.BossBar;

@Data
public class BarState {

  private BossBar bar;
  private int priority = 0;
  private int ticks = 0;

}
