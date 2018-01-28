package info.faceland.strife.data;

import info.faceland.strife.attributes.StrifeAttribute;
import java.util.HashMap;
import java.util.Map;

public class EntityStatData {
  private Map<StrifeAttribute, Double> baseValueMap;
  private Map<StrifeAttribute, Double> perLevelMap;
  private Map<StrifeAttribute, Double> perBonusLevelMap;

  public EntityStatData() {
    baseValueMap = new HashMap<>();
    perLevelMap = new HashMap<>();
  }

  public Map<StrifeAttribute, Double> getBaseValueMap() {
    return baseValueMap;
  }

  public Map<StrifeAttribute, Double> getPerLevelMap() {
    return perLevelMap;
  }

  public Map<StrifeAttribute, Double> getPerBonusLevelMap() {
    return perBonusLevelMap;
  }

  public void putBaseValue(StrifeAttribute attribute, double value) {
    baseValueMap.put(attribute, value);
  }

  public void putPerLevel(StrifeAttribute attribute, double value) {
    perLevelMap.put(attribute, value);
  }

  public void putPerBonusLevel(StrifeAttribute attribute, double value) {
    perBonusLevelMap.put(attribute, value);
  }
}
