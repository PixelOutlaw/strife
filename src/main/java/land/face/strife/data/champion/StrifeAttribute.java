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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import land.face.strife.stats.StrifeStat;
import org.bukkit.Sound;

public class StrifeAttribute {

  private final String key;
  private String name;
  private List<String> description;
  private int customData;
  private Sound clickSound;
  private float clickPitch;
  private Sound levelSound;
  private float levelPitch;
  private int slot;
  private int startCap;
  private int maxCap;
  private int levelsToRaiseCap;
  private Map<String, Integer> baseStatRequirements;
  private Map<String, Integer> statIncreaseIncrements;
  private Map<StrifeStat, Float> attributeMap;

  public int getLevelsToRaiseCap() {
    return levelsToRaiseCap;
  }

  public void setLevelsToRaiseCap(int levelsToRaiseCap) {
    this.levelsToRaiseCap = levelsToRaiseCap;
  }

  public int getStartCap() {
    return startCap;
  }

  public void setStartCap(int startCap) {
    this.startCap = startCap;
  }

  public int getMaxCap() {
    return maxCap;
  }

  public void setMaxCap(int maxCap) {
    this.maxCap = maxCap;
  }

  public Map<String, Integer> getBaseStatRequirements() {
    return baseStatRequirements;
  }

  public void setBaseStatRequirements(Map<String, Integer> baseStatRequirements) {
    this.baseStatRequirements = baseStatRequirements;
  }

  public Map<String, Integer> getStatIncreaseIncrements() {
    return statIncreaseIncrements;
  }

  public void setStatIncreaseIncrements(Map<String, Integer> statIncreaseIncrements) {
    this.statIncreaseIncrements = statIncreaseIncrements;
  }

  public StrifeAttribute(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getDescription() {
    return description;
  }

  public void setDescription(List<String> description) {
    this.description = description;
  }

  public int getCustomData() {
    return customData;
  }

  public void setCustomData(int customData) {
    this.customData = customData;
  }

  public Map<StrifeStat, Float> getAttributeMap() {
    return new HashMap<>(attributeMap);
  }

  public void setAttributeMap(Map<StrifeStat, Float> attributeMap) {
    this.attributeMap = attributeMap;
  }

  public double getAttribute(StrifeStat attribute) {
    if (attributeMap.containsKey(attribute)) {
      return attributeMap.get(attribute);
    }
    return 0;
  }

  @Override
  public int hashCode() {
    return key != null ? key.hashCode() : 0;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof StrifeAttribute)) {
      return false;
    }

    StrifeAttribute that = (StrifeAttribute) o;

    return !(key != null ? !key.equals(that.key) : that.key != null);
  }

  public int getSlot() {
    return slot;
  }

  public void setSlot(int slot) {
    this.slot = slot;
  }

  public Sound getClickSound() {
    return clickSound;
  }

  public void setClickSound(Sound clickSound) {
    this.clickSound = clickSound;
  }

  public float getClickPitch() {
    return clickPitch;
  }

  public void setClickPitch(float clickPitch) {
    this.clickPitch = clickPitch;
  }

  public Sound getLevelSound() {
    return levelSound;
  }

  public void setLevelSound(Sound levelSound) {
    this.levelSound = levelSound;
  }

  public float getLevelPitch() {
    return levelPitch;
  }

  public void setLevelPitch(float levelPitch) {
    this.levelPitch = levelPitch;
  }

}
