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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
  public void save(ChampionSaveData saveData) {
    SmartYamlConfiguration config;
    String champUuid = saveData.getUniqueId().toString();
    if (configMap.containsKey(saveData.getUniqueId())) {
      config = configMap.get(saveData.getUniqueId());
    } else {
      config = new SmartYamlConfiguration(
          new File(plugin.getDataFolder() + "/data", champUuid + ".json"));
    }

    for (Map.Entry<StrifeAttribute, Integer> entry : saveData.getLevelMap().entrySet()) {
      config.set(champUuid + ".stats." + entry.getKey().getKey(), entry.getValue());
    }

    // Preferences
    config.set(champUuid + ".display-exp", saveData.isDisplayExp());
    config.set(champUuid + ".glow-enabled", saveData.isGlowEnabled());

    config.set(champUuid + ".unused-stat-points", saveData.getUnusedStatPoints());
    config.set(champUuid + ".highest-reached-level", saveData.getHighestReachedLevel());
    config.set(champUuid + ".pvp-score", (int) saveData.getPvpScore());

    config.set(champUuid + ".prayer", (int) saveData.getPrayerPoints());

    config.set(champUuid + ".FACEGUY-xp", saveData.getGodXp().getOrDefault(SelectedGod.FACEGUY, 0));
    config.set(champUuid + ".AURORA-xp", saveData.getGodXp().getOrDefault(SelectedGod.AURORA, 0));
    config.set(champUuid + ".ZEXIR-xp", saveData.getGodXp().getOrDefault(SelectedGod.ZEXIR, 0));
    config.set(champUuid + ".ANYA-xp", saveData.getGodXp().getOrDefault(SelectedGod.ANYA, 0));

    config.set(champUuid + ".FACEGUY-level", saveData.getGodLevel().getOrDefault(SelectedGod.FACEGUY, 1));
    config.set(champUuid + ".AURORA-level", saveData.getGodLevel().getOrDefault(SelectedGod.AURORA, 1));
    config.set(champUuid + ".ZEXIR-level", saveData.getGodLevel().getOrDefault(SelectedGod.ZEXIR, 1));
    config.set(champUuid + ".ANYA-level", saveData.getGodLevel().getOrDefault(SelectedGod.ANYA, 1));

    for (LifeSkillType type : LifeSkillType.types) {
      config.set(champUuid + "." + type.getDataName() + "-level", saveData.getSkillLevel(type));
      config.set(champUuid + "." + type.getDataName() + "-exp", saveData.getSkillExp(type));
    }

    config.set(champUuid + ".catchup-xp-used", saveData.getCatchupExpUsed());

    config.set(champUuid + ".god", saveData.getSelectedGod() == null ? "NONE" : saveData.getSelectedGod().toString());

    List<String> boundAbilityIds = new ArrayList<>(saveData.getBoundAbilities());

    config.set(champUuid + ".lore-abilities", boundAbilityIds);

    if (saveData.getAbility(AbilitySlot.SLOT_A) != null) {
      config.set(champUuid + ".ability.SLOT_A.id", saveData.getAbility(AbilitySlot.SLOT_A));
    }
    if (saveData.getAbility(AbilitySlot.SLOT_B) != null) {
      config.set(champUuid + ".ability.SLOT_B.id", saveData.getAbility(AbilitySlot.SLOT_B));
    }
    if (saveData.getAbility(AbilitySlot.SLOT_C) != null) {
      config.set(champUuid + ".ability.SLOT_C.id", saveData.getAbility(AbilitySlot.SLOT_C));
    }
    if (saveData.getAbility(AbilitySlot.SLOT_D) != null) {
      config.set(champUuid + ".ability.SLOT_D.id", saveData.getAbility(AbilitySlot.SLOT_D));
    }

    config.set(champUuid + ".ability.SLOT_A.msg",
        saveData.getCastMessages().getOrDefault(AbilitySlot.SLOT_A, Collections.emptyList()));
    config.set(champUuid + ".ability.SLOT_B.msg",
        saveData.getCastMessages().getOrDefault(AbilitySlot.SLOT_B, Collections.emptyList()));
    config.set(champUuid + ".ability.SLOT_C.msg",
        saveData.getCastMessages().getOrDefault(AbilitySlot.SLOT_C, Collections.emptyList()));
    config.set(champUuid + ".ability.SLOT_D.msg",
        saveData.getCastMessages().getOrDefault(AbilitySlot.SLOT_D, Collections.emptyList()));

    for (Path path : LevelPath.PATH_VALUES) {
      if (saveData.getPathMap().containsKey(path)) {
        config.set(champUuid + ".passives." + path.toString(), saveData.getPathMap().get(path).toString());
      } else {
        config.set(champUuid + ".passives." + path.toString(), null);
      }
    }

    configMap.put(saveData.getUniqueId(), config);
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

      saveData.setSelectedGod(SelectedGod.valueOf(section.getString("god", "NONE")));

      saveData.setGodXp(SelectedGod.FACEGUY, section.getInt("FACEGUY-xp", 0));
      saveData.setGodXp(SelectedGod.ZEXIR, section.getInt("ZEXIR-xp", 0));
      saveData.setGodXp(SelectedGod.AURORA, section.getInt("AURORA-xp", 0));
      saveData.setGodXp(SelectedGod.ANYA, section.getInt("ANYA-xp", 0));

      saveData.setGodLevel(SelectedGod.FACEGUY, section.getInt("FACEGUY-level", 1));
      saveData.setGodLevel(SelectedGod.ZEXIR, section.getInt("ZEXIR-level", 1));
      saveData.setGodLevel(SelectedGod.AURORA, section.getInt("AURORA-level", 1));
      saveData.setGodLevel(SelectedGod.ANYA, section.getInt("ANYA-level", 1));

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

      String ability1 = section.getString("ability.SLOT_A.id");
      if (ability1 != null) {
        Ability ability = plugin.getAbilityManager().getAbility(ability1);
        if (ability != null && ability.getAbilityIconData() != null) {
          saveData.setAbility(AbilitySlot.SLOT_A, ability1);
        }
      }
      String ability2 = section.getString("ability.SLOT_B.id");
      if (ability2 != null) {
        Ability ability = plugin.getAbilityManager().getAbility(ability2);
        if (ability != null && ability.getAbilityIconData() != null) {
          saveData.setAbility(AbilitySlot.SLOT_B, ability2);
        }
      }
      String ability3 = section.getString("ability.SLOT_C.id");
      if (ability3 != null) {
        Ability ability = plugin.getAbilityManager().getAbility(ability3);
        if (ability != null && ability.getAbilityIconData() != null) {
          saveData.setAbility(AbilitySlot.SLOT_C, ability3);
        }
      }
      String ability4 = section.getString("ability.SLOT_D.id");
      if (ability4 != null) {
        Ability ability = plugin.getAbilityManager().getAbility(ability4);
        if (ability != null && ability.getAbilityIconData() != null) {
          saveData.setAbility(AbilitySlot.SLOT_D, ability4);
        }
      }

      for (String s : section.getStringList("lore-abilities")) {
        LoreAbility loreAbility = plugin.getLoreAbilityManager().getLoreAbilityFromId(s);
        if (loreAbility == null) {
          LogUtil.printError("LoreAbility " + s + " not found for player " + uuid);
          continue;
        }
        saveData.getBoundAbilities().add(s);
      }

      saveData.getCastMessages().put(AbilitySlot.SLOT_A, section.getStringList("ability.SLOT_C.msg"));
      saveData.getCastMessages().put(AbilitySlot.SLOT_B, section.getStringList("ability.SLOT_C.msg"));
      saveData.getCastMessages().put(AbilitySlot.SLOT_C, section.getStringList("ability.SLOT_C.msg"));
      saveData.getCastMessages().put(AbilitySlot.SLOT_D, section.getStringList("ability.SLOT_C.msg"));

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
        Bukkit.getLogger().warning("[Strife] No attribute section found for player " + uuid);
        for (StrifeAttribute s : plugin.getAttributeManager().getAttributes()) {
          saveData.setLevel(s, 0);
        }
      }
      saveData.setPvpScore((float) section.getDouble("pvp-score", 700));
      saveData.setPrayerPoints((float) section.getDouble("prayer", 0));
    }
    return saveData;
  }
}