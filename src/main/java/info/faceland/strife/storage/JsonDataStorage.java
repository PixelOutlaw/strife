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

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.Champion;
import info.faceland.strife.data.ChampionSaveData;
import info.faceland.strife.stats.StrifeStat;
import io.pixeloutlaw.minecraft.spigot.config.SmartYamlConfiguration;
import java.util.HashMap;
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
    private final Map<UUID, SmartYamlConfiguration> configMap;
    private long lastLoaded;

    public JsonDataStorage(StrifePlugin plugin) {
        this.plugin = plugin;
        this.configMap = new HashMap<>();
        this.configuration = new SmartYamlConfiguration(new File(plugin.getDataFolder(), "data.json"));
        this.lastLoaded = System.currentTimeMillis();
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
    public void saveAll() {
        long start = System.currentTimeMillis();
        int champs = 0;
        for (Champion champ : plugin.getChampionManager().getChampions()) {
            save(champ.getSaveData());
            champs++;
        }
        long diff = System.currentTimeMillis() - start;
        plugin.getLogger().info("Saved " + champs + " players in " + diff + "ms");
    }


    @Override
    public void save(ChampionSaveData champion) {
        SmartYamlConfiguration config;
        if (configMap.containsKey(champion.getUniqueId())) {
            config = configMap.get(champion.getUniqueId());
        } else {
            config = new SmartYamlConfiguration(new File(
                plugin.getDataFolder() + "/data", champion.getUniqueId().toString() + ".json"));
        }
        for (Map.Entry<StrifeStat, Integer> entry : champion.getLevelMap().entrySet()) {
            config.set(
                champion.getUniqueId().toString() + ".stats." + entry.getKey().getKey(),
                entry.getValue()
            );
        }
        config.set(
            champion.getUniqueId().toString() + ".unused-stat-points",
            champion.getUnusedStatPoints()
        );
        config.set(
            champion.getUniqueId().toString() + ".highest-reached-level",
            champion.getHighestReachedLevel()
        );
        config.set(
            champion.getUniqueId().toString() + ".bonus-levels",
            champion.getBonusLevels()
        );
        config.set(
            champion.getUniqueId().toString() + ".crafting-level",
            champion.getCraftingLevel()
        );
        config.set(
            champion.getUniqueId().toString() + ".crafting-exp",
            champion.getCraftingExp()
        );
        config.set(
            champion.getUniqueId().toString() + ".enchant-level",
            champion.getEnchantLevel()
        );
        config.set(
            champion.getUniqueId().toString() + ".enchant-exp",
            champion.getEnchantExp()
        );
        config.set(
            champion.getUniqueId().toString() + ".fishing-level",
            champion.getFishingLevel()
        );
        config.set(
            champion.getUniqueId().toString() + ".fishing-exp",
            champion.getFishingExp()
        );
        config.save();
        configMap.put(champion.getUniqueId(), config);
    }

    public ChampionSaveData load(UUID uuid) {
        SmartYamlConfiguration config = new SmartYamlConfiguration(
            new File(plugin.getDataFolder() + "/data", uuid.toString() + ".json"));
        config.load();
        ChampionSaveData saveData = new ChampionSaveData(uuid);
        for (String key : config.getKeys(false)) {
            if (!config.isConfigurationSection(key)) {
                continue;
            }
            ConfigurationSection section = config.getConfigurationSection(key);
            saveData.setHighestReachedLevel(section.getInt("highest-reached-level"));
            saveData.setBonusLevels(section.getInt("bonus-levels"));
            saveData.setCraftingLevel(section.getInt("crafting-level"));
            saveData.setCraftingExp((float)section.getDouble("crafting-exp"));
            saveData.setEnchantLevel(section.getInt("enchant-level"));
            saveData.setEnchantExp((float)section.getDouble("enchant-exp"));
            saveData.setFishingLevel(section.getInt("fishing-level"));
            saveData.setFishingExp((float)section.getDouble("fishing-exp"));

            saveData.setUnusedStatPoints(section.getInt("unused-stat-points"));
            ConfigurationSection statsSection = section.getConfigurationSection("stats");
            for (String k : statsSection.getKeys(false)) {
                StrifeStat stat = plugin.getStatManager().getStat(k);
                if (stat == null) {
                    continue;
                }
                saveData.setLevel(stat, statsSection.getInt(k));
            }
        }
        return saveData;
    }

    public Collection<ChampionSaveData> oldLoad() {
        if (loadIfAble()) {
            plugin.debug(Level.FINE, "Loading data.json");
        }
        Collection<ChampionSaveData> collection = new HashSet<>();
        for (String key : configuration.getKeys(false)) {
            if (!configuration.isConfigurationSection(key)) {
                continue;
            }
            ConfigurationSection section = configuration.getConfigurationSection(key);
            UUID uuid = UUID.fromString(key);
            ChampionSaveData saveData = new ChampionSaveData(uuid);
            saveData.setHighestReachedLevel(section.getInt("highest-reached-level"));
            saveData.setBonusLevels(section.getInt("bonus-levels"));
            saveData.setCraftingLevel(section.getInt("crafting-level"));
            saveData.setCraftingExp((float)section.getDouble("crafting-exp"));
            saveData.setEnchantLevel(section.getInt("enchant-level"));
            saveData.setEnchantExp((float)section.getDouble("enchant-exp"));
            saveData.setFishingLevel(section.getInt("fishing-level"));
            saveData.setFishingExp((float)section.getDouble("fishing-exp"));
            saveData.setUnusedStatPoints(section.getInt("unused-stat-points"));
            ConfigurationSection statsSection = section.getConfigurationSection("stats");
            for (String k : statsSection.getKeys(false)) {
                StrifeStat stat = plugin.getStatManager().getStat(k);
                if (stat == null) {
                    continue;
                }
                saveData.setLevel(stat, statsSection.getInt(k));
            }
            collection.add(saveData);
        }
        return collection;
    }

    //@Override
    //public ChampionSaveData load(UUID uuid) {
    //    if (loadIfAble()) {
    //        plugin.debug(Level.FINE, "Loading data.json");
    //    }
    //    if (!configuration.isConfigurationSection(uuid.toString())) {
    //        plugin.debug(Level.FINER, "Unable to find player with UUID " + uuid.toString());
    //        return null;
    //    }
    //    String key = uuid.toString();
    //    ConfigurationSection section = configuration.getConfigurationSection(key);
    //    ChampionSaveData saveData = new ChampionSaveData(uuid);
    //    boolean hadReset = checkResetAndSetLevels(section, saveData, true);
    //    saveData.setHighestReachedLevel(section.getInt("highest-reached-level"));
    //    saveData.setBonusLevels(section.getInt("bonus-levels"));
    //    saveData.setCraftingLevel(section.getInt("crafting-level"));
    //    saveData.setCraftingExp((float)section.getDouble("crafting-exp"));
    //    saveData.setEnchantLevel(section.getInt("enchant-level"));
    //    saveData.setEnchantExp((float)section.getDouble("enchant-exp"));
    //    saveData.setFishingLevel(section.getInt("fishing-level"));
    //    saveData.setFishingExp((float)section.getDouble("fishing-exp"));
    //    if (hadReset) {
    //        saveData.setUnusedStatPoints(saveData.getHighestReachedLevel());
    //    } else {
    //        saveData.setUnusedStatPoints(section.getInt("unused-stat-points"));
    //    }
    //    return saveData;
    //}

    private boolean loadIfAble() {
        long now = System.currentTimeMillis();
        long diff = now - lastLoaded;
        plugin.debug(Level.FINER, "Loading data.json...");
        if (diff >= plugin.getSettings().getInt("config.configuration-load-period", 10)) {
            configuration.load();
            lastLoaded = now;
            return true;
        }
        return false;
    }

}