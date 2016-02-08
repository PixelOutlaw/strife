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
package info.faceland.strife.storage;

import com.tealcube.minecraft.bukkit.config.SmartYamlConfiguration;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.Champion;
import info.faceland.strife.stats.StrifeStat;

import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class JsonDataStorage implements DataStorage {

    private final StrifePlugin plugin;
    private SmartYamlConfiguration configuration;

    public JsonDataStorage(StrifePlugin plugin) {
        this.plugin = plugin;
        this.configuration = new SmartYamlConfiguration(new File(plugin.getDataFolder(), "data.json"));
    }

    @Override
    public void init() {
        // do nothing
    }

    @Override
    public void shutdown() {
        // do nothing
    }

    @Override
    public void save(Collection<Champion> champions) {
        for (Champion champ : champions) {
            for (Map.Entry<StrifeStat, Integer> entry : champ.getLevelMap().entrySet()) {
                configuration.set(
                        champ.getUniqueId().toString() + ".stats." + entry.getKey().getKey(),
                        entry.getValue()
                );
            }
            configuration.set(
                    champ.getUniqueId().toString() + ".unused-stat-points",
                    champ.getUnusedStatPoints()
            );
            configuration.set(
                    champ.getUniqueId().toString() + ".highest-reached-level",
                    champ.getHighestReachedLevel()
            );
            for (Map.Entry<StrifeAttribute, Double> entry : champ.getAttributeStatCache().entrySet()) {
                configuration.set(
                        champ.getUniqueId().toString() + ".cache.stat." + entry.getKey().name(),
                        entry.getValue()
                );
            }
            for (Map.Entry<StrifeAttribute, Double> entry : champ.getAttributeArmorCache().entrySet()) {
                configuration.set(
                        champ.getUniqueId().toString() + ".cache.armor." + entry.getKey().name(),
                        entry.getValue()
                );
            }
            for (Map.Entry<StrifeAttribute, Double> entry : champ.getAttributeWeaponCache().entrySet()) {
                configuration.set(
                        champ.getUniqueId().toString() + ".cache.weapon." + entry.getKey().name(),
                        entry.getValue()
                );
            }
        }
        configuration.save();
    }

    @Override
    public Collection<Champion> load() {
        configuration.load();
        Collection<Champion> collection = new HashSet<>();
        for (String key : configuration.getKeys(false)) {
            if (!configuration.isConfigurationSection(key)) {
                continue;
            }
            ConfigurationSection section = configuration.getConfigurationSection(key);
            UUID uuid = UUID.fromString(key);
            Champion champion = new Champion(uuid);
            if (section.isConfigurationSection("stats")) {
                ConfigurationSection statsSection = section.getConfigurationSection("stats");
                for (String k : statsSection.getKeys(false)) {
                    plugin.debug(Level.INFO, key + " : " + k);
                    StrifeStat stat = plugin.getStatManager().getStat(k);
                    if (stat == null) {
                        continue;
                    }
                    plugin.debug(Level.INFO, "stat: " + stat.getName());
                    champion.setLevel(stat, section.getInt(k));
                }
            } else {
                for (String k : section.getKeys(false)) {
                    plugin.debug(Level.INFO, key + " : " + k);
                    StrifeStat stat = plugin.getStatManager().getStat(k);
                    if (stat == null) {
                        continue;
                    }
                    plugin.debug(Level.INFO, "stat: " + stat.getName());
                    section.set("stats." + stat.getKey(), section.getInt(k));
                    champion.setLevel(stat, section.getInt(k));
                }
            }
            if (section.isConfigurationSection("cache")) {
                ConfigurationSection cacheSection = section.getConfigurationSection("cache");
                if (cacheSection.isConfigurationSection("stat")) {
                    for (String k : cacheSection.getConfigurationSection("stat").getKeys(false)) {
                        StrifeAttribute attr = StrifeAttribute.fromName(k);
                        if (attr == null) {
                            continue;
                        }
                        champion.setStatCacheAttribue(attr, cacheSection.getDouble("stat." + k));
                    }
                }
                if (cacheSection.isConfigurationSection("armor")) {
                    for (String k : cacheSection.getConfigurationSection("armor").getKeys(false)) {
                        StrifeAttribute attr = StrifeAttribute.fromName(k);
                        if (attr == null) {
                            continue;
                        }
                        champion.setArmorCacheAttribue(attr, cacheSection.getDouble("armor." + k));
                    }
                }
                if (cacheSection.isConfigurationSection("weapon")) {
                    for (String k : cacheSection.getConfigurationSection("weapon").getKeys(false)) {
                        StrifeAttribute attr = StrifeAttribute.fromName(k);
                        if (attr == null) {
                            continue;
                        }
                        champion.setWeaponCacheAttribue(attr, cacheSection.getDouble("weapon." + k));
                    }
                }
            }
            champion.setUnusedStatPoints(section.getInt("unused-stat-points"));
            champion.setHighestReachedLevel(section.getInt("highest-reached-level"));
            collection.add(champion);
            for (String k : section.getKeys(false)) {
                if (k.equals("stats") || k.equals("unused-stat-points") || k.equals("highest-reached-level") ||
                        k.equals("cache")) {
                    continue;
                }
                plugin.debug(Level.INFO, "clearing " + key + "." + k);
                section.set(k, null);
            }
        }
        configuration.save();
        return collection;
    }

}
