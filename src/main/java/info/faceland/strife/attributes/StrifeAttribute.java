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
  SHOCK_CHANCE("Shock Chance"),
  FREEZE_CHANCE("Freeze Chance"),
  CORRUPT_CHANCE("Corrupt Chance"),
  MAX_EARTH_RUNES("Max Earth Runes"),

  BLEED_CHANCE("Bleed Chance"),
  BLEED_DAMAGE("Bleed Damage"),

  LIFE_STEAL("Life Steal"),
  HP_ON_HIT("Health On Hit"),
  HP_ON_KILL("Health On Kill"),

  MULTISHOT("Multishot"),

  MOVEMENT_SPEED("Movement Speed"),

  CRAFT_SKILL("Crafting Skill"),
  ENCHANT_SKILL("Enchanting Skill"),
  FISH_SKILL("Fishing Skill"),
  MINE_SKILL("Mining Skill"),

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

  EXPLOSION_MAGIC("Explosion Magic");

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
