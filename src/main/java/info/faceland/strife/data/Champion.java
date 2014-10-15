/******************************************************************************
 * Copyright (c) 2014, Richard Harrah                                         *
 *                                                                            *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted, provided that the above copyright notice and this permission notice appear in all copies.
 *                                                                            *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 ******************************************************************************/

package info.faceland.strife.data;

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

    public UUID getUniqueId() {
        return uniqueId;
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

    @Override
    public int hashCode() {
        return uniqueId != null ? uniqueId.hashCode() : 0;
    }

    public Map<StrifeStat, Integer> getLevelMap() {
        return new HashMap<>(levelMap);
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

    public Player getPlayer() {
        return Bukkit.getPlayer(getUniqueId());
    }

    public Map<StrifeAttribute, Double> getAttributeValues() {
        Map<StrifeAttribute, Double> attributeDoubleMap = new HashMap<>();
        for (Map.Entry<StrifeStat, Integer> entry : getLevelMap().entrySet()) {
            for (StrifeAttribute attr : StrifeAttribute.values()) {
                attributeDoubleMap.put(attr, entry.getKey().getAttribute(attr) * entry.getValue());
            }
        }
        for (ItemStack itemStack : getPlayer().getEquipment().getArmorContents()) {
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            for (StrifeAttribute attr : StrifeAttribute.values()) {
                double val = attributeDoubleMap.containsKey(attr) ? attributeDoubleMap.get(attr) : 0;
                if (!attributeDoubleMap.containsKey(attr)) {
                    val += attr.getBaseValue();
                }
                attributeDoubleMap.put(attr, val + AttributeHandler.getValue(itemStack, attr));
            }
        }
        if (getPlayer().getEquipment().getItemInHand() != null && getPlayer().getEquipment().getItemInHand().getType() != Material.AIR) {
            ItemStack itemStack = getPlayer().getEquipment().getItemInHand();
            for (StrifeAttribute attr : StrifeAttribute.values()) {
                double val = attributeDoubleMap.containsKey(attr) ? attributeDoubleMap.get(attr) : 0;
                if (!attributeDoubleMap.containsKey(attr)) {
                    val += attr.getBaseValue();
                }
                attributeDoubleMap.put(attr, val + AttributeHandler.getValue(itemStack, attr));
            }
        }
        return attributeDoubleMap;
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
