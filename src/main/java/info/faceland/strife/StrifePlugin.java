/*
 * This file is part of Strife, licensed under the ISC License.
 *
 * Copyright (c) 2014 Richard Harrah
 *
 * Permission to use, copy, modify, and/or distribute this software for any purpose with or without fee is hereby granted,
 * provided that the above copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS SOFTWARE INCLUDING ALL
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT,
 * INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF
 * THIS SOFTWARE.
 */
package info.faceland.strife;

import com.comphenix.xp.lookup.LevelingRate;
import com.tealcube.minecraft.bukkit.facecore.logging.PluginLogger;
import com.tealcube.minecraft.bukkit.facecore.plugin.FacePlugin;
import com.tealcube.minecraft.bukkit.facecore.shade.amp.ampmenus.MenuListener;
import com.tealcube.minecraft.bukkit.facecore.shade.config.MasterConfiguration;
import com.tealcube.minecraft.bukkit.facecore.shade.config.VersionedSmartConfiguration;
import com.tealcube.minecraft.bukkit.facecore.shade.config.VersionedSmartYamlConfiguration;
import com.tealcube.minecraft.bukkit.kern.methodcommand.CommandHandler;
import com.tealcube.minecraft.bukkit.kern.objecthunter.exp4j.Expression;
import com.tealcube.minecraft.bukkit.kern.objecthunter.exp4j.ExpressionBuilder;

import info.faceland.beast.BeastPlugin;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.commands.AttributesCommand;
import info.faceland.strife.commands.LevelUpCommand;
import info.faceland.strife.commands.StrifeCommand;
import info.faceland.strife.data.Champion;
import info.faceland.strife.listeners.BullionListener;
import info.faceland.strife.listeners.CombatListener;
import info.faceland.strife.listeners.DataListener;
import info.faceland.strife.listeners.ExperienceListener;
import info.faceland.strife.listeners.HealthListener;
import info.faceland.strife.listeners.LootListener;
import info.faceland.strife.managers.ChampionManager;
import info.faceland.strife.managers.StrifeStatManager;
import info.faceland.strife.menus.LevelupMenu;
import info.faceland.strife.menus.StatsMenu;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.storage.DataStorage;
import info.faceland.strife.storage.JsonDataStorage;
import info.faceland.strife.tasks.AttackSpeedTask;
import info.faceland.strife.tasks.HealthMovementTask;
import info.faceland.strife.tasks.SaveTask;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.HandlerList;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class StrifePlugin extends FacePlugin {

    private PluginLogger debugPrinter;
    private VersionedSmartYamlConfiguration configYAML;
    private VersionedSmartYamlConfiguration statsYAML;
    private StrifeStatManager statManager;
    private DataStorage storage;
    private ChampionManager championManager;
    private SaveTask saveTask;
    private AttackSpeedTask attackSpeedTask;
    private CommandHandler commandHandler;
    private MasterConfiguration settings;
    private LevelingRate levelingRate;
    private BeastPlugin beastPlugin;
    private HealthMovementTask healthMovementTask;
    private LevelupMenu levelupMenu;
    private StatsMenu statsMenu;

    public LevelingRate getLevelingRate() {
        return levelingRate;
    }

    public AttackSpeedTask getAttackSpeedTask() {
        return attackSpeedTask;
    }

    @Override
    public void enable() {
        debugPrinter = new PluginLogger(this);
        statsYAML = new VersionedSmartYamlConfiguration(new File(getDataFolder(), "stats.yml"),
                                                        getResource("stats.yml"),
                                                        VersionedSmartConfiguration.VersionUpdateType.BACKUP_AND_UPDATE);
        configYAML = new VersionedSmartYamlConfiguration(new File(getDataFolder(), "config.yml"),
                                                         getResource("config.yml"),
                                                         VersionedSmartConfiguration.VersionUpdateType.BACKUP_AND_UPDATE);

        statManager = new StrifeStatManager();

        storage = new JsonDataStorage(this);

        championManager = new ChampionManager();

        commandHandler = new CommandHandler(this);

        MenuListener.getInstance().register(this);

        beastPlugin = (BeastPlugin) Bukkit.getPluginManager().getPlugin("Beast");

        if (statsYAML.update()) {
            getLogger().info("Updating stats.yml");
        }
        if (configYAML.update()) {
            getLogger().info("Updating config.yml");
        }

        settings = MasterConfiguration.loadFromFiles(configYAML);

        List<StrifeStat> stats = new ArrayList<>();
        List<String> loadedStats = new ArrayList<>();
        for (String key : statsYAML.getKeys(false)) {
            if (!statsYAML.isConfigurationSection(key)) {
                continue;
            }
            ConfigurationSection cs = statsYAML.getConfigurationSection(key);
            StrifeStat stat = new StrifeStat(key);
            stat.setName(cs.getString("name"));
            stat.setOrder(cs.getInt("order"));
            stat.setDescription(cs.getString("description"));
            stat.setDyeColor(DyeColor.valueOf(cs.getString("dye-color", "WHITE")));
            stat.setChatColor(ChatColor.valueOf(cs.getString("chat-color", "WHITE")));
            stat.setMenuX(cs.getInt("menu-x"));
            stat.setMenuY(cs.getInt("menu-y"));
            Map<StrifeAttribute, Double> attributeMap = new HashMap<>();
            if (cs.isConfigurationSection("attributes")) {
                ConfigurationSection attrCS = cs.getConfigurationSection("attributes");
                for (String k : attrCS.getKeys(false)) {
                    StrifeAttribute attr = StrifeAttribute.fromName(k);
                    if (attr == null) {
                        continue;
                    }
                    attributeMap.put(attr, attrCS.getDouble(k));
                }
            }
            stat.setAttributeMap(attributeMap);
            stats.add(stat);
            loadedStats.add(stat.getKey());
        }
        for (StrifeStat stat : stats) {
            getStatManager().addStat(stat);
        }
        debug(Level.INFO, "Loaded stats: " + loadedStats.toString());

        for (Champion champ : storage.load()) {
            championManager.addChampion(champ);
        }

        saveTask = new SaveTask(this);
        attackSpeedTask = new AttackSpeedTask();
        healthMovementTask = new HealthMovementTask(this);

        commandHandler.registerCommands(new AttributesCommand(this));
        commandHandler.registerCommands(new LevelUpCommand(this));
        commandHandler.registerCommands(new StrifeCommand(this));

        levelingRate = new LevelingRate();
        Expression expr = new ExpressionBuilder(settings.getString("config.leveling.formula",
                                                                   "(5+(2*LEVEL)+(LEVEL^1.2))*LEVEL")).variable("LEVEL")
            .build();
        for (int i = 0; i < 100; i++) {
            levelingRate.put(i, i, (int) Math.round(expr.setVariable("LEVEL", i).evaluate()));
        }

        saveTask.runTaskTimer(this, 20L * 600, 20L * 600);
        attackSpeedTask.runTaskTimer(this, 5L, 5L);
        healthMovementTask.runTaskTimer(this, 20L * 10, 20L * 10);
        Bukkit.getPluginManager().registerEvents(new ExperienceListener(this), this);
        Bukkit.getPluginManager().registerEvents(new HealthListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CombatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new DataListener(this), this);
        if (Bukkit.getPluginManager().getPlugin("Loot") != null) {
            Bukkit.getPluginManager().registerEvents(new LootListener(this), this);
        }
        if (Bukkit.getPluginManager().getPlugin("Bullion") != null) {
            Bukkit.getPluginManager().registerEvents(new BullionListener(this), this);
        }

        levelupMenu = new LevelupMenu(this, getStatManager().getStats());
        statsMenu = new StatsMenu(this);
        debug(Level.INFO, "v" + getDescription().getVersion() + " enabled");
    }

    @Override
    public void disable() {
        debug(Level.INFO, "v" + getDescription().getVersion() + " disabled");
        saveTask.cancel();
        HandlerList.unregisterAll(this);
        storage.save(championManager.getChampions());
        configYAML = null;
        statsYAML = null;
        statManager = null;
        storage = null;
        championManager = null;
        saveTask = null;
        commandHandler = null;
        settings = null;
    }

    public StrifeStatManager getStatManager() {
        return statManager;
    }

    public void debug(Level level, String... messages) {
        if (debugPrinter != null) {
            debugPrinter.log(level, Arrays.asList(messages));
        }
    }

    public DataStorage getStorage() {
        return storage;
    }

    public ChampionManager getChampionManager() {
        return championManager;
    }

    public MasterConfiguration getSettings() {
        return settings;
    }

    public BeastPlugin getBeastPlugin() {
        return beastPlugin;
    }

    public LevelupMenu getLevelupMenu() {
        return levelupMenu;
    }

    public StatsMenu getStatsMenu() { return statsMenu; }
}
