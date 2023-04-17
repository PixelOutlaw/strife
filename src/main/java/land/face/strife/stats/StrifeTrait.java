package land.face.strife.stats;

import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

import java.util.*;

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
  BLEEDING_EDGE("|orange||ul|Passive Effect - Bleeding Edge",
      " |yellow|Half of attack damage taken from",
      " |yellow|life is instead added as |red|Bleeding"),
  AIR_DASH("|orange||ul|Passive Effect - Air Dash",
      " |yellow|Air Jumps gain horizontal strength",
      " |yellow|at the cost of vertical strength"),
  BARRIER_NO_BURN("|orange||ul|Passive Effect - Heat Shield",
      " |yellow|While you have barrier, |orange||ul|Burning",
      " |yellow|deals no damage"),
  STONE_SKIN("|orange||ul|Passive Effect - Stone Skin",
      " |yellow|Receive |white|3% |yellow|less damage per",
      " |yellow|active |brown|Earth Rune"),
  BLOOD_BOIL("|orange||ul|Passive Effect - Blood Boil",
      " |yellow|When |red|Bleeding|yellow|, |yellow|all |red|Rage |yellow|gained",
      " |yellow|is increased by |white|30%"),
  BLOOD_AND_ICE("|orange||ul|Passive Effect - Blood And Ice",
      " |yellow|Apply 30% more |cyan|Frost |yellow|to |red|Bleeding |yellow|enemies",
      " |yellow|Apply 30% more |red|Bleed to |cyan|Frosted |yellow|enemies"),
  OVERSHIELD("|orange||ul|Passive Effect - Overshield",
      " |yellow|When your |white|Life |yellow|is full, gain |white|30% |yellow|of",
      " |yellow|your |white|Life Regen. |yellow|as |white|Barrier Regen."),
  ELEMENTAL_CRITS_2("|orange||ul|Passive Effect - Status Strike",
      " |yellow|Critical strikes trigger |teal|elemental status",
      " |yellow|instead of dealing extra damage"),
  LETHAL_STRIKE("|orange||ul|Passive Effect - Lethal Strike",
      " |yellow|Attacks with over 100% Crit. Chance",
      " |yellow|can trigger a |red||ul|Lethal Strike|yellow|, doubling",
      " |yellow|crit damage and re-triggering |orange||ul|ON CRIT"),
  IRON_SCARS("|orange||ul|Passive Effect - Iron Scars",
      " |yellow|Receive |white|60% |yellow|less bonus damage from",
      " |yellow|critical strikes"),
  STRIDE("|orange||ul|Passive Effect - Stride",
      " |yellow|When |white|Slowness |yellow|is gained,",
      " |yellow|reduce the effect by |white|I |yellow|level"),
  UNSTOPPABLE("|orange||ul|Passive Effect - Unstoppable",
      " |yellow|Grants immunity to |white|Slowness"),
  TOXIC_MASCULINITY("|orange||ul|Passive Effect - Toxic Masculinity",
      " |yellow|Grants immunity to |white|Vulnerable"),
  HIGHLY_MOTIVATED("|orange||ul|Passive Effect - Highly Motivated",
      " |yellow|Grants immunity to |white|Weakness"),
  ANTI_POISON("|orange||ul|Passive Effect - Anti-poison",
      " |yellow|Grants immunity to |white|Poison"),
  INCORPOREAL("|orange||ul|Passive Effect - Incorporeal",
      " |yellow|Grants immunity to |white|Physical Damage"),
  ANTI_MAGIC("|orange||ul|Passive Effect - Anti-Magic",
      " |yellow|Grants immunity to |white|Magical Damage"),
  DEADLY_POISON("|orange||ul|Passive Effect - Deadly Poison",
      " |yellow|Poison can be applied up to VII"),
  SOUL_FLAME("|orange||ul|Passive Effect - Blue Flame");

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
    this.name = PaletteUtil.color(trigger);
    this.additionalText.addAll(PaletteUtil.color(Arrays.asList(additionalText)));
  }

}
