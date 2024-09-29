package land.face.strife.data;

import java.util.List;
import land.face.strife.data.pojo.KeyframeParticle;
import land.face.strife.data.pojo.KeyframeSound;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ContactPrefab {

  private final String id;
  private final String activeModel;
  private final String inactiveModel;
  private final String contactAnimation;
  private final int modelSwapDelay;
  private final double contactSize;
  private final double effectOffset;
  private final List<String> commands;
  private final List<KeyframeParticle> particles;
  private final List<KeyframeSound> sounds;

}
