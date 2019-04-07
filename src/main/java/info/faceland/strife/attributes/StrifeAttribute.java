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
package info.faceland.strife.attributes;

import java.util.HashMap;
import java.util.Map;

public enum StrifeAttribute {

  LEVEL_REQUIREMENT("Level Requirement"),

  HEALTH("Health"),
  REGENERATION("Base Regeneration"),

  BARRIER("Maximum Barrier"),
  BARRIER_SPEED("Barrier Recharge Rate"),

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

  DAMAGE_REDUCTION("Damage Reduction"),
  DAMAGE_REFLECT("Reflected Damage"),
  PROJECTILE_REDUCTION("Projectile Damage Reduction"),

  MELEE_DAMAGE("Melee Damage"),
  RANGED_DAMAGE("Ranged Damage"),
  MAGIC_DAMAGE("Magic Damage"),

  ATTACK_SPEED("Attack Speed"),
  OVERCHARGE("Overcharge"),

  CRITICAL_RATE("Critical Rate"),
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
  HP_ON_HIT("Health On Hit"),
  HP_ON_KILL("Health On Kill"),

  MULTISHOT("Multishot"),

  MOVEMENT_SPEED("Movement Speed"),
  TRUE_DAMAGE("True Damage"),

  PVP_ATTACK("PvP Attack"),
  PVP_DEFENCE("PvP Defence"),

  CRAFT_SKILL("Crafting Skill Level"),
  ENCHANT_SKILL("Enchanting Skill Level"),
  FISH_SKILL("Fishing Skill Level"),
  MINE_SKILL("Mining Skill Level"),

  XP_GAIN("Experience Gain"),
  SKILL_XP_GAIN("Skill Experience Gain"),
  ITEM_DISCOVERY("Item Discovery"),
  ITEM_RARITY("Item Rarity"),
  GOLD_FIND("Gold Find"),
  HEAD_DROP("Head Drop"),

  DOGE("Doge Chance"),

  HEALTH_MULT(),
  REGEN_MULT("Regeneration"),
  ARMOR_MULT(),
  EVASION_MULT(),
  WARD_MULT(),
  MELEE_MULT(),
  RANGED_MULT(),
  MAGIC_MULT(),
  DAMAGE_MULT(),
  PROJECTILE_SPEED("Projectile Speed"),
  ELEMENTAL_MULT("Elemental Damage"),
  ACCURACY_MULT(),
  APEN_MULT(),
  WPEN_MULT(),

  EXPLOSION_MAGIC("Explosion Magic"),
  SPELL_STRIKE_RANGE("Spell Strike Range"),
  EFFECT_DURATION("Effect Duration"),

  HEALTH_PER_TEN_B_LEVEL("Max Health Per Ten BLvl"),
  BARRIER_PER_TEN_B_LEVEL("Max Barrier Per Ten BLvl"),
  DAMAGE_PER_TEN_B_LEVEL("Damage Per Ten BLvl"),
  ARMOR_PER_TEN_B_LEVEL("Armor Per Ten BLvl"),
  EVASION_PER_TEN_B_LEVEL("Evasion Per Ten BLvl"),
  MS_PER_TEN_B_LEVEL("Move Speed Per Ten BLvl"),
  AS_PER_TEN_B_LEVEL("Attack Speed Per Ten BLvl");

  // values() is dumb, so we only run it once, and hit use this to
  // change String to enum instead of try catching or values()
  // TODO: We map String to StrifeAttribute, why not let the user customize the string rather than declaring it in the enum?
  private static final Map<String, StrifeAttribute> copyOfValues = buildStringToAttributeMap();

  private static Map<String, StrifeAttribute> buildStringToAttributeMap() {
    Map<String, StrifeAttribute> values = new HashMap<>();
    for (StrifeAttribute attribute : StrifeAttribute.values()) {
      if (attribute.getName() == null) {
        continue;
      }
      values.put(attribute.getName(), attribute);
    }
    return values;
  }

  public static StrifeAttribute fromName(String name) {
    return copyOfValues.getOrDefault(name, null);
  }

  private final String name;

  StrifeAttribute(String name) {
    this.name = name;
  }

  StrifeAttribute() {
    this.name = null;
  }

  public String getName() {
    return name;
  }

}
