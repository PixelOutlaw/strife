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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class Champion {

    private final Map<StrifeAttribute, Double> attributeBase;
    private final Map<StrifeAttribute, Double> attributeLevelPoint;
    private final Map<StrifeAttribute, Double> attributeArmorCache;
    private final Map<StrifeAttribute, Double> attributeWeaponCache;
    private final Map<StrifeAttribute, Double> combinedAttributeCache;

    private int mainHandHash;
    private int offHandHash;
    private int helmetHash;
    private int chestHash;
    private int legsHash;
    private int bootsHash;

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

    public Map<StrifeAttribute, Double> getAttributeBaseCache() {
        return attributeBase;
    }

    public Map<StrifeAttribute, Double> getAttributeLevelPointCache() {
        return attributeLevelPoint;
    }

    public Map<StrifeAttribute, Double> getAttributeArmorCache() {
        return attributeArmorCache;
    }

    public Map<StrifeAttribute, Double> getAttributeWeaponCache() {
        return attributeWeaponCache;
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

    public void setBonusLevels(int bonusLevels) {
        saveData.setBonusLevels(bonusLevels);
    }

    public int getBonusLevels() {
        return saveData.getBonusLevels();
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

    public void updateHashedEquipment() {
        PlayerInventory invy = getPlayer().getInventory();
        mainHandHash = invy.getItemInMainHand() == null ? -1 : invy.getItemInMainHand().hashCode();
        offHandHash = invy.getItemInOffHand() == null ? -1 : invy.getItemInOffHand().hashCode();
        helmetHash = invy.getHelmet() == null ? -1 : invy.getHelmet().hashCode();
        chestHash = invy.getChestplate() == null ? -1 : invy.getChestplate().hashCode();
        legsHash = invy.getLeggings() == null ? -1 : invy.getLeggings().hashCode();
        bootsHash = invy.getBoots() == null ? -1 : invy.getBoots().hashCode();
    }

    public boolean isEquipmentHashMatching() {
        PlayerInventory invy = getPlayer().getInventory();
        if (!itemStackHashMatch(invy.getItemInMainHand(), mainHandHash)) {
            return false;
        }
        if (!itemStackHashMatch(invy.getItemInOffHand(), offHandHash)) {
            return false;
        }
        if (!itemStackHashMatch(invy.getHelmet(), helmetHash)) {
            return false;
        }
        if (!itemStackHashMatch(invy.getChestplate(), chestHash)) {
            return false;
        }
        if (!itemStackHashMatch(invy.getLeggings(), legsHash)) {
            return false;
        }
        if (!itemStackHashMatch(invy.getBoots(), bootsHash)) {
            return false;
        }
        return true;
    }

    private boolean itemStackHashMatch(ItemStack stack, int hash) {
        if (stack == null) {
            return hash == -1;
        }
        return stack.hashCode() == hash;
  }
}