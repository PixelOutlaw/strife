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
package land.face.strife.storage;

import io.pixeloutlaw.minecraft.spigot.config.SmartYamlConfiguration;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.LoreAbility;
import land.face.strife.data.ability.Ability;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.ChampionSaveData;
import land.face.strife.data.champion.ChampionSaveData.HealthDisplayType;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.champion.StrifeAttribute;
import land.face.strife.util.LogUtil;
import org.bukkit.configuration.ConfigurationSection;

public class FlatfileStorage implements DataStorage {

  private final StrifePlugin plugin;
  private final Map<UUID, SmartYamlConfiguration> configMap;

  public FlatfileStorage(StrifePlugin plugin) {
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
    LogUtil.printDebug("Saved " + champs + " players in " + diff + "ms");
  }


  @Override
  public void save(ChampionSaveData champion) {
    SmartYamlConfiguration config;
    String champUuid = champion.getUniqueId().toString();
    if (configMap.containsKey(champion.getUniqueId())) {
      config = configMap.get(champion.getUniqueId());
    } else {
      config = new SmartYamlConfiguration(
          new File(plugin.getDataFolder() + "/data", champUuid + ".json"));
    }

    for (Map.Entry<StrifeAttribute, Integer> entry : champion.getLevelMap().entrySet()) {
      config.set(champUuid + ".stats." + entry.getKey().getKey(), entry.getValue());
    }

    config.set(champUuid + ".display-exp", champion.isDisplayExp());
    config.set(champUuid + ".health-display", champion.getHealthDisplayType().toString());
    config.set(champUuid + ".unused-stat-points", champion.getUnusedStatPoints());
    config.set(champUuid + ".highest-reached-level", champion.getHighestReachedLevel());
    config.set(champUuid + ".bonus-levels", champion.getBonusLevels());
    for (LifeSkillType type : LifeSkillType.types) {
      config.set(champUuid + "." + type.getDataName() + "-level", champion.getSkillLevel(type));
      config.set(champUuid + "." + type.getDataName() + "-exp", champion.getSkillExp(type));
    }

    List<String> abilityIds = new ArrayList<>();
    for (Ability ability : champion.getAbilities().values()) {
      if (ability == null) {
        continue;
      }
      abilityIds.add(ability.getId());
    }
    config.set(champUuid + ".abilities", abilityIds);

    List<String> boundAbilityIds = new ArrayList<>();
    for (LoreAbility loreAbility : champion.getBoundAbilities()) {
      if (loreAbility == null) {
        continue;
      }
      boundAbilityIds.add(loreAbility.getId());
    }
    config.set(champUuid + ".lore-abilities", boundAbilityIds);

    configMap.put(champion.getUniqueId(), config);
    config.save();
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

      HealthDisplayType displayType;
      try {
        displayType = HealthDisplayType
            .valueOf(section.getString("health-display", "TEN_HEALTH_HEARTS"));
      } catch (Exception e) {
        displayType = HealthDisplayType.TEN_HEALTH_HEARTS;
      }
      saveData.setHealthDisplayType(displayType);
      saveData.setDisplayExp(section.getBoolean("display-exp", false));
      saveData.setHighestReachedLevel(section.getInt("highest-reached-level"));
      saveData.setBonusLevels(section.getInt("bonus-levels"));
      for (LifeSkillType type : LifeSkillType.types) {
        saveData.setSkillLevel(type, section.getInt(type.getDataName() + "-level", 0));
        saveData.setSkillExp(type, (float) section.getDouble(type.getDataName() + "-exp", 0f));
      }
      saveData.setUnusedStatPoints(section.getInt("unused-stat-points"));

      for (String s : section.getStringList("abilities")) {
        Ability ability = plugin.getAbilityManager().getAbility(s);
        if (ability == null) {
          LogUtil.printError("Ability " + s + " not found for player " + uuid);
          continue;
        }
        if (ability.getAbilityIconData() == null) {
          LogUtil.printError("Ability " + s + " no longer supports being slotted! uuid: " + uuid);
          continue;
        }
        saveData.setAbility(ability.getAbilityIconData().getAbilitySlot(), ability);
      }

      for (String s : section.getStringList("lore-abilities")) {
        LoreAbility loreAbility = plugin.getLoreAbilityManager().getLoreAbilityFromId(s);
        if (loreAbility == null) {
          LogUtil.printError("LoreAbility " + s + " not found for player " + uuid);
          continue;
        }
        saveData.getBoundAbilities().add(loreAbility);
      }

      if (section.isConfigurationSection("stats")) {
        ConfigurationSection statsSection = section.getConfigurationSection("stats");
        for (StrifeAttribute s : plugin.getAttributeManager().getAttributes()) {
          saveData.setLevel(s, statsSection.getInt(s.getKey(), 0));
        }
      } else {
        for (StrifeAttribute s : plugin.getAttributeManager().getAttributes()) {
          saveData.setLevel(s, 0);
        }
      }
    }
    return saveData;
  }
}