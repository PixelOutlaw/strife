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
package info.faceland.strife.managers;

import info.faceland.strife.data.EntityStatData;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.util.LogUtil;
import info.faceland.strife.util.StatUtil;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

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

  public Map<StrifeStat, Float> getBaseStats(LivingEntity livingEntity) {
    return getBaseStats(livingEntity, -1);
  }

  public Map<StrifeStat, Float> getBaseStats(LivingEntity livingEntity, int level) {
    Map<StrifeStat, Float> levelStats = new HashMap<>();
    EntityType type = livingEntity.getType();
    if (!entityStatDataMap.containsKey(type)) {
      return levelStats;
    }
    if (level == -1) {
      level = StatUtil.getMobLevel(livingEntity);
    }
    for (StrifeStat attr : entityStatDataMap.get(type).getPerLevelMap().keySet()) {
      levelStats.put(attr, entityStatDataMap.get(type).getPerLevelMap().get(attr) * level);
    }
    if (type == EntityType.PLAYER && level >= 100) {
      int bLevel = championManager.getChampion((Player) livingEntity).getBonusLevels();
      Map<StrifeStat, Float> bonusStats = new HashMap<>();
      for (StrifeStat attr : entityStatDataMap.get(type).getPerBonusLevelMap().keySet()) {
        bonusStats.put(attr, entityStatDataMap.get(type).getPerBonusLevelMap().get(attr) * bLevel);
      }
      return StatUpdateManager
          .combineMaps(entityStatDataMap.get(type).getBaseValueMap(), levelStats, bonusStats);
    }
    return StatUpdateManager
        .combineMaps(entityStatDataMap.get(type).getBaseValueMap(), levelStats);
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
    if (cs.isConfigurationSection("per-bonus-level")) {
      ConfigurationSection attrCS = cs.getConfigurationSection("per-bonus-level");
      for (String k : attrCS.getKeys(false)) {
        StrifeStat attr = StrifeStat.valueOf(k);
        data.putPerBonusLevel(attr, (float) attrCS.getDouble(k));
      }
    }
    if ("default".equalsIgnoreCase(key)) {
      defaultData = data;
    } else {
      addEntityData(entityType, data);
    }
  }
}
