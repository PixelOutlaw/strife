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
package land.face.strife.managers;

import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.StrifeAttribute;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.StatUtil;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;

public class StrifeAttributeManager {

  private static Map<String, StrifeAttribute> attributeMap;
  private static final String upgradeAvailable = StringExtensionsKt.chatColorize(
      "&a&lCLICK TO UPGRADE!");
  private static final String pointCapReached = StringExtensionsKt.chatColorize(
      "&f&lMaxed Out!");
  private static final String noUnspentPoints = StringExtensionsKt.chatColorize(
      "&f&lNo Unspent Points");

  public StrifeAttributeManager() {
    attributeMap = new LinkedHashMap<>();
  }

  public StrifeAttribute getAttribute(String name) {
    return attributeMap.getOrDefault(name, null);
  }

  public List<StrifeAttribute> getAttributes() {
    return new ArrayList<>(attributeMap.values());
  }

  public int getPendingStatCap(StrifeAttribute attr, Champion champion) {

    int statCap = attr.getMaxCap();
    if (attr.getLevelsToRaiseCap() > 0) {
      double levelCap = attr.getStartCap() + (double) champion.getPlayer().getLevel() / attr
          .getLevelsToRaiseCap();
      statCap = Math.max(attr.getStartCap(), (int) Math.floor(levelCap));
    }

    for (Entry<String, Integer> baseEntry : attr.getBaseStatRequirements().entrySet()) {
      StrifeAttribute requirementStat = getAttribute(baseEntry.getKey());
      int requirementStatValue = champion.getPendingLevel(requirementStat);
      int unlockRequirement = baseEntry.getValue();
      int statIncrement = attr.getStatIncreaseIncrements().get(baseEntry.getKey());

      if (requirementStatValue < unlockRequirement) {
        return 0;
      }

      int newStatCap = attr.getStartCap();
      newStatCap += (requirementStatValue - unlockRequirement) / statIncrement;
      statCap = Math.min(statCap, newStatCap);
    }
    return Math.min(statCap, attr.getMaxCap());
  }

  public List<String> generateRequirementString(StrifeAttribute attr, Champion champion, int cap) {
    List<String> requirementList = new ArrayList<>();

    int levelRequirement = getLevelRequirement(attr, champion);
    int allocatedPoints = champion.getPendingLevel(attr);
    if (levelRequirement > champion.getPlayer().getLevel() && allocatedPoints < attr.getMaxCap() &&
        allocatedPoints == cap) {
      requirementList.add(increaseString("Level", levelRequirement));
    }
    for (Entry<String, Integer> entry : attr.getBaseStatRequirements().entrySet()) {
      int statRequirement = getStatRequirement(attr, entry.getKey(), champion);
      StrifeAttribute requirementStat = getAttribute(entry.getKey());
      if (allocatedPoints < attr.getMaxCap() && allocatedPoints == cap &&
          champion.getPendingLevel(requirementStat) < statRequirement) {
        requirementList.add(increaseString(requirementStat.getName(), statRequirement));
      }
    }
    if (requirementList.isEmpty()) {
      if (allocatedPoints == attr.getMaxCap()) {
        requirementList.add(pointCapReached);
      } else if (champion.getUnusedStatPoints() == 0) {
        requirementList.add(noUnspentPoints);
      } else {
        requirementList.add(upgradeAvailable);
      }
    }
    return requirementList;
  }

  private String increaseString(String name, int value) {
    return StringExtensionsKt.chatColorize("&f&lRequires: " + name + " " + value);
  }

  private int getLevelRequirement(StrifeAttribute attr, Champion champion) {
    if (attr.getLevelsToRaiseCap() <= 0) {
      return 0;
    }
    int requirementIncrease = champion.getPlayer().getLevel() / attr.getLevelsToRaiseCap();
    return attr.getLevelsToRaiseCap() + requirementIncrease * attr.getLevelsToRaiseCap();
  }

  private int getStatRequirement(StrifeAttribute attr, String requiredStatName, Champion champion) {
    StrifeAttribute requirementStat = getAttribute(requiredStatName);
    int baseRequirement = attr.getBaseStatRequirements().get(requiredStatName);
    int statIncrement = attr.getStatIncreaseIncrements().get(requiredStatName);
    if (baseRequirement > champion.getPendingLevel(requirementStat)) {
      return attr.getBaseStatRequirements().get(requiredStatName);
    }
    if (baseRequirement + statIncrement * champion.getPendingLevel(attr) <= champion
        .getPendingLevel(requirementStat)) {
      return -1;
    }
    int nextRank = (champion.getPendingLevel(requirementStat) - baseRequirement) / statIncrement;
    return attr.getBaseStatRequirements().get(requiredStatName) + (nextRank + 1) * statIncrement;
  }

  public void loadStat(String key, ConfigurationSection cs) {
    StrifeAttribute stat = new StrifeAttribute(key);
    stat.setName(PaletteUtil.color(cs.getString("name")));
    stat.setDescription(PaletteUtil.color(cs.getStringList("description")));
    stat.setCustomData(cs.getInt("custom-data", -1));
    stat.setClickSound(Sound.valueOf(cs.getString("sounds.click-sound", "ENTITY_CAT_HISS")));
    stat.setClickPitch((float) cs.getDouble("sounds.click-pitch", 1));
    stat.setLevelSound(Sound.valueOf(cs.getString("sounds.level-sound", "ENTITY_CAT_HISS")));
    stat.setLevelPitch((float) cs.getDouble("sounds.level-pitch", 1));
    stat.setSlot(cs.getInt("slot"));
    stat.setStartCap(cs.getInt("starting-cap", 0));
    stat.setMaxCap(cs.getInt("maximum-cap", 100));
    stat.setLevelsToRaiseCap(cs.getInt("levels-to-raise-cap", -1));
    Map<String, Integer> baseStatRequirements = new HashMap<>();
    if (cs.isConfigurationSection("base-attribute-requirements")) {
      ConfigurationSection reqs = cs.getConfigurationSection("base-attribute-requirements");
      for (String k : reqs.getKeys(false)) {
        baseStatRequirements.put(k, reqs.getInt(k));
      }
    }
    Map<String, Integer> raiseStatCapAttributes = new HashMap<>();
    if (cs.isConfigurationSection("attributes-to-raise-cap")) {
      ConfigurationSection raiseReqs = cs.getConfigurationSection("attributes-to-raise-cap");
      for (String k : raiseReqs.getKeys(false)) {
        raiseStatCapAttributes.put(k, raiseReqs.getInt(k));
      }
    }
    Map<StrifeStat, Float> attributeMap = new HashMap<>();
    if (cs.isConfigurationSection("stats")) {
      ConfigurationSection attrCS = cs.getConfigurationSection("stats");
      attributeMap.putAll(StatUtil.getStatMapFromSection(attrCS));
    }
    stat.setStatIncreaseIncrements(raiseStatCapAttributes);
    stat.setBaseStatRequirements(baseStatRequirements);
    stat.setAttributeMap(attributeMap);
    StrifeAttributeManager.attributeMap.put(stat.getKey(), stat);
  }
}
