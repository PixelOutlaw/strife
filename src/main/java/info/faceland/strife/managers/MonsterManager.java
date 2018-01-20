/**
 * The MIT License
 * Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package info.faceland.strife.managers;

import info.faceland.strife.attributes.AttributeHandler;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.EntityStatData;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.EntityType;

public class MonsterManager {

    private Map<EntityType, EntityStatData> entityStatDataMap;

    public MonsterManager() {
        entityStatDataMap = new HashMap<>();
    }

    public EntityStatData getBaseStats(EntityType type) {
        if (entityStatDataMap.containsKey(type)) {
            return entityStatDataMap.get(type);
        }
        return null;
    }

    public void addEntityData(EntityType type, EntityStatData data) {
        entityStatDataMap.put(type, data);
    }

    public Map<EntityType, EntityStatData> getEntityStatDataMap() {
        return entityStatDataMap;
    }

    public Map<StrifeAttribute, Double> getBaseStats(EntityType type, int level) {
        Map<StrifeAttribute, Double> levelBasedStats = new HashMap<>();
        if (!entityStatDataMap.containsKey(type)) {
            return levelBasedStats;
        }
        for (Map.Entry<StrifeAttribute, Double> stat : entityStatDataMap.get(type).getPerLevelMap().entrySet()) {
            levelBasedStats.put(stat.getKey(), stat.getValue() * level);
        }
        return AttributeHandler.combineMaps(entityStatDataMap.get(type).getBaseValueMap(), levelBasedStats);
    }

}
