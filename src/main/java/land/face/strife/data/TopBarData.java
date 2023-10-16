package land.face.strife.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TopBarData {

  private String compass = "";
  private String location = "";
  private String clock = "";
  private String skills = "";

  public String getFinalTitle() {
    return compass + location + clock + skills;
  }

}
