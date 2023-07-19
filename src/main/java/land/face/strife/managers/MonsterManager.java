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

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import com.tealcube.minecraft.bukkit.shade.objecthunter.exp4j.Expression;
import com.tealcube.minecraft.bukkit.shade.objecthunter.exp4j.ExpressionBuilder;
import java.util.HashMap;
import java.util.Map;
import land.face.strife.data.EntityStatData;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.LogUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public class MonsterManager {

  private final ChampionManager championManager;
  private final Map<EntityType, EntityStatData> entityStatDataMap;
  private EntityStatData defaultData;

  public MonsterManager(ChampionManager championManager) {
    this.championManager = championManager;
    this.entityStatDataMap = new HashMap<>();
    this.defaultData = new EntityStatData();
  }

  public boolean containsEntityType(EntityType type) {
    return entityStatDataMap.containsKey(type);
  }

  private void addEntityData(EntityType type, EntityStatData data) {
    entityStatDataMap.put(type, data);
  }

  public float getBaseExp(LivingEntity livingEntity, int mobLevel) {
    EntityStatData data = entityStatDataMap.get(livingEntity.getType());
    Expression expression;
    if (data == null) {
      expression = defaultData.getBaseExpExpr();
    } else {
      expression = data.getBaseExpExpr();
    }
    return (float) expression.setVariable("LEVEL", mobLevel).evaluate();
  }

  public Map<StrifeStat, Float> getBaseStats(EntityType type, int level) {
    Map<StrifeStat, Float> levelStats = new HashMap<>();
    if (!entityStatDataMap.containsKey(type)) {
      return levelStats;
    }
    for (StrifeStat attr : entityStatDataMap.get(type).getPerLevelMap().keySet()) {
      levelStats.put(attr, entityStatDataMap.get(type).getPerLevelMap().get(attr) * level);
    }
    return StatUpdateManager.combineMaps(entityStatDataMap.get(type).getBaseValueMap(), levelStats);
  }

  public void loadBaseStats(String key, ConfigurationSection cs) {
    EntityType entityType;
    if ("default".equalsIgnoreCase(key) && !defaultData.getBaseValueMap().isEmpty()) {
      return;
    }
    try {
      if ("default".equalsIgnoreCase(key)) {
        entityType = null;
      } else {
        entityType = EntityType.valueOf(key);
      }
    } catch (Exception e) {
      LogUtil.printWarning("Skipping base stat load for invalid entity type '" + key + "'");
      return;
    }
    EntityStatData data = new EntityStatData(defaultData);
    String expStr = cs.getString("exp-expression");
    if (StringUtils.isNotBlank(expStr)) {
      data.setBaseExpExpr(new ExpressionBuilder(expStr).variables("LEVEL").build());
    }
    if (cs.isConfigurationSection("base-values")) {
      ConfigurationSection attrCS = cs.getConfigurationSection("base-values");
      for (String k : attrCS.getKeys(false)) {
        StrifeStat attr = StrifeStat.valueOf(k);
        data.putBaseValue(attr, (float) attrCS.getDouble(k));
      }
    }
    if (cs.isConfigurationSection("per-level")) {
      ConfigurationSection attrCS = cs.getConfigurationSection("per-level");
      for (String k : attrCS.getKeys(false)) {
        StrifeStat attr = StrifeStat.valueOf(k);
        data.putPerLevel(attr, (float) attrCS.getDouble(k));
      }
    }
    if ("default".equalsIgnoreCase(key)) {
      defaultData = data;
    } else {
      addEntityData(entityType, data);
    }
  }
}
