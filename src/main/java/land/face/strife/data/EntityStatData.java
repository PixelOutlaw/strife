package land.face.strife.data;

import com.tealcube.minecraft.bukkit.shade.objecthunter.exp4j.Expression;
import java.util.HashMap;
import java.util.Map;
import land.face.strife.stats.StrifeStat;

public class EntityStatData {

  private Expression baseExpExpr;
  private Map<StrifeStat, Float> baseValueMap;
  private Map<StrifeStat, Float> perLevelMap;

  public EntityStatData() {
    baseValueMap = new HashMap<>();
    perLevelMap = new HashMap<>();
  }

  public EntityStatData(EntityStatData entityStatData) {
    baseExpExpr = entityStatData.getBaseExpExpr();
    baseValueMap = new HashMap<>(entityStatData.getBaseValueMap());
    perLevelMap = new HashMap<>(entityStatData.getPerLevelMap());
  }

  public Expression getBaseExpExpr() {
    return baseExpExpr;
  }

  public void setBaseExpExpr(Expression baseExpExpr) {
    this.baseExpExpr = baseExpExpr;
  }

  public Map<StrifeStat, Float> getBaseValueMap() {
    return baseValueMap;
  }

  public Map<StrifeStat, Float> getPerLevelMap() {
    return perLevelMap;
  }

  public void putBaseValue(StrifeStat attribute, float value) {
    baseValueMap.put(attribute, value);
  }

  public void putPerLevel(StrifeStat attribute, float value) {
    perLevelMap.put(attribute, value);
  }
}
