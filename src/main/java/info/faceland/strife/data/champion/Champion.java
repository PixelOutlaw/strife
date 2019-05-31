/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package info.faceland.strife.data.champion;

import info.faceland.strife.attributes.StrifeTrait;
import info.faceland.strife.data.LoreAbility;
import info.faceland.strife.managers.AttributeUpdateManager;
import info.faceland.strife.attributes.StrifeAttribute;

import info.faceland.strife.managers.LoreAbilityManager.TriggerType;
import info.faceland.strife.stats.StrifeStat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Player;

public class Champion {

  private final Map<StrifeAttribute, Double> attributeBase;
  private final Map<StrifeAttribute, Double> attributeLevelPoint;
  private final Map<StrifeAttribute, Double> combinedAttributeCache;
  private final ChampionSaveData saveData;
  private final PlayerEquipmentCache equipmentCache;

  private Player player;

  public Champion(Player player, ChampionSaveData saveData) {
    this.attributeBase = new HashMap<>();
    this.attributeLevelPoint = new HashMap<>();
    this.combinedAttributeCache = new HashMap<>();
    this.equipmentCache = new PlayerEquipmentCache();
    this.player = player;
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
  }

  public void recombineCache() {
    clearCombinedCache();
    combinedAttributeCache.putAll(AttributeUpdateManager.combineMaps(
        attributeBase,
        attributeLevelPoint,
        equipmentCache.getCombinedStats()
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

  public ChampionSaveData getSaveData() {
    return saveData;
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

  public int getCraftingLevel() {
    return saveData.getCraftingLevel();
  }

  public float getCraftingExp() {
    return saveData.getCraftingExp();
  }

  public int getCraftSkill(boolean updateEquipment) {
    if (updateEquipment) {
      recombineCache();
    }
    return getCraftingLevel() + combinedAttributeCache
        .getOrDefault(StrifeAttribute.CRAFT_SKILL, 0D).intValue();
  }

  public int getEnchantLevel() {
    return saveData.getEnchantLevel();
  }

  public float getEnchantExp() {
    return saveData.getEnchantExp();
  }

  public int getEnchantSkill(boolean updateEquipment) {
    if (updateEquipment) {
      recombineCache();
    }
    return getEnchantLevel() + combinedAttributeCache
        .getOrDefault(StrifeAttribute.ENCHANT_SKILL, 0D).intValue();
  }

  public int getFishingLevel() {
    return saveData.getFishingLevel();
  }

  public float getFishingExp() {
    return saveData.getFishingExp();
  }

  public int getFishSkill(boolean updateEquipment) {
    if (updateEquipment) {
      recombineCache();
    }
    return getFishingLevel() + combinedAttributeCache
        .getOrDefault(StrifeAttribute.FISH_SKILL, 0D).intValue();
  }

  public int getMiningLevel() {
    return saveData.getMiningLevel();
  }

  public float getMiningExp() {
    return saveData.getMiningExp();
  }

  public int getMineSkill(boolean updateEquipment) {
    if (updateEquipment) {
      recombineCache();
    }
    return getMiningLevel() + combinedAttributeCache
        .getOrDefault(StrifeAttribute.MINE_SKILL, 0D).intValue();
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
    return player;
  }

  public void setPlayer(Player player) {
    this.player = player;
  }

  public PlayerEquipmentCache getEquipmentCache() {
    return equipmentCache;
  }

  public Map<TriggerType, Set<LoreAbility>> getLoreAbilities() {
    return equipmentCache.getCombinedAbilities();
  }

  public Set<StrifeTrait> getTraits() {
    return equipmentCache.getCombinedTraits();
  }
}