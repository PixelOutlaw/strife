package land.face.strife.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;

public class VagabondClass {

  @Getter
  private final String id;
  @Getter
  private final Map<String, Integer> levelsPerAttribute = new HashMap<>();
  @Getter
  private final Map<String, Integer> startLevelPerAttribute = new HashMap<>();
  @Getter
  private final List<String> possibleAbilitiesA = new ArrayList<>();
  @Getter
  private final List<String> possibleAbilitiesB = new ArrayList<>();
  @Getter
  private final List<String> possibleAbilitiesC = new ArrayList<>();

  public VagabondClass(String id) {
    this.id = id;
  }

}
