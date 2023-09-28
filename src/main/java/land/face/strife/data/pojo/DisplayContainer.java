package land.face.strife.data.pojo;

import java.util.ArrayList;
import java.util.List;
import land.face.strife.data.DisplayFrame;
import lombok.Getter;
import lombok.Setter;

public class DisplayContainer {

  @Getter
  private final List<DisplayFrame> frames = new ArrayList<>();
  @Getter @Setter
  private int loops = 1;

}
