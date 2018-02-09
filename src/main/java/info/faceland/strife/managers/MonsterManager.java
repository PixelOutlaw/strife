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

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.math.NumberUtils;
import com.tealcube.minecraft.bukkit.shade.google.common.base.CharMatcher;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.AttributeHandler;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.EntityStatData;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class MonsterManager {

    private final StrifePlugin plugin;
    private Map<EntityType, EntityStatData> entityStatDataMap;

    public MonsterManager(StrifePlugin plugin) {
        this.plugin = plugin;
        this.entityStatDataMap = new HashMap<>();
    }

    public void addEntityData(EntityType type, EntityStatData data) {
        entityStatDataMap.put(type, data);
    }

    public Map<StrifeAttribute, Double> getBaseStats(LivingEntity livingEntity) {
        Map<StrifeAttribute, Double> levelStats = new HashMap<>();
        EntityType type = livingEntity.getType();
        if (!entityStatDataMap.containsKey(type)) {
            return levelStats;
        }
        int level = getEntityLevel(livingEntity);

        for (Map.Entry<StrifeAttribute, Double> stat : entityStatDataMap.get(type).getPerLevelMap().entrySet()) {
            levelStats.put(stat.getKey(), stat.getValue() * level);
        }
        if (type == EntityType.PLAYER && level >= 100) {
            int bonusLevel = plugin.getChampionManager().getChampion(livingEntity.getUniqueId()).getBonusLevels();
            Map<StrifeAttribute, Double> bonusStats = new HashMap<>();
            for (Map.Entry<StrifeAttribute, Double> stat : entityStatDataMap.get(type).getPerBonusLevelMap().entrySet()) {
                bonusStats.put(stat.getKey(), stat.getValue() * bonusLevel);
            }
            levelStats = AttributeHandler.combineMaps(levelStats, bonusStats);
        }
        return AttributeHandler.combineMaps(entityStatDataMap.get(type).getBaseValueMap(), levelStats);
    }

    private int getEntityLevel(LivingEntity entity) {
        if (entity instanceof Player) {
            return ((Player) entity).getLevel();
        }
        if (entity.getCustomName() != null) {
            return NumberUtils.toInt(CharMatcher.DIGIT.retainFrom(ChatColor.stripColor(entity.getCustomName())), 0);
        }
        return 0;
    }
}
