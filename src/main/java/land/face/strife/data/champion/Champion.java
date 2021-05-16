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
package land.face.strife.data.champion;

import com.tealcube.minecraft.bukkit.shade.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.CombatDetailsContainer;
import land.face.strife.data.LoreAbility;
import land.face.strife.managers.LoreAbilityManager.TriggerType;
import land.face.strife.managers.StatUpdateManager;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import org.bukkit.entity.Player;

public class Champion {

  private final PlayerEquipmentCache equipmentCache;
  private final ChampionSaveData saveData;
  private final CombatDetailsContainer detailsContainer = new CombatDetailsContainer();

  private final Map<StrifeStat, Float> baseStats;
  private final Map<StrifeStat, Float> levelPointStats;
  private final Map<StrifeStat, Float> pathStats;
  private final Set<StrifeTrait> pathTraits;

  private final Map<StrifeStat, Float> combinedStatMap;

  private Player player;

  private static final Map<LifeSkillType, StrifeStat> SKILL_TO_ATTR_MAP = ImmutableMap.<LifeSkillType, StrifeStat>builder()
      .put(LifeSkillType.CRAFTING, StrifeStat.CRAFT_SKILL)
      .put(LifeSkillType.ENCHANTING, StrifeStat.ENCHANT_SKILL)
      .put(LifeSkillType.SNEAK, StrifeStat.SNEAK_SKILL)
      .build();

  public Champion(Player player, ChampionSaveData saveData) {
    this.player = player;
    this.saveData = saveData;

    baseStats = new HashMap<>();
    levelPointStats = new HashMap<>();
    combinedStatMap = new HashMap<>();
    pathStats = new HashMap<>();
    equipmentCache = new PlayerEquipmentCache();
    pathTraits = new HashSet<>();
  }

  public Map<StrifeStat, Float> getCombinedCache() {
    return new HashMap<>(combinedStatMap);
  }

  private void clearCombinedCache() {
    combinedStatMap.clear();
  }

  public void recombineCache() {
    clearCombinedCache();
    combinedStatMap.putAll(StatUpdateManager.combineMaps(
        baseStats,
        levelPointStats,
        pathStats,
        equipmentCache.getCombinedStats(),
        StrifePlugin.getInstance().getBoostManager().getStats()
    ));
  }

  public void setPathStats(Map<StrifeStat, Float> map) {
    pathStats.clear();
    pathStats.putAll(map);
  }

  public void setPathTraits(Set<StrifeTrait> traits) {
    pathTraits.clear();
    pathTraits.addAll(traits);
  }

  public void setBaseStats(Map<StrifeStat, Float> map) {
    baseStats.clear();
    baseStats.putAll(map);
  }

  public void setLevelPointStats(Map<StrifeStat, Float> map) {
    levelPointStats.clear();
    levelPointStats.putAll(map);
  }

  public ChampionSaveData getSaveData() {
    return saveData;
  }

  public int getAttributeLevel(StrifeAttribute attr) {
    return saveData.getLevelMap().getOrDefault(attr, 0);
  }

  public int getPendingLevel(StrifeAttribute stat) {
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
    return saveData.getSkillLevel(type) +
        combinedStatMap.getOrDefault(SKILL_TO_ATTR_MAP.get(type), 0f);
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

  public void setLevel(StrifeAttribute stat, int level) {
    saveData.setLevel(stat, level);
  }

  public void setPendingLevel(StrifeAttribute stat, int level) {
    saveData.getPendingLevelMap().put(stat, level);
  }

  public Map<StrifeAttribute, Integer> getLevelMap() {
    return saveData.getLevelMap();
  }

  public Map<StrifeAttribute, Integer> getPendingLevelMap() {
    return saveData.getPendingLevelMap();
  }

  public Player getPlayer() {
    return player;
  }

  public void setPlayer(Player player) {
    this.player = player;
  }

  public CombatDetailsContainer getDetailsContainer() {
    return detailsContainer;
  }

  public PlayerEquipmentCache getEquipmentCache() {
    return equipmentCache;
  }

  public Map<StrifeStat, Float> getPathStats() {
    return pathStats;
  }

  public Set<StrifeTrait> getPathTraits() {
    return pathTraits;
  }

  public Map<TriggerType, Set<LoreAbility>> getLoreAbilities() {
    return equipmentCache.getCombinedAbilities();
  }

  public Set<StrifeTrait> getTraits() {
    Set<StrifeTrait> traits = new HashSet<>();
    traits.addAll(equipmentCache.getCombinedTraits());
    traits.addAll(pathTraits);
    return traits;
  }

  public boolean hasTrait (StrifeTrait trait) {
    return equipmentCache.getCombinedTraits().contains(trait) || pathTraits.contains(trait);
  }

  public int getUnchosenPaths() {
    return getHighestReachedLevel() / 10 - saveData.getPathMap().size();
  }
}