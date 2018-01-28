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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Champion {

    private final Map<StrifeAttribute, Double> attributeBase;
    private final Map<StrifeAttribute, Double> attributeLevelPoint;
    private final Map<StrifeAttribute, Double> attributeArmorCache;
    private final Map<StrifeAttribute, Double> attributeWeaponCache;
    private final Map<StrifeAttribute, Double> combinedAttributeCache;

    private ChampionSaveData saveData;

    public Champion(ChampionSaveData saveData) {
        this.attributeBase = new HashMap<>();
        this.attributeLevelPoint = new HashMap<>();
        this.attributeArmorCache = new HashMap<>();
        this.attributeWeaponCache = new HashMap<>();
        this.combinedAttributeCache = new HashMap<>();
        this.saveData = saveData;
    }

    public Map<StrifeAttribute, Double> getCombinedCache() {
        return new HashMap<>(combinedAttributeCache);
    }

    private void clearCombinedCache() {
        combinedAttributeCache.clear();
    }

    private void clearAttributeCaches() {
        attributeBase.clear();
        attributeLevelPoint.clear();
        attributeArmorCache.clear();
        attributeWeaponCache.clear();
    }

    public void recombineCache() {
        clearCombinedCache();
        combinedAttributeCache.putAll(AttributeHandler.combineMaps(
            attributeBase,
            attributeLevelPoint,
            attributeWeaponCache,
            attributeArmorCache
        ));
    }

    public void setAttributeBaseCache(Map<StrifeAttribute, Double> map) {
        attributeBase.clear();
        attributeBase.putAll(map);
    }

    public void setAttributeLevelPointCache(Map<StrifeAttribute, Double> map) {
        attributeLevelPoint.clear();
        attributeLevelPoint.putAll(map);
    }

    public void setAttributeArmorCache(Map<StrifeAttribute, Double> map) {
        attributeArmorCache.clear();
        attributeArmorCache.putAll(map);
    }

    public void setAttributeWeaponCache(Map<StrifeAttribute, Double> map) {
        attributeWeaponCache.clear();
        attributeWeaponCache.putAll(map);
    }

    public ChampionSaveData getSaveData() {
        return saveData;
    }

    public void setSaveData(ChampionSaveData data) {
        this.saveData = data;
    }

    public int getLevel(StrifeStat stat) {
        return saveData.getLevel(stat);
    }

    public int getUnusedStatPoints() {
        return saveData.getUnusedStatPoints();
    }

    public void setUnusedStatPoints(int unusedStatPoints) {
        saveData.setUnusedStatPoints(unusedStatPoints);
    }

    public int getHighestReachedLevel() {
        return saveData.getHighestReachedLevel();
    }

    public void setHighestReachedLevel(int highestReachedLevel) {
        saveData.setHighestReachedLevel(highestReachedLevel);
    }

    public UUID getUniqueId() {
        return saveData.getUniqueId();
    }

    public void setLevel(StrifeStat stat, int level) {
        saveData.setLevel(stat, level);
    }

    public Map<StrifeStat, Integer> getLevelMap() {
        return saveData.getLevelMap();
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(getUniqueId());
    }
}