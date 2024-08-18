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
  ACCURATE_CRITS("Critical Strikes Cannot Miss",
      "|none|丒", "|yellow|Critical Strikes Cannot Miss"),
  NO_ENERGY_BASICS("Basic Attacks Cost No Energy"),
  ENERGY_ABSORB("20% Of Damage Taken From Energy"),
  NO_ENERGY_REGEN("Energy Does Not Regenerate"),
  BLEEDING_EDGE("|orange||ul|Passive Effect - Bleeding Edge",
      "|none|丒", "|yellow|Half of attack damage taken from",
      "|yellow|life is instead added as |red|Bleeding"),
  AIR_DASH("|orange||ul|Passive Effect - Air Dash",
      "|none|丒", "|yellow|Air Jumps gain horizontal strength",
      "|yellow|at the cost of vertical strength"),
  BARRIER_NO_BURN("|orange||ul|Passive Effect - Heat Shield",
      "|none|丒", "|yellow|While you have barrier, |orange||ul|Burning",
      "|yellow|deals no damage"),
  STONE_SKIN("|orange||ul|Passive Effect - Stone Skin",
      "|none|丒", "|white|-3% Damage Taken |yellow|per |brown|Earth Rune"),
  BLOOD_BOIL("|orange||ul|Passive Effect - Blood Boil",
      "|none|丒", "|yellow|When |red|Bleeding|yellow|, |yellow|all |red|Rage |yellow|gained",
      "|yellow|is increased by |white|30%"),
  BLOOD_AND_ICE("|orange||ul|Passive Effect - Blood And Ice",
      "|none|丒", "|yellow|Apply 30% more |cyan|Frost |yellow|to |red|Bleeding |yellow|enemies",
      "|yellow|Apply 30% more |red|Bleed to |cyan|Frosted |yellow|enemies"),
  OVERSHIELD("|orange||ul|Passive Effect - Overshield",
      "|none|丒", "|yellow|When your |white|Life |yellow|is full, gain |white|50% |yellow|of",
      "|yellow|your |white|Life Regen. |yellow|as |white|Barrier Regen."),
  OVERDRAW("|orange||ul|Passive Effect - Overdraw",
      "|none|丒", "|yellow|Basic attacks can be overcharged",
      "|yellow|for up to |white|35% |yellow|more damage"),
  ELEMENTAL_CRITS_2("|orange||ul|Passive Effect - Status Strike",
      "|none|丒", "|yellow|Critical strikes trigger |teal|elemental status",
      "|yellow|instead of dealing extra damage"),
  ICY_VEINS("|orange||ul|Passive Effect - Icy Veins",
      "|none|丒", "|yellow|Cold environments do not freeze you"),
  CORPSE_EATER("|orange||ul|Passive Effect - Corpse Eater",
      " |white|Life On Kill |yellow|ignores |white|Rotting"),
  DIVINE_BLOOD("|orange||ul|Passive Effect - Divine Blood",
      "|none|丒", "|yellow|Prayer altars fully heal you"),
  LETHAL_STRIKE("|orange||ul|Passive Effect - Lethal Strike",
      "|none|丒",
      "|yellow|Over 100% Crit. Chance can trigger",
      "|yellow|a |red||ul|Lethal Strike|yellow| which",
      "|yellow|doubles crit damage and re-triggers",
      "|yellow|all |orange||ul|ON CRIT|yellow| effects"),
  IRON_SCARS("|orange||ul|Passive Effect - Iron Scars",
      "|none|丒", "|white|-60% Extra Damage Taken |yellow|from",
      "|yellow|enemy critical strikes"),
  VENGEANCE("|orange||ul|Passive Effect - Vengeance",
      "|none|丒", "|white|+1% Damage |yellow|per |red|1% Missing Life"),
  REALLY_STUBBORN("|orange||ul|Passive Effect - Really Stubborn",
      "|none|丒", "|yellow|You cannot die"),
  MASOCHISM("|orange||ul|Passive Effect - Masochism", "|none|丒",
      "|yellow|Gain |white|20% |yellow|of lost Life as Energy"),
  PERMAFROST("|orange||ul|Passive Effect - Permafrost", "|none|丒", "|cyan|Frost |yellow|does not decay"),

  STRIDE("|orange||ul|Passive Effect - Stride",
      "|none|丒", "|yellow|When |white|Slow |yellow|is gained,",
      "|yellow|reduce the effect by |white|I"),
  UNSTOPPABLE("|orange||ul|Passive Effect - Unstoppable",
      "|none|丒", "|yellow|Immunity to |white|Slow"),
  TOXIC_MASCULINITY("|orange||ul|Passive Effect - Toxic Masculinity",
      "|none|丒", "|yellow|Immunity to |white|Vulnerable"),
  HIGHLY_MOTIVATED("|orange||ul|Passive Effect - Highly Motivated",
      "|none|丒", "|yellow|Immunity to |white|Weak"),
  EXTRA_FRESH("|orange||ul|Passive Effect - Extra Fresh",
      "|none|丒", "|yellow|Immunity to |white|Rotting"),
  LEMON_SCENTED("|orange||ul|Passive Effect - Lemon Scented",
      "|none|丒", "|yellow|Immunity to |white|Stink"),
  ANTI_POISON("|orange||ul|Passive Effect - Anti-poison",
      "|none|丒", "|yellow|Immunity to |white|Poison"),

  INCORPOREAL("|orange||ul|Passive Effect - Incorporeal",
      "|none|丒", "|yellow|Grants immunity to |white|Physical Damage"),
  DEADLY_POISON("|orange||ul|Passive Effect - Deadly Poison",
      "|none|丒", "|yellow|Poison can be applied up to VII"),
  SHIELD_BOUNCE("|orange||ul|Passive Effect - Shield Bounce",
      "|none|丒", "|yellow|Shields can block fall damage"),
  SOFT_LANDING("|orange||ul|Passive Effect - Soft Landing",
      "|none|丒", "|yellow|Grants immunity to |teal|Fall Damage"),
  RUNNER("|orange||ul|Passive Effect - Runner",
      "|none|丒", "|yellow|Sprinting costs no energy"),

  WEAK_LINK("|orange||ul|Passive - Weak Link",
      "|none|丒", "|yellow|Enemy elemental resists", "|yellow|are capped at their 2nd", "|yellow|lowest resistance"
  ),

  RESIST_PHYSICAL("|orange||ul|Resistance - Physical",
      "|none|丒", "|yellow|Physical damage taken is halved"),
  RESIST_FIRE("|orange||ul|Resistance - Fire",
      "|none|丒", "|yellow|Fire damage taken is halved"),
  RESIST_ICE("|orange||ul|Resistance - Ice",
      "|none|丒", "|yellow|Ice damage taken is halved"),
  RESIST_LIGHTNING("|orange||ul|Resistance - Lightning",
      "|none|丒", "|yellow|Lightning damage taken is halved"),
  RESIST_EARTH("|orange||ul|Resistance - Earth",
      "|none|丒", "|yellow|Earth damage taken is halved"),
  RESIST_LIGHT("|orange||ul|Resistance - Light",
      "|none|丒", "|yellow|Light damage taken is halved"),
  RESIST_DARK("|orange||ul|Resistance - Shadow",
      "|none|丒", "|yellow|Shadow damage taken is halved"),

  FAITH_DEFENSE("|orange||ul|Passive Effect - Divine Armor",
      "|pink|Armor Of Faith |yellow|reduces damage by",
      "|yellow|10% per missing armor piece"
  ),
  FAITH_DAMAGE("|orange||ul|Passive Effect - Weapon Of Faith",
      "|none|丒", "|pink|Fervor |yellow|increases damage by",
      "|yellow|5% per missing armor piece"
  ),

  COIN_FLIP("|orange||ul|Passive Effect - Coin Flip",
      "|none|丒", "|white|Crit. Chance |yellow|is capped at |white|50%"
  ),

  SLURP("|orange||ul|Passive Effect - Super Slurper",
      "|none|丒", "|yellow|Drink potions twice as quickly"),
  FAST_ACTING("|orange||ul|Passive Effect - Fast Acting",
      "|none|丒", "|yellow|Life/Energy from potions",
      "|yellow|that applies over time is",
      "|yellow|instead applied instantly"
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
