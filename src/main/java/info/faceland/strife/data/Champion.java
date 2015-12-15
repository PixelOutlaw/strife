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
package info.faceland.strife.data;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;

import info.faceland.strife.attributes.AttributeHandler;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.stats.StrifeStat;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Champion {

    private UUID uniqueId;
    private Map<StrifeStat, Integer> levelMap;
    private int unusedStatPoints;
    private int highestReachedLevel;

    public Champion(UUID uniqueId) {
        this.uniqueId = uniqueId;
        this.levelMap = new HashMap<>();
    }

    @Override
    public int hashCode() {
        return uniqueId != null ? uniqueId.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Champion)) {
            return false;
        }

        Champion champion = (Champion) o;

        return !(uniqueId != null ? !uniqueId.equals(champion.uniqueId) : champion.uniqueId != null);
    }

    public int getLevel(StrifeStat stat) {
        if (levelMap.containsKey(stat)) {
            return levelMap.get(stat);
        }
        return 0;
    }

    public void setLevel(StrifeStat stat, int level) {
        levelMap.put(stat, level);
    }

    public Map<StrifeAttribute, Double> getAttributeValues() {
        Map<StrifeAttribute, Double> attributeDoubleMap = new HashMap<>();
        for (StrifeAttribute attr : StrifeAttribute.values()) {
            attributeDoubleMap.put(attr, attr != StrifeAttribute.ATTACK_SPEED ? attr.getBaseValue() : 0);
        }
        for (Map.Entry<StrifeStat, Integer> entry : getLevelMap().entrySet()) {
            for (StrifeAttribute attr : StrifeAttribute.values()) {
                double val = attributeDoubleMap.get(attr);
                attributeDoubleMap
                        .put(attr, attr.getCap() > 0D ? Math
                                .min(val + entry.getKey().getAttribute(attr) * entry.getValue(), attr.getCap())
                                : val + entry.getKey().getAttribute(attr) * entry.getValue());
            }
        }
        if (getPlayer().getEquipment() == null) {
            return attributeDoubleMap;
        }
        boolean spam = false;
        for (ItemStack itemStack : getPlayer().getEquipment().getArmorContents()) {
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            if (!AttributeHandler.meetsLevelRequirement(getPlayer(), itemStack)) {
                spam = true;
                continue;
            }
            for (StrifeAttribute attr : StrifeAttribute.values()) {
                double val = attributeDoubleMap.get(attr);
                attributeDoubleMap.put(attr, attr.getCap() > 0D ? Math
                        .min(attr.getCap(), val + AttributeHandler.getValue(itemStack, attr))
                        : val + AttributeHandler.getValue(itemStack, attr));
            }
        }
        if (getPlayer().getEquipment().getItemInHand() != null
                && getPlayer().getEquipment().getItemInHand().getType() != Material.AIR) {
            ItemStack itemStack = getPlayer().getEquipment().getItemInHand();
            if (AttributeHandler.meetsLevelRequirement(getPlayer(), itemStack)) {
                for (StrifeAttribute attr : StrifeAttribute.values()) {
                    if (attr == StrifeAttribute.ARMOR || attr == StrifeAttribute.EVASION || attr ==
                            StrifeAttribute.HEALTH || attr == StrifeAttribute.MOVEMENT_SPEED || attr ==
                            StrifeAttribute.XP_GAIN) {
                        continue;
                    }
                    double val = attributeDoubleMap.get(attr);
                    attributeDoubleMap.put(attr, attr.getCap() > 0D ? Math
                            .min(val + AttributeHandler.getValue(itemStack, attr), attr.getCap())
                            : val + AttributeHandler.getValue(itemStack, attr));
                }
            } else {
                spam = true;
            }
        }
        if (spam) {
            MessageUtils.sendMessage(getPlayer(),
                    "<red>You don't meet the requirement for one of your items! It will not give any stats!");
        }
        return attributeDoubleMap;
    }

    public Map<StrifeStat, Integer> getLevelMap() {
        return new HashMap<>(levelMap);
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(getUniqueId());
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public int getUnusedStatPoints() {
        return unusedStatPoints;
    }

    public void setUnusedStatPoints(int unusedStatPoints) {
        this.unusedStatPoints = unusedStatPoints;
    }

    public int getMaximumStatLevel() {
        return 5 + (getHighestReachedLevel() / 5);
    }

    public int getHighestReachedLevel() {
        return highestReachedLevel;
    }

    public void setHighestReachedLevel(int highestReachedLevel) {
        this.highestReachedLevel = highestReachedLevel;
    }

}
