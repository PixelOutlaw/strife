package land.face.strife.stats;

import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import lombok.Getter;
import net.kyori.adventure.text.TextComponent;
import net.md_5.bungee.api.ChatColor;

import java.util.*;

public enum StrifeTrait {

  NO_HEALTH_REGEN("Health Cannot Be Regenerated"),
  NO_BARRIER_ALLOWED("Maximum Barrier Is Always Zero"),
  NO_CRIT_MULT("Critical Strikes Do No Extra Damage"),
  ACCURATE_CHARGED("Fully Charged Hits Cannot Miss"),
  ACCURATE_CRITS("Critical Strikes Cannot Miss", "|none|丒",
      "|yellow|Critical Strikes Cannot Miss"),
  NO_ENERGY_BASICS("Basic Attacks Cost No Energy"),
  ENERGY_ABSORB("20% Of Damage Taken From Energy"),
  NO_ENERGY_REGEN("Energy Does Not Regenerate"),
  BLEEDING_EDGE("|orange||ul|Passive Effect - Bleeding Edge", "|none|丒",
      "|yellow|Half of attack damage taken from",
      "|yellow|life is instead added as |red|Bleeding"),
  NO_DUAL_WIELD("|orange||ul|Passive Effect - Unwieldy", "|none|丒",
      "|yellow|You cannot dual wield"),
  AIR_DASH("|orange||ul|Passive Effect - Air Dash", "|none|丒",
      "|yellow|Air Jumps gain horizontal",
      "|yellow|speed and lose vertical speed"),
  BARRIER_NO_BURN("|orange||ul|Passive Effect - Heat Shield", "|none|丒",
      "|yellow|While you have barrier",
      "|orange||ul|Burning|yellow| deals no damage"),
  BARRIERMAN("|orange||ul|Passive Effect - Barrierman", "|none|丒",
      "|yellow|Take |white|0% |yellow|to |white|30% |yellow|less damage from",
      "|yellow|attacks, based on remaining |white|Barrier"),
  STONE_SKIN("|orange||ul|Passive Effect - Stone Skin", "|none|丒",
      "|yellow|Take |white|3% Less Damage |yellow|per",
      "|yellow|active |brown|Earth Rune"),
  BLOOD_BOIL("|orange||ul|Passive Effect - Blood Boil", "|none|丒",
      "|yellow|When |red|Bleeding|yellow|, red|Rage |yellow|gained",
      "|yellow|is increased by |white|30%"),
  LEVEL_HEADED("|orange||ul|Passive Effect - Level Headed", "|none|丒",
      "|yellow|You cannot become |crimson|Enraged"),
  BLOOD_AND_ICE("|orange||ul|Passive Effect - Blood And Ice", "|none|丒",
      "|yellow|Apply 30% more |cyan|Frost",
      "|yellow|to |red|Bleeding |yellow|enemies",
      "|yellow|Apply 30% more |red|Bleed",
      "|yellow|to |cyan|Frosted |yellow|enemies"),
  OVERSHIELD("|orange||ul|Passive Effect - Overshield", "|none|丒",
      "|red|[REQ: Full Life]",
      "|yellow|Gain |none|儡 |yellow|equal to |white|50%|none|儠"),
  OVERSHIELD_PATH("|pink||ul|Path Of Temperance", "|none|丒",
      "|red|[REQ: Full Life]",
      "|yellow|Gain |none|儡 |yellow|equal to |white|50%|none|儠"),
  OVERDRAW("|orange||ul|Passive Effect - Overdraw", "|none|丒",
      "|yellow|Basic attacks can be",
      "|yellow|overcharged for up to",
      "|white|50% |yellow|more damage"),
  ELEMENTAL_CRITS_2("|orange||ul|Passive Effect - Status Strike", "|none|丒",
      "|yellow|Critical hits trigger |teal|status",
      "|teal|hit |yellow|instead of dealing",
      "|yellow|extra damage"),
  SHARP_EDGE("|orange||ul|Passive Effect - Sharp Edge", "|none|丒",
      "|yellow|True Damage benefits from",
      "|yellow|critical strikes"),
  ICY_VEINS("|orange||ul|Passive Effect - Icy Veins", "|none|丒",
      "|yellow|Cold environments do not freeze you"),
  CORPSE_EATER("|orange||ul|Passive Effect - Corpse Eater", "|none|丒",
      " |white|Life On Kill |yellow|ignores |white|Rotting"),
  DIVINE_BLOOD("|orange||ul|Passive Effect - Divine Blood", "|none|丒",
      "|yellow|Prayer altars fully heal you"),
  LETHAL_STRIKE("|orange||ul|Passive Effect - Lethal Strike", "|none|丒",
      "|yellow|Over 100% Crit. Chance can",
      "|yellow|trigger a |red||ul|Lethal Strike",
      "|yellow|which doubles crit damage",
      "|yellow|and re-triggers |orange||ul|ON CRIT"),
  IRON_SCARS("|orange||ul|Passive Effect - Iron Scars", "|none|丒",
      "|white|-60% Extra Damage Taken |yellow|from",
      "|yellow|enemy critical strikes"),
  VENGEANCE("|orange||ul|Passive Effect - Vengeance", "|none|丒",
      "|white|+1% Damage |yellow|per |red|1% Missing Life"),
  REALLY_STUBBORN("|orange||ul|Passive Effect - Really Stubborn", "|none|丒",
      "|yellow|You cannot die"),
  MASOCHISM("|orange||ul|Passive Effect - Masochism", "|none|丒",
      "|yellow|Gain |white|20% |yellow|of lost |none|儔 |yellow|as |none|儓"),
  REBUKE("|orange||ul|Passive Effect - Rubuke", "|none|丒",
      "|yellow|Increase |lavender|Reflected Damage",
      "|yellow|by |white|1% |yellow|per |brown|Shield Mastery |yellow|Lv"),
  PERMAFROST("|orange||ul|Passive Effect - Permafrost", "|none|丒",
      "|cyan|Frost |yellow|does not decay"),

  STRIDE("|orange||ul|Passive Effect - Stride", "|none|丒",
      "|yellow|When |white|Slow |yellow|is gained,",
      "|yellow|reduce the effect by |white|I"),
  UNSTOPPABLE("|orange||ul|Passive Effect - Unstoppable", "|none|丒",
      "|yellow|Immunity to |white|Slow"),
  TOXIC_MASCULINITY("|orange||ul|Passive Effect - Toxic Masculinity", "|none|丒",
      "|yellow|Immunity to |white|Vulnerable"),
  HIGHLY_MOTIVATED("|orange||ul|Passive Effect - Highly Motivated", "|none|丒",
      "|yellow|Immunity to |white|Weak"),
  EXTRA_FRESH("|orange||ul|Passive Effect - Extra Fresh", "|none|丒",
      "|yellow|Immunity to |white|Rotting"),
  LEMON_SCENTED("|orange||ul|Passive Effect - Lemon Scented", "|none|丒",
      "|yellow|Immunity to |white|Stink"),
  ANTI_POISON("|orange||ul|Passive Effect - Anti-poison", "|none|丒",
      "|yellow|Immunity to |white|Poison"),

  INCORPOREAL("|orange||ul|Passive Effect - Incorporeal", "|none|丒",
      "|yellow|Grants immunity to |white|Physical Damage"),
  DEADLY_POISON("|orange||ul|Passive Effect - Deadly Poison", "|none|丒",
      "|yellow|Poison can be applied up to VII"),
  POISON_EATER("|orange||ul|Passive Effect - Poison Eater", "|none|丒",
      "|yellow|Attacks have |white|4% |yellow|Life Steal",
      "|yellow|per |lgreen|Poison |yellow|on enemies"),
  DEATH_EATER("|orange||ul|Passive Effect - Death Eater", "|none|丒",
      "|yellow|Healing from attacks is increased",
      "|yellow|by |white|1% |yellow|per enemy |purple|Corruption"),
  ICE_PICK("|orange||ul|Passive Effect - Ice Pick", "|none|丒",
      "|red|Physical Damage |yellow|is increased",
      "|yellow|by |white|1% |yellow|per enemy |cyan|Frost"),
  RUNNER("|orange||ul|Passive Effect - Runner", "|none|丒",
      "|yellow|Sprinting costs no energy"),

  SHIELD_BOUNCE("|orange||ul|Passive Effect - Shield Bounce", "|none|丒",
      "|yellow|Shields can block fall damage"),
  SOFT_LANDING("|orange||ul|Passive Effect - Soft Landing", "|none|丒",
      "|yellow|Grants immunity to |teal|Fall Damage"),
  SKY_CRASH("|orange||ul|Passive Effect - Sky Crash", "|none|丒",
      "|yellow|When falling, damage taken is",
      "|yellow|reduced by |white|10% |yellow|and basic melee",
      "|yellow|attacks deal |white|20% |yellow|more damage"
  ),

  WEAK_LINK("|orange||ul|Passive - Weak Link", "|none|丒",
      "|yellow|Enemy elemental resists", "|yellow|are capped at their 2nd", "|yellow|lowest resistance"
  ),

  RESIST_PHYSICAL("|orange||ul|Resistance - Physical", "|none|丒",
      "|yellow|Physical damage taken is halved"),
  RESIST_FIRE("|orange||ul|Resistance - Fire", "|none|丒",
      "|yellow|Fire damage taken is halved"),
  RESIST_ICE("|orange||ul|Resistance - Ice", "|none|丒",
      "|yellow|Ice damage taken is halved"),
  RESIST_LIGHTNING("|orange||ul|Resistance - Lightning", "|none|丒",
      "|yellow|Lightning damage taken is halved"),
  RESIST_EARTH("|orange||ul|Resistance - Earth", "|none|丒",
      "|yellow|Earth damage taken is halved"),
  RESIST_LIGHT("|orange||ul|Resistance - Light", "|none|丒",
      "|yellow|Light damage taken is halved"),
  RESIST_DARK("|orange||ul|Resistance - Shadow", "|none|丒",
      "|yellow|Shadow damage taken is halved"),

  FAITH_DEFENSE("|orange||ul|Passive Effect - Divine Armor", "|none|丒",
      "|pink||ul|Armor Of Faith|yellow| grants |white|10%",
      "|yellow|damage reduction per missing",
      "|yellow|piece of armor"
  ),
  FAITH_DAMAGE("|orange||ul|Passive Effect - Weapon Of Faith", "|none|丒",
      "|pink||ul|Fervor|yellow| grants |white|5% damage",
      "|yellow|per missing piece of armor"
  ),

  COIN_FLIP("|orange||ul|Passive Effect - Coin Flip", "|none|丒",
      "|white|Crit. Chance |yellow|is capped at |white|50%"
  ),

  SLURP("|orange||ul|Passive Effect - Super Slurper", "|none|丒",
      "|yellow|Drink potions twice as quickly"),
  FAST_ACTING("|orange||ul|Passive Effect - Fast Acting", "|none|丒",
      "|yellow|Life/Energy from potions",
      "|yellow|that applies over time is",
      "|yellow|instead applied instantly"
  ),
  SOUL_FLAME("|orange||ul|Passive Effect - Blue Flame", "|none|丒",
      "|yellow|Ignore half of enemy",
      "|yellow|Fire Resistance"
  );

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
  @Getter
  private final List<TextComponent> descAsComponents = new ArrayList<>();
  @Getter
  private final List<String> additionalTextRaw = new ArrayList<>();

  StrifeTrait(String trigger, String... additionalText) {
    this.name = PaletteUtil.color(trigger);
    this.additionalTextRaw.addAll(Arrays.asList(additionalText));
    this.additionalText.addAll(PaletteUtil.color(Arrays.asList(additionalText)));
    this.additionalText.forEach(at -> descAsComponents.add(PaletteUtil.convert(at)));
  }

}
