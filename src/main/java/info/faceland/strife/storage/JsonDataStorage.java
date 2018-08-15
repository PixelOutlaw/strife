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
package info.faceland.strife.storage;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.Champion;
import info.faceland.strife.data.ChampionSaveData;
import info.faceland.strife.stats.StrifeStat;
import io.pixeloutlaw.minecraft.spigot.config.SmartYamlConfiguration;
import java.util.HashMap;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.util.Map;
import java.util.UUID;

public class JsonDataStorage implements DataStorage {

  private final StrifePlugin plugin;
  private final Map<UUID, SmartYamlConfiguration> configMap;

  public JsonDataStorage(StrifePlugin plugin) {
    this.plugin = plugin;
    this.configMap = new HashMap<>();
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
      config.set(champion.getUniqueId().toString() + ".stats." + entry.getKey().getKey(),
          entry.getValue()
      );
    }
    config.set(champion.getUniqueId().toString() + ".unused-stat-points",
        champion.getUnusedStatPoints());
    config.set(champion.getUniqueId().toString() + ".highest-reached-level",
        champion.getHighestReachedLevel());
    config.set(champion.getUniqueId().toString() + ".bonus-levels", champion.getBonusLevels());
    config.set(champion.getUniqueId().toString() + ".crafting-level", champion.getCraftingLevel());
    config.set(champion.getUniqueId().toString() + ".crafting-exp", champion.getCraftingExp());
    config.set(champion.getUniqueId().toString() + ".enchant-level", champion.getEnchantLevel());
    config.set(champion.getUniqueId().toString() + ".enchant-exp", champion.getEnchantExp());
    config.set(champion.getUniqueId().toString() + ".fishing-level", champion.getFishingLevel());
    config.set(champion.getUniqueId().toString() + ".fishing-exp", champion.getFishingExp());
    config.set(champion.getUniqueId().toString() + ".mining-level", champion.getMiningLevel());
    config.set(champion.getUniqueId().toString() + ".mining-exp", champion.getMiningExp());
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
      saveData.setCraftingExp((float) section.getDouble("crafting-exp"));
      saveData.setEnchantLevel(section.getInt("enchant-level"));
      saveData.setEnchantExp((float) section.getDouble("enchant-exp"));
      saveData.setFishingLevel(section.getInt("fishing-level"));
      saveData.setFishingExp((float) section.getDouble("fishing-exp"));
      saveData.setFishingLevel(section.getInt("mining-level"));
      saveData.setFishingExp((float) section.getDouble("mining-exp"));

      saveData.setUnusedStatPoints(section.getInt("unused-stat-points"));
      if (section.isConfigurationSection("stats")) {
        ConfigurationSection statsSection = section.getConfigurationSection("stats");
        for (StrifeStat s : plugin.getStatManager().getStats()) {
          saveData.setLevel(s, statsSection.getInt(s.getKey(), 0));
        }
      } else {
        for (StrifeStat s : plugin.getStatManager().getStats()) {
          saveData.setLevel(s, 0);
        }
      }
    }
    return saveData;
  }
}