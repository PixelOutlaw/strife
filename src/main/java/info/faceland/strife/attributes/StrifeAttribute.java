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

public enum StrifeAttribute {

    // modify any attributes here
    HEALTH("Health", 20, false),
    REGENERATION("Regeneration", 1.0, true),
    ARMOR("Armor", 0, false),
    RESISTANCE("Elemental Resist", 0.2, true),
    BLOCK("Block", 0.0, true, 0.85),
    ABSORB_CHANCE("Absorb Chance", 0, true, 0.35),
    PARRY("Parry Chance", 0, true, 0.75),
    EVASION("Evasion", 0, false),
    MOVEMENT_SPEED("Movement Speed", 100, false),
    MELEE_DAMAGE("Melee Damage", 1, false),
    RANGED_DAMAGE("Ranged Damage", 3, false),
    MAGIC_DAMAGE("Magic Damage", 1, false),
    ATTACK_SPEED("Attack Speed", 2.0D, true),
    OVERCHARGE("Overcharge", 0.1, true),
    ARMOR_PENETRATION("Armor Penetration", 0, true, 0.7),
    ACCURACY("Accuracy", 0.2, true, 0.85),
    CRITICAL_RATE("Critical Rate", 0.05, true),
    CRITICAL_DAMAGE("Critical Damage", 1.5D, true),
    FIRE_DAMAGE("Fire Damage", 0, false),
    LIGHTNING_DAMAGE("Lightning Damage", 0, false),
    ICE_DAMAGE("Ice Damage", 0, false),
    IGNITE_CHANCE("Ignite Chance", 0.15, true),
    SHOCK_CHANCE("Shock Chance", 0.15, true),
    FREEZE_CHANCE("Freeze Chance", 0.15, true),
    LIFE_STEAL("Life Steal", 0.0, true, 0.65),
    XP_GAIN("Experience Gain", 0D, true),
    ITEM_DISCOVERY("Item Discovery", 0D, true),
    GOLD_FIND("Gold Find", 0D, true),
    HEAD_DROP("Head Drop", 0D, true),
    LEVEL_REQUIREMENT("Level Requirement", 0, false),
    DOGE("Doge Chance", 0D, true, 100D);

    private final String name;
    private final double baseValue;
    private final boolean percentage;
    private final double cap;

    StrifeAttribute(String name, double baseValue, boolean percentage) {
        this(name, baseValue, percentage, -1D);
    }

    StrifeAttribute(String name, double baseValue, boolean percentage, double cap) {
        this.name = name;
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

    public String getName() { return name; }

    public double getBaseValue() {
        return baseValue;
    }

    public boolean isPercentage() { return percentage; }

    public double getCap() {
        return cap;
    }

}
