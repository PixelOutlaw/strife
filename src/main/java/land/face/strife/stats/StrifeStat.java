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
package land.face.strife.stats;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;

public enum StrifeStat {

  LEVEL_REQUIREMENT("Level Requirement"),
  SKILL_REQUIREMENT("Skill Requirement"),

  CHARACTER_SIZE("Character Size"),
  ATTACK_RANGE("Melee Reach"),
  WEIGHT("Weight"),

  HEALTH("Maximum Life"),
  REGENERATION("Life Regeneration"),

  ENERGY("Maximum Energy"),
  ENERGY_MULT("More Max. Energy", "Less Max. Energy"),

  ENERGY_REGEN("Energy Regeneration"),
  ENERGY_REGEN_MULT("More Energy Regen", "Less Energy Regen"),

  ENERGY_ON_HIT("Energy On Hit"),
  ENERGY_ON_KILL("Energy On Kill"),
  ENERGY_WHEN_HIT("Energy When Hit"),

  BARRIER("Maximum Barrier"),
  BARRIER_MULT("More Max. Barrier", "Less Max. Barrier"),
  BARRIER_SPEED("Barrier Recharge Rate"),
  BARRIER_REGEN("Barrier Regeneration"),
  BARRIER_START_SPEED("Shorter Barrier Delay"),
  DAMAGE_TO_BARRIERS("Damage To Barriers"),

  PHYSICAL_DAMAGE("Physical Damage"),
  PHYSICAL_MULT("More Physical Damage", "Less Physical Damage"),
  MELEE_PHYSICAL_MULT("Melee Physical Damage"),
  RANGED_PHYSICAL_MULT("Ranged Physical Damage"),

  ABILITY_DAMAGE("Ability Damage"),

  ARMOR("Armor"),
  EVASION("Evasion"),
  DODGE_CHANCE("Dodge Chance"),

  FIRE_RESIST("Fire Resistance"),
  ICE_RESIST("Ice Resistance"),
  LIGHTNING_RESIST("Lightning Resistance"),
  EARTH_RESIST("Earth Resistance"),
  LIGHT_RESIST("Light Resistance"),
  DARK_RESIST("Shadow Resistance"),
  ALL_RESIST("Elemental Resist"),

  POISON_RESIST("Poison Resistance"),
  BURNING_RESIST("Burning Resistance"),

  BLOCK("Block"),
  BLOCK_RECOVERY("Block Recovery"),

  TENACITY("Tenacity"),
  COOLDOWN_REDUCTION("Cooldown Reduction"),

  DAMAGE_REDUCTION("Damage Reduction"),
  DAMAGE_REFLECT("Reflected Damage"),

  MULTISHOT("Multishot"),
  EXTRA_PROJECTILES("Extra Projectiles"),
  PIERCE_CHANCE("Pierce Chance"),
  PROJECTILE_SPEED("Projectile Speed"),
  PROJECTILE_DAMAGE("Projectile Damage"),
  PROJECTILE_REDUCTION("Projectile Protection"),

  LUNG_CAPACITY("Lung Capacity"),

  ATTACK_SPEED("Attack Speed"),
  CRITICAL_RATE("Critical Chance"),
  CRITICAL_DAMAGE("Critical Damage"),

  ARMOR_PENETRATION("Armor Penetration"),
  ACCURACY("Accuracy"),

  FIRE_DAMAGE("Fire Damage"),
  LIGHTNING_DAMAGE("Lightning Damage"),
  ICE_DAMAGE("Ice Damage"),
  EARTH_DAMAGE("Earth Damage"),
  LIGHT_DAMAGE("Light Damage"),
  DARK_DAMAGE("Shadow Damage"),

  FIRE_MULT("More Fire Damage", "Less Fire Damage"),
  LIGHTNING_MULT("More Lightning Damage", "Less Lightning Damage"),
  ICE_MULT("More Ice Damage", "Less Ice Damage"),
  EARTH_MULT("More Earth Damage", "Less Earth Damage"),
  LIGHT_MULT("More Light Damage", "Less Light Damage"),
  DARK_MULT("More Shadow Damage", "Less Shadow Damage"),

  ELEMENTAL_STATUS("Elemental Status Chance"),
  MAX_EARTH_RUNES("Max Earth Runes"),

  MAXIMUM_RAGE("Maximum Rage"),
  MAXIMUM_RAGE_MULT("More Max. Rage", "Less Max. Rage"),
  ENRAGE_DURATION("Enrage Duration"),
  RAGE_ON_HIT("Rage On Hit"),
  RAGE_WHEN_HIT("Rage When Hit"),
  RAGE_ON_KILL("Rage On Kill"),

  BLEED_CHANCE("Bleed Chance"),
  BLEED_DAMAGE("Bleed Damage"),
  BLEED_RESIST("Bleed Resistance"),

  POISON_CHANCE("Poison Chance"),
  POISON_DURATION("Poison Duration"),

  LIFE_STEAL("Life Steal"),
  HP_ON_HIT("Life On Hit"),
  HP_ON_KILL("Life On Kill"),
  HEALING_POWER("Healing Power"),

  MAX_PRAYER_POINTS("Maximum Faith"),
  PRAYER_COST("Piety"),

  MINION_DAMAGE("Minion Damage"),
  MINION_LIFE("Minion Max Life"),
  MINION_SPEED("Minion Move Speed"),
  MAX_MINIONS("Maximum Minions"),

  AREA_OF_EFFECT("Area Of Effect"),
  CAST_RANGE("Casting Range"),

  MOVEMENT_SPEED("Movement Speed"),
  TRUE_DAMAGE("True Damage"),

  PVP_ATTACK("PvP Attack"),
  PVP_DEFENCE("PvP Defence"),

  XP_GAIN("Combat XP"),
  SKILL_XP_GAIN("Skill XP"),

  ITEM_DISCOVERY("Loot Bonus"),
  ITEM_RARITY("Loot Rarity"),
  GOLD_FIND("Bits Dropped"),

  FISHING_SPEED("Fishing Speed"),
  FISHING_TREASURE("Fishing Treasures"),
  FISH_BAIT_KEEP("Less Bait Consumed"),
  AUTO_FISH_CHANCE("AutoFish Chance"),

  CRAFT_SKILL("Crafting Skill"),
  ENCHANT_SKILL("Enchanting Skill"),
  SNEAK_SKILL("Sneak Skill"), // DEPRECATED

  STEALTH_BONUS("Stealthiness"),
  SNEAK_ATTACK_BONUS("Sneak Attack Damage"),

  MINING_SPEED("Mining Speed"),
  MINING_GEMS("Mining Gems"),

  GATHERING_SPEED("Gathering Speed"),

  MONEY_KEPT("Bits Kept On Death"),
  XP_LOST_ON_DEATH("Less XP Loss On Death"),

  JUMP_HEIGHT("Jump Height"),
  FALL_DAMAGE("Reduced Fall Damage"),
  REDUCED_GRAVITY("Reduced Gravity"),
  INCREASED_GRAVITY("Increased Gravity"),
  AIR_JUMPS("Additional Air Jumps"),

  DOGE("Doge Chance"),

  HEALTH_MULT("More Max. Life", "Less Max. Life"),
  REGEN_MULT("More Life Regeneration", "Less Life Regeneration"),
  ARMOR_MULT("More Armor", "Less Armor"),
  EVASION_MULT("More Evasion", "Less Evasion"),

  DAMAGE_MULT("Damage"),
  DAMAGE_TAKEN("Damage Taken"),
  ELEMENTAL_MULT("Elemental Damage"),
  ACCURACY_MULT("More Accuracy", "Less Accuracy"),
  MINION_MULT_INTERNAL(),

  LIFE_FROM_POTIONS("Life From Potions"),
  ENERGY_FROM_POTIONS("Energy From Potions"),
  POTION_REFILL("Potion Refill Speed"),
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

  @Getter
  private final String name, negativeName;

  StrifeStat(String name, String negativeName) {
    this.name = name;
    this.negativeName = negativeName;
  }

  StrifeStat(String name) {
    this.name = name;
    this.negativeName = name;
  }

  StrifeStat() {
    this.name = null;
    this.negativeName = null;
  }

}
