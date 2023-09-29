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

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import land.face.strife.StrifePlugin;
import land.face.strife.data.CombatDetailsContainer;
import land.face.strife.data.ability.Ability;
import land.face.strife.data.ability.Ability.TargetType;
import land.face.strife.data.champion.ChampionSaveData.SelectedGod;
import land.face.strife.managers.StatUpdateManager;
import land.face.strife.stats.AbilitySlot;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

public class Champion {

  @Getter
  private final ChampionSaveData saveData;
  @Getter
  private final CombatDetailsContainer detailsContainer = new CombatDetailsContainer();

  private final Map<StrifeStat, Float> baseStats;
  private final Map<StrifeStat, Float> levelPointStats;
  @Getter
  private final Map<StrifeStat, Float> pathStats;
  @Getter
  private final Set<StrifeTrait> pathTraits;

  private final Map<AbilitySlot, Ability> abilities = new HashMap<>();

  private final Map<StrifeStat, Float> combinedStatMap;
  @Getter
  private String attributeHeatmap = "";

  @Getter
  private final List<LifeSkillType> recentSkills = new ArrayList<>();
  @Getter @Setter
  private Player player;
  @Getter @Setter
  private long lastChanged = System.currentTimeMillis();

  public Champion(Player player, ChampionSaveData saveData) {
    this.player = player;
    this.saveData = saveData;

    baseStats = new HashMap<>();
    levelPointStats = new HashMap<>();
    combinedStatMap = new HashMap<>();
    pathStats = new HashMap<>();
    pathTraits = new HashSet<>();
    buildAttributeHeatmap();
  }

  public Map<StrifeStat, Float> getCombinedCache() {
    return new HashMap<>(combinedStatMap);
  }

  private void clearCombinedCache() {
    combinedStatMap.clear();
  }

  public void recombineCache(StrifePlugin plugin) {
    clearCombinedCache();
    abilities.put(AbilitySlot.SLOT_A, plugin.getAbilityManager().getAbility(saveData.getAbility(AbilitySlot.SLOT_A)));
    abilities.put(AbilitySlot.SLOT_B, plugin.getAbilityManager().getAbility(saveData.getAbility(AbilitySlot.SLOT_B)));
    abilities.put(AbilitySlot.SLOT_C, plugin.getAbilityManager().getAbility(saveData.getAbility(AbilitySlot.SLOT_C)));
    abilities.put(AbilitySlot.SLOT_D, plugin.getAbilityManager().getAbility(saveData.getAbility(AbilitySlot.SLOT_D)));
    combinedStatMap.putAll(StatUpdateManager.combineMaps(
        baseStats,
        levelPointStats,
        pathStats,
        getAbilityStats(AbilitySlot.SLOT_A),
        getAbilityStats(AbilitySlot.SLOT_B),
        getAbilityStats(AbilitySlot.SLOT_C),
        getAbilityStats(AbilitySlot.SLOT_D),
        StrifePlugin.getInstance().getBoostManager().getStats()
    ));
    lastChanged = System.currentTimeMillis();
  }

  public Ability getAbility(AbilitySlot slot) {
    return abilities.get(slot);
  }

  public boolean hasAbility(Ability ability) {
    return abilities.containsValue(ability);
  }

  public void setAbility(StrifePlugin plugin, AbilitySlot slot, Ability ability) {
    abilities.put(slot, ability);
    saveData.setAbility(slot, ability != null ? ability.getId() : null);
    recombineCache(plugin);
  }

  public boolean hasSoulSight() {
    for (Ability ability : abilities.values()) {
      if (ability == null) {
        continue;
      }
      if (ability.getTargetType() == TargetType.NEAREST_SOUL) {
        return true;
      }
    }
    return false;
  }

  public Map<StrifeStat, Float> getAbilityStats(AbilitySlot abilitySlot) {
    Ability ability = abilities.get(abilitySlot);
    if (ability == null) {
      return new HashMap<>();
    }
    return StrifePlugin.getInstance().getAbilityManager().getApplicableAbilityPassiveStats(player, ability);
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

  public int getAttributeLevel(StrifeAttribute attr) {
    return saveData.getLevelMap().getOrDefault(attr, 0);
  }

  public int getPendingLevel(StrifeAttribute stat) {
    return saveData.getPendingStats().getOrDefault(stat, 0);
  }

  public void buildAttributeHeatmap() {
    int red = 0;
    int yellow = 0;
    int green = 0;
    int blue = 0;
    for (StrifeAttribute attribute : getLevelMap().keySet()) {
      switch (attribute.getKey()) {
        case "str" -> red = getLevelMap().get(attribute);
        case "con" -> yellow = getLevelMap().get(attribute);
        case "dex" -> green = getLevelMap().get(attribute);
        case "int" -> blue = getLevelMap().get(attribute);
      }
    }
    float total = Math.max(1, red + yellow + green + blue);
    int segments =  22 + player.getName().length();

    int redSegments = (int) Math.ceil((float) segments * (float) red / total);
    total -= red;
    segments -= redSegments;

    int yellowSegments = (int) Math.floor((float) segments * (float) yellow / total);
    total -= yellow;
    segments -= yellowSegments;

    int greenSegments = (int) Math.ceil((float) segments * (float) green / total);
    segments -= greenSegments;

    int blueSegments = segments;

    attributeHeatmap =
        FaceColor.RED + IntStream.range(0, redSegments).mapToObj(i -> "▌").collect(Collectors.joining()) +
            FaceColor.YELLOW + IntStream.range(0, yellowSegments).mapToObj(i -> "▌").collect(Collectors.joining()) +
            FaceColor.GREEN + IntStream.range(0, greenSegments).mapToObj(i -> "▌").collect(Collectors.joining()) +
            FaceColor.BLUE + IntStream.range(0, blueSegments).mapToObj(i -> "▌").collect(Collectors.joining());
  }

  public int getLifeSkillLevel(LifeSkillType type) {
    return saveData.getSkillLevel(type);
  }

  public float getLifeSkillExp(LifeSkillType type) {
    return saveData.getSkillExp(type);
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
    saveData.getPendingStats().put(stat, level);
  }

  public Map<StrifeAttribute, Integer> getLevelMap() {
    return saveData.getLevelMap();
  }

  public Map<StrifeAttribute, Integer> getPendingLevelMap() {
    return saveData.getPendingStats();
  }

  public void setGod(SelectedGod god) {
    saveData.setSelectedGod(god);
  }

  public void addGodXp(SelectedGod god, int amount) {
    if (saveData.getSelectedGod() == SelectedGod.NONE) {
      return;
    }
    saveData.getGodXp().put(god, saveData.getGodXp().get(god) + amount);
    // TODO: check levelup
  }

  public void addGodXp(int amount) {
    addGodXp(saveData.getSelectedGod(), amount);
  }

  public int getUnchosenPaths() {
    return getHighestReachedLevel() / 10 - saveData.getPathMap().size();
  }
}