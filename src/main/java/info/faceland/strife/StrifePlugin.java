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
package info.faceland.strife;

import com.comphenix.xp.lookup.LevelingRate;
import com.tealcube.minecraft.bukkit.facecore.logging.PluginLogger;
import com.tealcube.minecraft.bukkit.facecore.plugin.FacePlugin;
import com.tealcube.minecraft.bukkit.shade.objecthunter.exp4j.Expression;
import com.tealcube.minecraft.bukkit.shade.objecthunter.exp4j.ExpressionBuilder;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.commands.AttributesCommand;
import info.faceland.strife.commands.LevelUpCommand;
import info.faceland.strife.commands.StrifeCommand;
import info.faceland.strife.data.Champion;
import info.faceland.strife.listeners.*;
import info.faceland.strife.managers.MultiplierManager;
import info.faceland.strife.managers.ChampionManager;
import info.faceland.strife.managers.StrifeStatManager;
import info.faceland.strife.menus.LevelupMenu;
import info.faceland.strife.menus.StatsMenu;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.storage.DataStorage;
import info.faceland.strife.storage.JsonDataStorage;
import info.faceland.strife.tasks.AttackSpeedTask;
import info.faceland.strife.tasks.BlockTask;
import info.faceland.strife.tasks.HealthRegenTask;
import info.faceland.strife.tasks.SaveTask;
import io.pixeloutlaw.minecraft.spigot.config.MasterConfiguration;
import io.pixeloutlaw.minecraft.spigot.config.VersionedConfiguration;
import io.pixeloutlaw.minecraft.spigot.config.VersionedSmartYamlConfiguration;
import ninja.amp.ampmenus.MenuListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.HandlerList;
import se.ranzdo.bukkit.methodcommand.CommandHandler;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class StrifePlugin extends FacePlugin {

    private PluginLogger debugPrinter;
    private VersionedSmartYamlConfiguration configYAML;
    private VersionedSmartYamlConfiguration statsYAML;
    private StrifeStatManager statManager;
    private MultiplierManager multiplierManager;
    private DataStorage storage;
    private ChampionManager championManager;
    private SaveTask saveTask;
    private HealthRegenTask regenTask;
    private AttackSpeedTask attackSpeedTask;
    private BlockTask blockTask;
    private CommandHandler commandHandler;
    private MasterConfiguration settings;
    private LevelingRate levelingRate;
    private LevelupMenu levelupMenu;
    private StatsMenu statsMenu;

    public LevelingRate getLevelingRate() {
        return levelingRate;
    }

    public AttackSpeedTask getAttackSpeedTask() {
        return attackSpeedTask;
    }
    public BlockTask getBlockTask() {
        return blockTask;
    }

    @Override
    public void enable() {
        debugPrinter = new PluginLogger(this);
        statsYAML = new VersionedSmartYamlConfiguration(new File(getDataFolder(), "stats.yml"),
                                                        getResource("stats.yml"),
                                                        VersionedConfiguration.VersionUpdateType.BACKUP_AND_UPDATE);
        configYAML = new VersionedSmartYamlConfiguration(new File(getDataFolder(), "config.yml"),
                                                         getResource("config.yml"),
                                                         VersionedConfiguration.VersionUpdateType.BACKUP_AND_UPDATE);

        statManager = new StrifeStatManager();

        multiplierManager = new MultiplierManager();

        storage = new JsonDataStorage(this);

        championManager = new ChampionManager(this);

        commandHandler = new CommandHandler(this);

        MenuListener.getInstance().register(this);

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
        regenTask = new HealthRegenTask(this);
        attackSpeedTask = new AttackSpeedTask();
        blockTask = new BlockTask();

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

        saveTask.runTaskTimer(this,
                20L * 660, // Start save after 11 minutes
                20L * 600 // Run every 10 minutes after that
        );
        regenTask.runTaskTimer(this,
                20L * 10, // Start timer after 10s
                20L * 2 // Run it every 2 seconds after //
        );
        attackSpeedTask.runTaskTimer(this, 5L, 5L);
        blockTask.runTaskTimer(this, 5L, 5L);
        Bukkit.getPluginManager().registerEvents(new ExperienceListener(this), this);
        Bukkit.getPluginManager().registerEvents(new HealthListener(), this);
        Bukkit.getPluginManager().registerEvents(new CombatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new DataListener(this), this);
        Bukkit.getPluginManager().registerEvents(new AttributeUpdateListener(this), this);
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
        regenTask.cancel();
        HandlerList.unregisterAll(this);
        storage.save(championManager.getChampions());
        configYAML = null;
        statsYAML = null;
        statManager = null;
        multiplierManager = null;
        storage = null;
        championManager = null;
        saveTask = null;
        regenTask = null;
        commandHandler = null;
        settings = null;
    }

    public StrifeStatManager getStatManager() {
        return statManager;
    }

    public MultiplierManager getMultiplierManager() {
        return multiplierManager;
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

    public LevelupMenu getLevelupMenu() {
        return levelupMenu;
    }

    public StatsMenu getStatsMenu() { return statsMenu; }
}
