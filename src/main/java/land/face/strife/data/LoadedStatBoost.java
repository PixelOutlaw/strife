package land.face.strife.data;

import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.noShadow;

import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import land.face.strife.stats.StrifeStat;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

@Getter
@Setter
public class LoadedStatBoost {

  private final String creator;
  private final int duration;
  private final int announceInterval;
  private final TextComponent topBarText;
  private final TextComponent topBarIcon;
  private final Map<StrifeStat, Float> stats;
  private final List<String> announceStart;
  private final List<String> announceRun;
  private final List<String> announceEnd;
  private String modernId = null;
  private String modernLines = null;

  public LoadedStatBoost(String creator, String topBarIcon, String topBarText, int announceInterval, int duration) {
    this.creator = creator;
    this.duration = duration;
    this.announceInterval = announceInterval;
    this.topBarIcon = noShadow(Component.text(topBarIcon));
    this.topBarText = PaletteUtil.convert(PaletteUtil.color(topBarText));
    this.stats = new HashMap<>();
    this.announceStart = new ArrayList<>();
    this.announceRun = new ArrayList<>();
    this.announceEnd = new ArrayList<>();
  }
}
