package land.face.strife.stats;

import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

import java.util.*;

public enum StrifeTrait {

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
  ICY_VEINS("|orange||ul|Passive Effect - Icy Veins",
      " |yellow|Cold environments do not freeze you"),
  LETHAL_STRIKE("|orange||ul|Passive Effect - Lethal Strike",
      " |yellow|Attacks with over 100% Crit. Chance",
      " |yellow|can trigger a |red||ul|Lethal Strike|yellow|, doubling",
      " |yellow|crit damage and re-triggering |orange||ul|ON CRIT"),
  IRON_SCARS("|orange||ul|Passive Effect - Iron Scars",
      " |yellow|Receive |white|60% |yellow|less bonus damage from",
      " |yellow|critical strikes"),
  VENGEANCE("|orange||ul|Passive Effect - Vengeance",
      " |yellow|Gain 1% Damage Per 1% Missing Life"),
  REALLY_STUBBORN("|orange||ul|Passive Effect - Really Stubborn",
      " |yellow|You cannot die"),
  MASOCHISM("|orange||ul|Passive Effect - Masochism",
      " |yellow|Gain |white|20% |yellow|of lost Life as Energy"),

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
  DEADLY_POISON("|orange||ul|Passive Effect - Deadly Poison",
      " |yellow|Poison can be applied up to VII"),
  SHIELD_BOUNCE("|orange||ul|Passive Effect - Shield Bounce",
      " |yellow|Shields can block fall damage"),
  SOFT_LANDING("|orange||ul|Passive Effect - Soft Landing",
      " |yellow|Grants immunity to |teal|Fall Damage"),
  RUNNER("|orange||ul|Passive Effect - Runner",
      " |yellow|Sprinting costs no energy"),

  RESIST_PHYSICAL("|orange||ul|Resistance - Physical",
      " |yellow|Physical damage taken is halved"),
  RESIST_FIRE("|orange||ul|Resistance - Fire",
      " |yellow|Fire damage taken is halved"),
  RESIST_ICE("|orange||ul|Resistance - Ice",
      " |yellow|Ice damage taken is halved"),
  RESIST_LIGHTNING("|orange||ul|Resistance - Lightning",
      " |yellow|Lightning damage taken is halved"),
  RESIST_EARTH("|orange||ul|Resistance - Earth",
      " |yellow|Earth damage taken is halved"),
  RESIST_LIGHT("|orange||ul|Resistance - Light",
      " |yellow|Light damage taken is halved"),
  RESIST_DARK("|orange||ul|Resistance - Shadow",
      " |yellow|Shadow damage taken is halved"),

  SLURP("|orange||ul|Passive Effect - Super Slurper",
      " |yellow|Drink potions twice as quickly"),
  FAST_ACTING("|orange||ul|Passive Effect - Fast Acting",
      " |yellow| Life/Energy from potions",
      " |yellow| that applies over time is",
      " |yellow| instead applied instantly"
  ),
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
