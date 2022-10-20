/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
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
import land.face.strife.data.LevelPath;
import land.face.strife.data.LevelPath.Choice;
import land.face.strife.data.LevelPath.Path;
import land.face.strife.data.LoreAbility;
import land.face.strife.data.ability.Ability;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.ChampionSaveData;
import land.face.strife.data.champion.ChampionSaveData.SelectedGod;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.champion.StrifeAttribute;
import land.face.strife.stats.AbilitySlot;
import land.face.strife.util.LogUtil;
import org.bukkit.Bukkit;
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

    // Preferences
    config.set(champUuid + ".display-exp", champion.isDisplayExp());
    config.set(champUuid + ".glow-enabled", champion.isGlowEnabled());

    config.set(champUuid + ".unused-stat-points", champion.getUnusedStatPoints());
    config.set(champUuid + ".highest-reached-level", champion.getHighestReachedLevel());
    config.set(champUuid + ".bonus-levels", champion.getBonusLevels());
    config.set(champUuid + ".pvp-score", (int) champion.getPvpScore());

    for (LifeSkillType type : LifeSkillType.types) {
      config.set(champUuid + "." + type.getDataName() + "-level", champion.getSkillLevel(type));
      config.set(champUuid + "." + type.getDataName() + "-exp", champion.getSkillExp(type));
    }

    config.set(champUuid + ".catchup-xp-used", champion.getCatchupExpUsed());

    config.set(champUuid + "." + "god", champion.getSelectedGod() == null ?
        "NONE" : champion.getSelectedGod().toString());
    for (SelectedGod g : SelectedGod.values()) {
      if (g == SelectedGod.NONE) {
        continue;
      }
      config.set(champUuid + "." + g + "-xp", champion.getGodXp().get(g));
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

    if (champion.getCastMessages().get(AbilitySlot.SLOT_A) != null) {
      config.set(champUuid + ".slot-messages.SLOT_A", champion.getCastMessages().get(AbilitySlot.SLOT_A));
    }
    if (champion.getCastMessages().get(AbilitySlot.SLOT_B) != null) {
      config.set(champUuid + ".slot-messages.SLOT_B", champion.getCastMessages().get(AbilitySlot.SLOT_B));
    }
    if (champion.getCastMessages().get(AbilitySlot.SLOT_C) != null) {
      config.set(champUuid + ".slot-messages.SLOT_C", champion.getCastMessages().get(AbilitySlot.SLOT_C));
    }

    for (Path path : LevelPath.PATH_VALUES) {
      if (champion.getPathMap().containsKey(path)) {
        config.set(champUuid + ".passives." + path.toString(), champion.getPathMap().get(path).toString());
      } else {
        config.set(champUuid + ".passives." + path.toString(), null);
      }
    }

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

      saveData.setDisplayExp(section.getBoolean("display-exp", false));
      saveData.setGlowEnabled(section.getBoolean("glow-enabled", true));

      saveData.setHighestReachedLevel(section.getInt("highest-reached-level"));
      saveData.setBonusLevels(section.getInt("bonus-levels"));

      saveData.setSelectedGod(SelectedGod.valueOf(section.getString("god", "NONE")));
      saveData.setGodXp(SelectedGod.FACEGUY, section.getInt("FACEGUY-xp", 0));
      saveData.setGodXp(SelectedGod.ZEXIR, section.getInt("ZEXIR-xp", 0));
      saveData.setGodXp(SelectedGod.AURORA, section.getInt("AURORA-xp", 0));
      saveData.setGodXp(SelectedGod.ANYA, section.getInt("ANYA-xp", 0));

      saveData.setCatchupExpUsed(section.getDouble("catchup-xp-used"));

      for (LifeSkillType type : LifeSkillType.types) {
        int level = section.getInt(type.getDataName() + "-level", 1);
        saveData.setSkillLevel(type, Math.max(1, level));
        float xp = (float) section.getDouble(type.getDataName() + "-exp", 0f);
        Float primXp = new Float(xp);
        if (primXp.isNaN() || primXp.isInfinite()) {
          xp = 0f;
          Bukkit.getLogger().warning("Hey! NaN xp value for " + type + " on user " + uuid);
        }
        saveData.setSkillExp(type, xp);
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

      if (section.isConfigurationSection("slot-messages")) {
        ConfigurationSection msg = section.getConfigurationSection("slot-messages");
        saveData.getCastMessages().put(AbilitySlot.SLOT_A, msg.getStringList("SLOT_A"));
        saveData.getCastMessages().put(AbilitySlot.SLOT_B, msg.getStringList("SLOT_B"));
        saveData.getCastMessages().put(AbilitySlot.SLOT_C, msg.getStringList("SLOT_C"));
      }

      if (section.isConfigurationSection("passives")) {
        ConfigurationSection passiveSection = section.getConfigurationSection("passives");
        if (passiveSection != null) {
          for (String passive : passiveSection.getKeys(false)) {
            Path path = Path.valueOf(passive);
            Choice choice = Choice.valueOf(passiveSection.getString(passive));
            saveData.getPathMap().put(path, choice);
          }
        }
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
      saveData.setPvpScore((float) section.getDouble("pvp-score", 700));
    }
    return saveData;
  }
}