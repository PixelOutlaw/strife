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
    private Map<StrifeAttribute, Double> attributeStatCache;
    private Map<StrifeAttribute, Double> attributeArmorCache;
    private Map<StrifeAttribute, Double> attributeWeaponCache;
    private Map<StrifeAttribute, Double> attributeCache;
    private int unusedStatPoints;
    private int highestReachedLevel;

    public Champion(UUID uniqueId) {
        this.uniqueId = uniqueId;
        this.levelMap = new HashMap<>();
        this.attributeStatCache = new HashMap<>();
        this.attributeArmorCache = new HashMap<>();
        this.attributeWeaponCache = new HashMap<>();
        this.attributeCache = new HashMap<>();
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

    public Map<StrifeAttribute, Double> getStatAttributeValues() {
        attributeStatCache.clear();
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
        attributeStatCache.putAll(attributeDoubleMap);
        return attributeDoubleMap;
    }

    public Map<StrifeAttribute, Double> getArmorAttributeValues() {
        attributeArmorCache.clear();
        Map<StrifeAttribute, Double> attributeDoubleMap = new HashMap<>();
        for (ItemStack itemStack : getPlayer().getEquipment().getArmorContents()) {
            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }
            for (StrifeAttribute attr : StrifeAttribute.values()) {
                double val = AttributeHandler.getValue(itemStack, attr);
                double curVal = attributeDoubleMap.containsKey(attr) ? attributeDoubleMap.get(attr) : 0;
                attributeDoubleMap.put(attr,
                        attr.getCap() > 0D ? Math.min(attr.getCap(), val + curVal) : val + curVal);
            }
        }
        attributeArmorCache.putAll(attributeDoubleMap);
        return attributeDoubleMap;
    }

    public Map<StrifeAttribute, Double> getWeaponAttributeValues() {
        attributeWeaponCache.clear();
        Map<StrifeAttribute, Double> attributeDoubleMap = new HashMap<>();
        ItemStack itemStack = getPlayer().getEquipment().getItemInHand();
        if (itemStack == null || itemStack.getType() == Material.AIR || isArmor(itemStack.getType())) {
            attributeWeaponCache.putAll(attributeDoubleMap);
            return attributeWeaponCache;
        }
        for (StrifeAttribute attr : StrifeAttribute.values()) {
            double val = AttributeHandler.getValue(itemStack, attr);
            double curVal = attributeDoubleMap.containsKey(attr) ? attributeDoubleMap.get(attr) : 0D;
            attributeDoubleMap.put(attr,
                    attr.getCap() > 0D ? Math.min(attr.getCap(), val + curVal) : val + curVal);
        }
        attributeWeaponCache.putAll(attributeDoubleMap);
        return attributeDoubleMap;
    }

    public Map<StrifeAttribute, Double> getAttributeValues(boolean refresh) {
        attributeCache.clear();
        Map<StrifeAttribute, Double> attributeDoubleMap = new HashMap<>();
        attributeDoubleMap.putAll(AttributeHandler.combineMaps(
                refresh ? getStatAttributeValues() : getAttributeStatCache(),
                refresh ? getArmorAttributeValues() : getAttributeArmorCache(),
                refresh ? getWeaponAttributeValues() : getAttributeWeaponCache()
        ));
        attributeCache.putAll(attributeDoubleMap);
        return attributeDoubleMap;
    }

    public Map<StrifeAttribute, Double> recombineCache() {
        attributeCache.clear();
        Map<StrifeAttribute, Double> attributeDoubleMap = new HashMap<>();
        attributeDoubleMap.putAll(AttributeHandler.combineMaps(
                getAttributeStatCache(),
                getAttributeArmorCache(),
                getAttributeWeaponCache()
        ));
        attributeCache.putAll(attributeDoubleMap);
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
        return 10 + (getHighestReachedLevel() / 5) * 2;
    }

    public int getHighestReachedLevel() {
        return highestReachedLevel;
    }

    public void setHighestReachedLevel(int highestReachedLevel) {
        this.highestReachedLevel = highestReachedLevel;
    }

    public Map<StrifeAttribute, Double> getAttributeStatCache() {
        return new HashMap<>(attributeStatCache);
    }

    public Map<StrifeAttribute, Double> getAttributeArmorCache() {
        return new HashMap<>(attributeArmorCache);
    }

    public Map<StrifeAttribute, Double> getAttributeWeaponCache() {
        return new HashMap<>(attributeWeaponCache);
    }

    public Map<StrifeAttribute, Double> getAttributeCache() {
        return new HashMap<>(attributeCache);
    }

    public double getStatCacheAttribute(StrifeAttribute attribute, double def) {
        return attributeStatCache.containsKey(attribute) ? attributeStatCache.get(attribute) : def;
    }

    public double getArmorCacheAttribute(StrifeAttribute attribute, double def) {
        return attributeArmorCache.containsKey(attribute) ? attributeArmorCache.get(attribute) : def;
    }

    public double getWeaponCacheAttribute(StrifeAttribute attribute, double def) {
        return attributeWeaponCache.containsKey(attribute) ? attributeWeaponCache.get(attribute) : def;
    }

    public double getCacheAttribute(StrifeAttribute attribute, double def) {
        return attributeCache.containsKey(attribute) ? attributeCache.get(attribute) : def;
    }

    public double getCacheAttribute(StrifeAttribute attribute) {
        return attributeCache.containsKey(attribute) ? attributeCache.get(attribute) : attribute.getBaseValue();
    }

    public void setStatCacheAttribue(StrifeAttribute attribute, double value) {
        attributeStatCache.put(attribute, value);
    }

    public void setArmorCacheAttribue(StrifeAttribute attribute, double value) {
        attributeArmorCache.put(attribute, value);
    }

    public void setWeaponCacheAttribue(StrifeAttribute attribute, double value) {
        attributeWeaponCache.put(attribute, value);
    }

    public void setCacheAttribute(StrifeAttribute attribute, double value) {
        attributeCache.put(attribute, value);
    }

    private boolean isArmor(Material material) {
        String name = material.name();
        return name.contains("HELMET") || name.contains("CHESTPLATE") || name.contains("LEGGINGS") ||
                name.contains("BOOTS");
    }
}
