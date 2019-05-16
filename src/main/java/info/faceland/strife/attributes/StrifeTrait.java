package info.faceland.strife.attributes;

import java.util.HashMap;
import java.util.Map;

public enum StrifeTrait {

  ELEMENTAL_CRITS("Elemental Damage Can Critically Strike"),
  NO_HEALTH_REGEN("Health Cannot Be Regenerated"),
  NO_BARRIER_ALLOWED("Maximum Barrier Is Always Zero"),
  NO_CRIT_MULT("Critical Strikes Do No Extra Damage"),
  ACCURATE_CHARGED("Fully Charged Hits Cannot Miss"),
  ACCURATE_CRITS("Critical Strikes Cannot Miss");

  // values() is dumb, so we only run it once, and hit use this to
  // change String to enum instead of try catching or values()
  // TODO: We map String to StrifeAttribute, why not let the user customize the string rather than declaring it in the enum?
  private static final Map<String, StrifeTrait> copyOfValues = buildStringToTraitMap();

  private static Map<String, StrifeTrait> buildStringToTraitMap() {
    Map<String, StrifeTrait> values = new HashMap<>();
    for (StrifeTrait trait : StrifeTrait.values()) {
      if (trait.getName() == null) {
        continue;
      }
      values.put(trait.getName(), trait);
    }
    return values;
  }

  public static StrifeTrait fromName(String name) {
    return copyOfValues.getOrDefault(name, null);
  }

  private final String name;

  StrifeTrait(String name) {
    this.name = name;
  }

  StrifeTrait() {
    this.name = null;
  }

  public String getName() {
    return name;
  }

}
