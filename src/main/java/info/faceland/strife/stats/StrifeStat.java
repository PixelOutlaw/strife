/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package info.faceland.strife.stats;

import java.util.HashMap;
import java.util.Map;

public enum StrifeStat {

  LEVEL_REQUIREMENT("Level Requirement"),

  HEALTH("Maximum Life"),
  REGENERATION("Base Life Regeneration"),

  BARRIER("Maximum Barrier"),
  BARRIER_SPEED("Barrier Recharge Rate"),
  BARRIER_REGEN("Barrier Regeneration"),

  PHYSICAL_DAMAGE("Physical Damage"),
  MAGIC_DAMAGE("Magical Damage"),

  ARMOR("Armor"),
  WARDING("Warding"),
  EVASION("Evasion"),

  FIRE_RESIST("Fire Resistance"),
  ICE_RESIST("Ice Resistance"),
  LIGHTNING_RESIST("Lightning Resistance"),
  EARTH_RESIST("Earth Resistance"),
  LIGHT_RESIST("Light Resistance"),
  DARK_RESIST("Shadow Resistance"),
  ALL_RESIST("Elemental Resist"),

  BLOCK("Block"),
  BLOCK_RECOVERY("Block Recovery"),

  TENACITY("Tenacity"),

  DAMAGE_REDUCTION("Damage Reduction"),
  DAMAGE_REFLECT("Reflected Damage"),

  MULTISHOT("Multishot"),
  PROJECTILE_SPEED("Projectile Speed"),
  PROJECTILE_DAMAGE("Projectile Damage"),
  PROJECTILE_REDUCTION("Projectile Damage Reduction"),

  ATTACK_SPEED("Attack Speed"),
  OVERCHARGE("Overcharge"),

  CRITICAL_RATE("Critical Chance"),
  CRITICAL_DAMAGE("Critical Damage"),

  ARMOR_PENETRATION("Armor Penetration"),
  WARD_PENETRATION("Ward Penetration"),
  ACCURACY("Accuracy"),

  FIRE_DAMAGE("Fire Damage"),
  LIGHTNING_DAMAGE("Lightning Damage"),
  ICE_DAMAGE("Ice Damage"),
  EARTH_DAMAGE("Earth Damage"),
  LIGHT_DAMAGE("Light Damage"),
  DARK_DAMAGE("Shadow Damage"),

  IGNITE_CHANCE("Ignite Chance"),
  IGNITE_DURATION("Ignite Duration"),
  SHOCK_CHANCE("Shock Chance"),
  FREEZE_CHANCE("Freeze Chance"),
  CORRUPT_CHANCE("Corrupt Chance"),
  MAX_EARTH_RUNES("Max Earth Runes"),

  MAXIMUM_RAGE("Maximum Rage"),
  RAGE_ON_HIT("Rage On Hit"),
  RAGE_WHEN_HIT("Rage When Hit"),
  RAGE_ON_KILL("Rage On Kill"),

  BLEED_CHANCE("Bleed Chance"),
  BLEED_DAMAGE("Bleed Damage"),
  BLEED_RESIST("Bleed Resistance"),

  LIFE_STEAL("Life Steal"),
  HP_ON_HIT("Life On Hit"),
  HP_ON_KILL("Life On Kill"),

  MINION_DAMAGE("Minion Damage"),
  MINION_LIFE("Minion Max Life"),
  MAX_MINIONS("Maximum Minions"),

  MOVEMENT_SPEED("Movement Speed"),
  TRUE_DAMAGE("True Damage"),

  PVP_ATTACK("PvP Attack"),
  PVP_DEFENCE("PvP Defence"),

  CRAFT_SKILL("Crafting Skill Level"),
  ENCHANT_SKILL("Enchanting Skill Level"),
  FISH_SKILL("Fishing Skill Level"),
  MINE_SKILL("Mining Skill Level"),
  SNEAK_SKILL("Sneak Skill Level"),

  XP_GAIN("Experience Gain"),
  SKILL_XP_GAIN("Skill Experience Gain"),
  ITEM_DISCOVERY("Item Discovery"),
  ITEM_RARITY("Item Rarity"),
  GOLD_FIND("Gold Find"),
  HEAD_DROP("Head Drop"),

  DOGE("Doge Chance"),

  HEALTH_MULT(),
  REGEN_MULT("Life Regeneration"),
  ARMOR_MULT(),
  EVASION_MULT(),
  WARD_MULT(),

  MELEE_PHYSICAL_MULT(),
  RANGED_PHYSICAL_MULT(),
  MAGIC_MULT(),

  DAMAGE_MULT(),
  ELEMENTAL_MULT("Elemental Damage"),
  ACCURACY_MULT(),
  MINION_MULT_INTERNAL(),

  SPELL_STRIKE_RANGE("Spell Strike Range"),
  EFFECT_DURATION("Effect Duration");

  // values() is dumb, so we only run it once, and hit use this to
  // change String to enum instead of try catching or values()
  // TODO: We map String to StrifeStat, why not let the user customize the string rather than declaring it in the enum?
  private static final Map<String, StrifeStat> copyOfValues = buildStringToAttributeMap();

  private static Map<String, StrifeStat> buildStringToAttributeMap() {
    Map<String, StrifeStat> values = new HashMap<>();
    for (StrifeStat stat : StrifeStat.values()) {
      if (stat.getName() == null) {
        continue;
      }
      values.put(stat.getName(), stat);
    }
    return values;
  }

  public static StrifeStat fromName(String name) {
    return copyOfValues.getOrDefault(name, null);
  }

  private final String name;

  StrifeStat(String name) {
    this.name = name;
  }

  StrifeStat() {
    this.name = null;
  }

  public String getName() {
    return name;
  }

}
