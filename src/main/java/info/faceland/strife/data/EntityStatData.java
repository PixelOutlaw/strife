package info.faceland.strife.data;

import info.faceland.strife.stats.StrifeStat;
import java.util.HashMap;
import java.util.Map;

public class EntityStatData {
  private Map<StrifeStat, Float> baseValueMap;
  private Map<StrifeStat, Float> perLevelMap;
  private Map<StrifeStat, Float> perBonusLevelMap;

  public EntityStatData() {
    baseValueMap = new HashMap<>();
    perLevelMap = new HashMap<>();
    perBonusLevelMap = new HashMap<>();
  }

  public EntityStatData(EntityStatData entityStatData) {
    baseValueMap = new HashMap<>(entityStatData.getBaseValueMap());
    perLevelMap = new HashMap<>(entityStatData.getPerLevelMap());
    perBonusLevelMap = new HashMap<>(entityStatData.getPerBonusLevelMap());
  }

  public Map<StrifeStat, Float> getBaseValueMap() {
    return baseValueMap;
  }

  public Map<StrifeStat, Float> getPerLevelMap() {
    return perLevelMap;
  }

  public Map<StrifeStat, Float> getPerBonusLevelMap() {
    return perBonusLevelMap;
  }

  public void putBaseValue(StrifeStat attribute, float value) {
    baseValueMap.put(attribute, value);
  }

  public void putPerLevel(StrifeStat attribute, float value) {
    perLevelMap.put(attribute, value);
  }

  public void putPerBonusLevel(StrifeStat attribute, float value) {
    perBonusLevelMap.put(attribute, value);
  }
}
