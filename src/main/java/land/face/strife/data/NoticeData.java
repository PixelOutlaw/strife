package land.face.strife.data;

import com.sentropic.guiapi.gui.GUIComponent;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeData {

  private GUIComponent textComponent;
  private int durationTicks;

  public NoticeData(GUIComponent guiComponent, int durationTicks) {
    this.textComponent = guiComponent;
    this.durationTicks = durationTicks;
  }

}
