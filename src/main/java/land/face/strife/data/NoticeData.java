package land.face.strife.data;

import lombok.Data;
import net.md_5.bungee.api.chat.TextComponent;

@Data
public class NoticeData {

  private TextComponent textComponent;
  private int durationTicks;

  public NoticeData(TextComponent textComponent, int durationTicks) {
    this.textComponent = textComponent;
    this.durationTicks = durationTicks;
  }

}
