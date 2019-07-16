package info.faceland.strife.data;

import info.faceland.strife.stats.StrifeStat;
import java.util.HashMap;
import java.util.Map;

public class EntityStatData {
  private Map<StrifeStat, Double> baseValueMap;
  private Map<StrifeStat, Double> perLevelMap;
  private Map<StrifeStat, Double> perBonusLevelMap;

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

  public Map<StrifeStat, Double> getBaseValueMap() {
    return baseValueMap;
  }

  public Map<StrifeStat, Double> getPerLevelMap() {
    return perLevelMap;
  }

  public Map<StrifeStat, Double> getPerBonusLevelMap() {
    return perBonusLevelMap;
  }

  public void putBaseValue(StrifeStat attribute, double value) {
    baseValueMap.put(attribute, value);
  }

  public void putPerLevel(StrifeStat attribute, double value) {
    perLevelMap.put(attribute, value);
  }

  public void putPerBonusLevel(StrifeStat attribute, double value) {
    perBonusLevelMap.put(attribute, value);
  }
}
