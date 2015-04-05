/*
 * This file is part of Strife, licensed under the ISC License.
 *
 * Copyright (c) 2014 Richard Harrah
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted,
 * provided that the above copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF
 * THIS SOFTWARE.
 */
package info.faceland.strife.attributes;

import org.bukkit.ChatColor;

public enum StrifeAttribute {

    // modify any attributes here
    HEALTH("Health", ChatColor.BLUE, "Maximum Health. Two Health = One Heart.", 20, false),
    REGENERATION("Regeneration", ChatColor.BLUE, "How much health you recover per natural regeneration tick.", 1, false),
    ARMOR("Armor", ChatColor.BLUE, "Percent by which incoming damage is reduced.", 0, true, 0.95),
    BLOCK("Block", ChatColor.BLUE, "Percent by which incoming damage is reduced (after armor) when blocking.", 0, true),
    PARRY("Parry Chance", ChatColor.BLUE, "Chance to take no damage and reflect damage when blocking.", 0, true),
    EVASION("Evasion", ChatColor.BLUE, "Chance to dodge an attack entirely.", 0, true, 0.8),
    DAMAGE_REFLECT("Damage Reflect", ChatColor.BLUE, "Percentage of damage taken reflected back at your attacker.", 0, true),
    MOVEMENT_SPEED("Movement Speed", ChatColor.BLUE, "Player's movement speed, base is 100, 200 is double.", 100, false),
    MELEE_DAMAGE("Melee Damage", ChatColor.YELLOW, "Base melee damage dealt.", 1, false),
    RANGED_DAMAGE("Ranged Damage", ChatColor.YELLOW, "Maximum damage dealt by fired arrows.", 1, false),
    ATTACK_SPEED("Attack Speed", ChatColor.YELLOW, "How quickly your melee damage recharges inbetween attacks", 2D, true),
    OVERCHARGE("Overcharge", ChatColor.YELLOW, "Bonus damage dealt when your attacks are fully recharged", 0, true),
    ARMOR_PENETRATION("Armor Penetration", ChatColor.YELLOW, "Percentage of enemy armor that is ignored.", 0, true),
    CRITICAL_RATE("Critical Rate", ChatColor.YELLOW, "Chance of landing a critical strike, dealing bonus damage.", 0, true),
    CRITICAL_DAMAGE("Critical Damage", ChatColor.YELLOW, "Percent damage you deal when you critically strike.", 1.25D, true),
    FIRE_DAMAGE("Fire Damage", ChatColor.YELLOW, "On hit, ignites enemies for one second per point.", 0, false),
    LIFE_STEAL("Life Steal", ChatColor.YELLOW, "Percentage of damage dealt recovered as health.", 0, true),
    XP_GAIN("Experience Gain", ChatColor.GREEN, "Increases the rate at which experience is gained.", 0D, true),
    ITEM_DISCOVERY("Item Discovery", ChatColor.GREEN, "Increases the rate at which items are found.", 0D, true),
    GOLD_FIND("Gold Find", ChatColor.GREEN, "Increases the amount of Gold dropped by monsters killed.", 0D, true),
    DOGE("Doge Chance", ChatColor.AQUA, "Much funny, very meme, so doge, wow.", 0D, true, 100D);

    private final String name;
    private final ChatColor displayColor;
    private final String description;
    private final double baseValue;
    private final boolean percentage;
    private final double cap;

    private StrifeAttribute(String name, ChatColor displayColor, String description, double baseValue, boolean percentage) {
        this(name, displayColor, description, baseValue, percentage, -1D);
    }

    private StrifeAttribute(String name, ChatColor displayColor, String description, double baseValue, boolean percentage, double cap) {
        this.name = name;
        this.displayColor = displayColor;
        this.description = description;
        this.baseValue = baseValue;
        this.percentage = percentage;
        this.cap = cap;
    }

    public static StrifeAttribute fromName(String s) {
        for (StrifeAttribute val : values()) {
            if (val.name.equalsIgnoreCase(s) || val.name().equalsIgnoreCase(s) || val.name.replace(" ", "-").equalsIgnoreCase(s) ||
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
