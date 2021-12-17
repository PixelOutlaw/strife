package land.face.strife.stats;

import io.pixeloutlaw.minecraft.spigot.garbage.ListExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

public enum StrifeTrait {

  ELEMENTAL_CRITS("Elemental Damage Can Crit"),
  NO_HEALTH_REGEN("Health Cannot Be Regenerated"),
  NO_BARRIER_ALLOWED("Maximum Barrier Is Always Zero"),
  NO_CRIT_MULT("Critical Strikes Do No Extra Damage"),
  ACCURATE_CHARGED("Fully Charged Hits Cannot Miss"),
  ACCURATE_CRITS("Critical Strikes Cannot Miss"),
  NO_ENERGY_BASICS("Basic Attacks Cost No Energy"),
  ENERGY_ABSORB("20% Of Damage Taken From Energy"),
  NO_ENERGY_REGEN("Energy Does Not Regenerate"),
  BLEEDING_EDGE("&6&nPassive Effect - Bleeding Edge", " &eHalf of attack damage taken from", " &elife is instead added as &4Bleeding"),
  BARRIER_NO_BURN("&6&nPassive Effect - Heat Shield", " &eWhile you have barrier, &6&nBurning", " &edeals no damage"),
  STONE_SKIN("&6&nPassive Effect - Stone Skin", " &eReceive 3% less damage per", " &eactive &2Earth Rune"),
  BLOOD_BOIL("&6&nPassive Effect - Blood Boil", " &eWhen &4Bleeding&e, &eall &cRage &egained", " &eis increased by &f30%"),
  BLOOD_AND_ICE("&6&nPassive Effect - Blood And Ice", " &eApply 30% more &bFrost &eto &4Bleeding &eenemies", " &eApply 30% more &4Bleed to &bFrosted &eenemies"),
  SOUL_FLAME("Passive Effect - Blue Flame");

  // TODO: We map String to StrifeStat, why not let the user
  //  customize the string rather than declaring it in the enum?
  private static final Map<String, StrifeTrait> copyOfValues = buildStringToTraitMap();

  private static Map<String, StrifeTrait> buildStringToTraitMap() {
    Map<String, StrifeTrait> values = new HashMap<>();
    for (StrifeTrait trait : StrifeTrait.values()) {
      if (trait.getName() == null) {
        continue;
      }
      values.put(ChatColor.stripColor(trait.getName()), trait);
    }
    return values;
  }

  public static StrifeTrait fromName(String trigger) {
    return copyOfValues.getOrDefault(trigger, null);
  }

  @Getter
  private final String name;
  @Getter
  private final List<String> additionalText = new ArrayList<>();

  StrifeTrait(String trigger, String... additionalText) {
    this.name = StringExtensionsKt.chatColorize(trigger);
    this.additionalText.addAll(ListExtensionsKt.chatColorize(Arrays.asList(additionalText)));
  }

}
