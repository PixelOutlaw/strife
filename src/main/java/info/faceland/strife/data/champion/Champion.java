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

import com.tealcube.minecraft.bukkit.shade.google.common.collect.ImmutableMap;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.attributes.StrifeTrait;
import info.faceland.strife.data.LoreAbility;
import info.faceland.strife.data.champion.ChampionSaveData.LifeSkillType;
import info.faceland.strife.managers.AttributeUpdateManager;
import info.faceland.strife.managers.LoreAbilityManager.TriggerType;
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

  private static final Map<LifeSkillType, StrifeAttribute> SKILL_TO_ATTR_MAP = ImmutableMap.<LifeSkillType, StrifeAttribute>builder()
      .put(LifeSkillType.CRAFTING, StrifeAttribute.CRAFT_SKILL)
      .put(LifeSkillType.ENCHANTING, StrifeAttribute.ENCHANT_SKILL)
      .put(LifeSkillType.FISHING, StrifeAttribute.FISH_SKILL)
      .put(LifeSkillType.MINING, StrifeAttribute.MINE_SKILL)
      .put(LifeSkillType.SNEAK, StrifeAttribute.SNEAK_SKILL)
      .build();

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
    return saveData.getLevelMap().getOrDefault(stat, 0);
  }

  public int getPendingLevel(StrifeStat stat) {
    return saveData.getPendingLevelMap().getOrDefault(stat, 0);
  }

  public void setBonusLevels(int bonusLevels) {
    saveData.setBonusLevels(bonusLevels);
  }

  public int getBonusLevels() {
    return saveData.getBonusLevels();
  }

  public int getLifeSkillLevel(LifeSkillType type) {
    return saveData.getSkillLevel(type);
  }

  public float getLifeSkillExp(LifeSkillType type) {
    return saveData.getSkillExp(type);
  }

  public float getEffectiveLifeSkillLevel(LifeSkillType type, boolean updateEquipment) {
    if (updateEquipment) {
      recombineCache();
    }
    return saveData.getSkillLevel(type) + combinedAttributeCache
        .getOrDefault(SKILL_TO_ATTR_MAP.get(type), 0D).floatValue();
  }

  public int getUnusedStatPoints() {
    return saveData.getUnusedStatPoints();
  }

  public void setUnusedStatPoints(int unusedStatPoints) {
    saveData.setUnusedStatPoints(unusedStatPoints);
  }

  public int getPendingUnusedStatPoints() {
    return saveData.getPendingUnusedStatPoints();
  }

  public void setPendingUnusedStatPoints(int unusedStatPoints) {
    saveData.setPendingUnusedStatPoints(unusedStatPoints);
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

  public void setPendingLevel(StrifeStat stat, int level) {
    saveData.getPendingLevelMap().put(stat, level);
  }

  public Map<StrifeStat, Integer> getLevelMap() {
    return saveData.getLevelMap();
  }

  public Map<StrifeStat, Integer> getPendingLevelMap() {
    return saveData.getPendingLevelMap();
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

  public boolean hasTrait (StrifeTrait trait) {
    return equipmentCache.getCombinedTraits().contains(trait);
  }
}