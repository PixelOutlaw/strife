package land.face.strife.data.pojo;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

public class KeyframeScript {

  @Getter @Setter
  private String bone;
  @Getter @Setter
  private List<KeyframeSound> sounds = new ArrayList<>();
  @Getter @Setter
  private List<KeyframeParticle> particles = new ArrayList<>();

  public void execute(Location location) {
    sounds.forEach(s -> s.play(location));
    particles.forEach(p -> p.play(location));
  }

}
