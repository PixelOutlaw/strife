/******************************************************************************
 * Copyright (c) 2014, Richard Harrah                                         *
 *                                                                            *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted, provided that the above copyright notice and this permission notice appear in all copies.
 *                                                                            *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 ******************************************************************************/

package info.faceland.strife.attributes;

import org.bukkit.ChatColor;
import org.bukkit.Sound;

public enum StrifeAttribute {

    // modify any attributes here
    MELEE_DAMAGE("Melee Damage", ChatColor.YELLOW, "Maximum damage dealt by melee attacks", 1, false, null),
    RANGED_DAMAGE("Ranged Damage", ChatColor.YELLOW, "Maximum damage dealt by bow", 1, false, null),
    CRITICAL_RATE("Critical Rate", ChatColor.YELLOW, "Chance of landing a critical strike", 0, true, null),
    CRITICAL_DAMAGE("Critical Damage", ChatColor.YELLOW, "100% Critical Damage = no bonus. 200% Critical Damage = 2x.", 1.5D, true, null),
    ATTACK_SPEED("Attack Speed", ChatColor.YELLOW, "100% Attack Speed = 2s. 200% Attack Speed = 1s.", 2D, true, null),
    FIRE_DAMAGE("Fire Damage", ChatColor.YELLOW, "Sets enemies on fire.", 0, false, null),
    LIFE_STEAL("Life Steal", ChatColor.YELLOW, "Heals for a percentage of damage dealt.", 0, true, null),
    HEALTH("Health", ChatColor.BLUE, "Increases maximum health.", 20, false, null),
    REGENERATION("Regeneration", ChatColor.BLUE, "Increases health regained during natural regeneration.", 0, false, null),
    ARMOR("Armor", ChatColor.BLUE, "Protects from a percentage of damage received.", 0, true, null),
    ARMOR_PENETRATION("Armor Penetration", ChatColor.YELLOW, "Percentage of enemy armor that is ignored.", 0, true, null),
    DAMAGE_REFLECT("Damage Reflect", ChatColor.BLUE, "Percentage of damage taken reflected back to enemy.", 0, true, null),
    EVASION("Evasion", ChatColor.BLUE, "Chance to dodge an attack entirely.", 0, true, null),
    BLOCK("Block", ChatColor.BLUE, "Percentage of damage taken to reduce when blocking.", 0, true, null),
    PARRY("Parry Chance", ChatColor.BLUE, "Chance to take no damage and reflect some damage when blocking.", 0, true, null);

    private final String name;
    private final ChatColor displayColor;
    private final String description;
    private final double baseValue;
    private final boolean percentage;
    private final Sound sound;

    private StrifeAttribute(String name, ChatColor displayColor, String description, double baseValue, boolean percentage, Sound sound) {
        this.name = name;
        this.displayColor = displayColor;
        this.description = description;
        this.baseValue = baseValue;
        this.percentage = percentage;
        this.sound = sound;
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

    public Sound getSound() {
        return sound;
    }

    public static StrifeAttribute fromName(String s) {
        for (StrifeAttribute val : values()) {
            if (val.name.equalsIgnoreCase(s) || val.name().equalsIgnoreCase(s)) {
                return val;
            }
        }
        return null;
    }

    public ChatColor getDisplayColor() {
        return displayColor;
    }

}
