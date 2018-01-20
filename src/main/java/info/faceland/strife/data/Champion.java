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
import info.faceland.strife.util.ItemTypeUtil;
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
    private Map<StrifeAttribute, Double> currentBaseStats;
    private int unusedStatPoints;
    private int highestReachedLevel;
    private ChampionCache cache;

    private final static String LVL_REQ_MAIN_WEAPON = "<red>You do not meet the level requirement for your weapon! " +
        "It will not give you any stats when used!";
    private final static String LVL_REQ_OFF_WEAPON = "<red>You do not meet the level requirement for your offhand " +
        "item! It will not give you any stats when used!";
    private final static String LVL_REQ_ARMOR = "<red>You do not meet the level requirement for a piece of your " +
        "armor! It will not give you any stats while equipped!";

    public Champion(UUID uniqueId) {
        this.uniqueId = uniqueId;
        this.levelMap = new HashMap<>();
        this.cache = new ChampionCache(this.uniqueId);
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

    public Map<StrifeAttribute, Double> getEntityAttributeValues() {
        cache.clearBaseStatCache();
        cache.setBaseStatCache(currentBaseStats);
        return currentBaseStats;
    }

    public Map<StrifeAttribute, Double> getLevelpointAttributeValues() {
        cache.clearStatCache();
        Map<StrifeAttribute, Double> attributeDoubleMap = new HashMap<>();
        for (Map.Entry<StrifeStat, Integer> entry : getLevelMap().entrySet()) {
            for (Map.Entry<StrifeAttribute, Double> pointSection : entry.getKey().getAttributeMap().entrySet()) {
                double amount = pointSection.getValue() * entry.getValue();
                if (attributeDoubleMap.containsKey(pointSection.getKey())) {
                    amount += attributeDoubleMap.get(pointSection.getKey());
                }
                attributeDoubleMap.put(pointSection.getKey(), amount);
            }
        }
        cache.setAttributeStatCache(attributeDoubleMap);
        return attributeDoubleMap;
    }

    public Map<StrifeAttribute, Double> getArmorAttributeValues() {
        cache.clearArmorCache();
        Map<StrifeAttribute, Double> attributeDoubleMap = new HashMap<>();
        boolean spam = false;
        for (ItemStack itemStack : getPlayer().getEquipment().getArmorContents()) {
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            Map<StrifeAttribute, Double> itemStatMap = AttributeHandler.getItemStats(itemStack);
            if (itemStatMap.containsKey(StrifeAttribute.LEVEL_REQUIREMENT)) {
                if (getPlayer().getLevel() < itemStatMap.get(StrifeAttribute.LEVEL_REQUIREMENT)) {
                    spam = true;
                    continue;
                }
            }
            attributeDoubleMap = AttributeHandler.combineMaps(attributeDoubleMap, itemStatMap);
        }
        if (spam) {
            MessageUtils.sendMessage(getPlayer(), LVL_REQ_ARMOR);
        }

        cache.setAttributeArmorCache(attributeDoubleMap);
        return attributeDoubleMap;
    }

    public Map<StrifeAttribute, Double> getWeaponAttributeValues() {
        cache.clearWeaponCache();
        Map<StrifeAttribute, Double> attributeDoubleMap = new HashMap<>();
        ItemStack mainHandItemStack = getPlayer().getEquipment().getItemInMainHand();
        ItemStack offHandItemStack = getPlayer().getEquipment().getItemInOffHand();
        if (mainHandItemStack != null && mainHandItemStack.getType() != Material.AIR && !ItemTypeUtil.isArmor(mainHandItemStack.getType())) {
            Map<StrifeAttribute, Double> itemStatMap = AttributeHandler.getItemStats(mainHandItemStack);
            if (itemStatMap.containsKey(StrifeAttribute.LEVEL_REQUIREMENT) && getPlayer().getLevel() < itemStatMap
                .get(StrifeAttribute.LEVEL_REQUIREMENT)) {
                MessageUtils.sendMessage(getPlayer(), LVL_REQ_MAIN_WEAPON);
            } else {
                attributeDoubleMap = AttributeHandler.combineMaps(attributeDoubleMap, itemStatMap);
            }
        }
        if (offHandItemStack != null && offHandItemStack.getType() != Material.AIR && !ItemTypeUtil.isArmor(offHandItemStack.getType())) {
            double dualWieldEfficiency = ItemTypeUtil.getDualWieldEfficiency(mainHandItemStack, offHandItemStack);
            Map<StrifeAttribute, Double> itemStatMap = AttributeHandler.getItemStats(offHandItemStack, dualWieldEfficiency);
            if (itemStatMap.containsKey(StrifeAttribute.LEVEL_REQUIREMENT) && getPlayer().getLevel() < itemStatMap
                .get(StrifeAttribute.LEVEL_REQUIREMENT) / dualWieldEfficiency) {
                MessageUtils.sendMessage(getPlayer(), LVL_REQ_OFF_WEAPON);
            } else {
                attributeDoubleMap = AttributeHandler.combineMaps(attributeDoubleMap, itemStatMap);
            }
        }
        cache.setAttributeWeaponCache(attributeDoubleMap);
        System.out.println("FINAL WEAPON STAT MAP: " + attributeDoubleMap);
        return attributeDoubleMap;
    }

    public Map<StrifeAttribute, Double> getAttributeValues(boolean refresh) {
        Map<StrifeAttribute, Double> attributeDoubleMap = new HashMap<>();
        if (getPlayer() == null || getPlayer().getEquipment() == null) {
            return attributeDoubleMap;
        }
        if (refresh) {
            cache.clear();
            attributeDoubleMap = AttributeHandler.combineMaps(
                getEntityAttributeValues(),
                getLevelpointAttributeValues(),
                getArmorAttributeValues(),
                getWeaponAttributeValues()
            );
            cache.recombine();
        } else {
            attributeDoubleMap = cache.getCache();
        }
        return attributeDoubleMap;
    }

    public void setCurrentBaseStats(Map<StrifeAttribute, Double> baseStatMap) {
        currentBaseStats = baseStatMap;
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
        return 10 + (getHighestReachedLevel() / 5) * 2;
    }

    public int getHighestReachedLevel() {
        return highestReachedLevel;
    }

    public void setHighestReachedLevel(int highestReachedLevel) {
        this.highestReachedLevel = highestReachedLevel;
    }

    public ChampionCache getCache() {
        return cache;
    }

    public void setCache(ChampionCache cache) {
        this.cache = cache;
    }
}