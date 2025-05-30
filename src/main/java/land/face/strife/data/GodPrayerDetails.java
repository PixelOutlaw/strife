package land.face.strife.data;

import java.util.List;
import land.face.strife.data.buff.LoadedBuff;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.sound.Sound;

@Getter
@Setter
public class GodPrayerDetails {

  private LoadedBuff loadedBuff;
  private String name;
  private List<String> description;
  private float activationCost;
  private float perTickCost;
  private Sound sound;

}
