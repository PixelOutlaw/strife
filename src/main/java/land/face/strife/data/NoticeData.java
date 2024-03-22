package land.face.strife.data;

import com.sentropic.guiapi.gui.GUIComponent;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.chat.TextComponent;

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
