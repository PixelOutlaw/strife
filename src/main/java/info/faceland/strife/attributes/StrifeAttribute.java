/**
 * The MIT License
 * Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package info.faceland.strife.attributes;

import org.bukkit.ChatColor;

public enum StrifeAttribute {

    // modify any attributes here
    HEALTH("Health", ChatColor.BLUE, "Maximum Health. Two Health = One Heart.", 20, false),
    REGENERATION("Regeneration", ChatColor.BLUE, "How much health you recover per natural regeneration tick.", 1, false),
    ARMOR("Armor", ChatColor.BLUE, "Damage reduction from normal attacks.", 0, true),
    RESISTANCE("Resistance", ChatColor.BLUE, "[PVP ONLY] Reduces the probability of status effects.", 0, true),
    BLOCK("Block", ChatColor.BLUE, "Percent by which incoming damage is reduced when blocking.", 0.1, true, 0.85),
    PARRY("Parry Chance", ChatColor.BLUE, "Chance to take no damage and reflect damage when blocking.", 0, true, 0.75),
    EVASION("Evasion", ChatColor.BLUE, "Chance to dodge an attack entirely.", 0, true),
    DAMAGE_REFLECT("Damage Reflect", ChatColor.BLUE, "Percentage of damage taken reflected back at your attacker.", 0, true, 0.3),
    MOVEMENT_SPEED("Movement Speed", ChatColor.BLUE, "Player's movement speed, base is 100, 200 is double.", 100, false),
    MELEE_DAMAGE("Melee Damage", ChatColor.YELLOW, "Base melee damage dealt.", 1, false),
    RANGED_DAMAGE("Ranged Damage", ChatColor.YELLOW, "Maximum damage dealt by fired arrows.", 3, false),
    SNARE_CHANCE("Snare Chance", ChatColor.YELLOW, "Chance to snare enemies struck by your arrows for 2s", 0, true),
    ATTACK_SPEED("Attack Speed", ChatColor.YELLOW, "How quickly your melee damage recharges inbetween attacks", 2D, true),
    OVERCHARGE("Overcharge", ChatColor.YELLOW, "Bonus damage dealt when your attacks are fully recharged", 0.1, true),
    ARMOR_PENETRATION("Armor Penetration", ChatColor.YELLOW, "Percentage of enemy armor that is ignored.", 0, true),
    ACCURACY("Accuracy", ChatColor.YELLOW, "Percent that enemy's evasion is reduced", 0, true),
    CRITICAL_RATE("Critical Rate", ChatColor.YELLOW, "Chance of landing a critical strike, dealing bonus damage.", 0, true),
    CRITICAL_DAMAGE("Critical Damage", ChatColor.YELLOW, "Bonus Damage on crit", 1.3D, true),
    FIRE_DAMAGE("Fire Damage", ChatColor.YELLOW, "On Ignite: Burns target for X fire ticks.", 0, false),
    LIGHTNING_DAMAGE("Lightning Damage", ChatColor.YELLOW, "On Shock: Deals X bonus true damage.", 0, false),
    ICE_DAMAGE("Ice Damage", ChatColor.YELLOW, "On Freeze: Deals X% health damage and slows.", 0, false),
    IGNITE_CHANCE("Ignite Chance", ChatColor.YELLOW, "Chance to ignite target on hit.", 0.15, true),
    SHOCK_CHANCE("Shock Chance", ChatColor.YELLOW, "Chance to shock target on hit.", 0.15, true),
    FREEZE_CHANCE("Freeze Chance", ChatColor.YELLOW, "Chance to freeze target on hit.", 0.15, true),
    LIFE_STEAL("Life Steal", ChatColor.YELLOW, "Percentage of damage dealt recovered as health.", 0, true),
    XP_GAIN("Experience Gain", ChatColor.GREEN, "Bonus xp gained", 0D, true),
    ITEM_DISCOVERY("Item Discovery", ChatColor.GREEN, "Bonus drop rate", 0D, true),
    GOLD_FIND("Gold Find", ChatColor.GREEN, "Bonus Bits dropped", 0D, true),
    DOGE("Doge Chance", ChatColor.AQUA, "wow", 0D, true, 100D);

    private final String name;
    private final ChatColor displayColor;
    private final String description;
    private final double baseValue;
    private final boolean percentage;
    private final double cap;

    StrifeAttribute(String name, ChatColor displayColor, String description, double baseValue,
                    boolean percentage) {
        this(name, displayColor, description, baseValue, percentage, -1D);
    }

    StrifeAttribute(String name, ChatColor displayColor, String description, double baseValue, boolean percentage,
                    double cap) {
        this.name = name;
        this.displayColor = displayColor;
        this.description = description;
        this.baseValue = baseValue;
        this.percentage = percentage;
        this.cap = cap;
    }

    public static StrifeAttribute fromName(String s) {
        for (StrifeAttribute val : values()) {
            if (val.name.equalsIgnoreCase(s) || val.name().equalsIgnoreCase(s) || val.name.replace(" ", "-")
                .equalsIgnoreCase(s) ||
                val.name().replace("_", "-").equalsIgnoreCase(s)) {
                return val;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public double getBaseValue() {
        return baseValue;
    }

    public boolean isPercentage() {
        return percentage;
    }

    public ChatColor getDisplayColor() {
        return displayColor;
    }

    public double getCap() {
        return cap;
    }

}
