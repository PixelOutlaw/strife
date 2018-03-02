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
import info.faceland.strife.data.ChampionSaveData;
import info.faceland.strife.data.EntityStatCache;
import info.faceland.strife.data.EntityStatData;
import info.faceland.strife.listeners.*;
import info.faceland.strife.managers.BarrierManager;
import info.faceland.strife.managers.BleedManager;
import info.faceland.strife.managers.MonsterManager;
import info.faceland.strife.managers.MultiplierManager;
import info.faceland.strife.managers.ChampionManager;
import info.faceland.strife.managers.StrifeExperienceManager;
import info.faceland.strife.managers.StrifeStatManager;
import info.faceland.strife.menus.LevelupMenu;
import info.faceland.strife.menus.StatsMenu;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.storage.DataStorage;
import info.faceland.strife.storage.JsonDataStorage;
import info.faceland.strife.tasks.AttackSpeedTask;
import info.faceland.strife.tasks.BarrierTask;
import info.faceland.strife.tasks.BleedTask;
import info.faceland.strife.tasks.BlockTask;
import info.faceland.strife.tasks.DarknessReductionTask;
import info.faceland.strife.tasks.HealthRegenTask;
import info.faceland.strife.tasks.SaveTask;
import info.faceland.strife.tasks.TrackedPruneTask;
import io.pixeloutlaw.minecraft.spigot.config.MasterConfiguration;
import io.pixeloutlaw.minecraft.spigot.config.VersionedConfiguration;
import io.pixeloutlaw.minecraft.spigot.config.VersionedSmartYamlConfiguration;
import ninja.amp.ampmenus.MenuListener;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.event.HandlerList;
import se.ranzdo.bukkit.methodcommand.CommandHandler;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class StrifePlugin extends FacePlugin {

    private PluginLogger debugPrinter;
    private VersionedSmartYamlConfiguration configYAML;
    private VersionedSmartYamlConfiguration statsYAML;
    private VersionedSmartYamlConfiguration baseStatsYAML;
    private StrifeStatManager statManager;
    private BarrierManager barrierManager;
    private BleedManager bleedManager;
    private MonsterManager monsterManager;
    private MultiplierManager multiplierManager;
    private DataStorage storage;
    private ChampionManager championManager;
    private StrifeExperienceManager experienceManager;
    private EntityStatCache entityStatCache;
    private SaveTask saveTask;
    private TrackedPruneTask trackedPruneTask;
    private HealthRegenTask regenTask;
    private BleedTask bleedTask;
    private BarrierTask barrierTask;
    private DarknessReductionTask darkTask;
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

    final private static long attackTickRate = 2L;

    @Override
    public void enable() {
        debugPrinter = new PluginLogger(this);
        baseStatsYAML = new VersionedSmartYamlConfiguration(new File(getDataFolder(), "base-entity-stats.yml"),
            getResource("base-entity-stats.yml"), VersionedConfiguration.VersionUpdateType.BACKUP_AND_UPDATE);
        statsYAML = new VersionedSmartYamlConfiguration(new File(getDataFolder(), "stats.yml"),
            getResource("stats.yml"), VersionedConfiguration.VersionUpdateType.BACKUP_AND_UPDATE);
        configYAML = new VersionedSmartYamlConfiguration(new File(getDataFolder(), "config.yml"),
            getResource("config.yml"), VersionedConfiguration.VersionUpdateType.BACKUP_AND_UPDATE);

        statManager = new StrifeStatManager();
        barrierManager = new BarrierManager();
        bleedManager = new BleedManager();
        monsterManager = new MonsterManager(this);
        multiplierManager = new MultiplierManager();
        storage = new JsonDataStorage(this);
        championManager = new ChampionManager(this);
        experienceManager = new StrifeExperienceManager(this);
        entityStatCache = new EntityStatCache(this);
        commandHandler = new CommandHandler(this);

        MenuListener.getInstance().register(this);

        if (baseStatsYAML.update()) {
            getLogger().info("Updating base-entity-stats.yml");
        }
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
            stat.setDescription(cs.getStringList("description"));
            stat.setDyeColor(DyeColor.valueOf(cs.getString("dye-color", "WHITE")));
            stat.setSlot(cs.getInt("slot"));
            stat.setStartCap(cs.getInt("starting-cap", 0));
            stat.setMaxCap(cs.getInt("maximum-cap", 100));
            stat.setLevelsToRaiseCap(cs.getInt("levels-to-raise-cap", -1));
            Map<String, Integer> baseStatRequirements = new HashMap<>();
            if (cs.isConfigurationSection("base-attribute-requirements")) {
                ConfigurationSection reqs = cs.getConfigurationSection("base-attribute-requirements");
                for (String k : reqs.getKeys(false)) {
                    baseStatRequirements.put(k, reqs.getInt(k));
                }
            }
            Map<String, Integer> raiseStatCapAttributes = new HashMap<>();
            if (cs.isConfigurationSection("attributes-to-raise-cap")) {
                ConfigurationSection raiseReqs = cs.getConfigurationSection("attributes-to-raise-cap");
                for (String k : raiseReqs.getKeys(false)) {
                    raiseStatCapAttributes.put(k, raiseReqs.getInt(k));
                }
            }
            Map<StrifeAttribute, Double> attributeMap = new HashMap<>();
            if (cs.isConfigurationSection("attributes")) {
                ConfigurationSection attrCS = cs.getConfigurationSection("attributes");
                for (String k : attrCS.getKeys(false)) {
                    StrifeAttribute attr = StrifeAttribute.valueOf(k);
                    attributeMap.put(attr, attrCS.getDouble(k));
                }
            }
            stat.setStatIncreaseIncrements(raiseStatCapAttributes);
            stat.setBaseStatRequirements(baseStatRequirements);
            stat.setAttributeMap(attributeMap);
            stats.add(stat);
            loadedStats.add(stat.getKey());
        }
        for (StrifeStat stat : stats) {
            getStatManager().addStat(stat);
        }
        debug(Level.INFO, "Loaded stats: " + loadedStats.toString());

        for (String entityKey : baseStatsYAML.getKeys(false)) {
            if (!baseStatsYAML.isConfigurationSection(entityKey)) {
                continue;
            }
            EntityType entityType = EntityType.valueOf(entityKey);
            ConfigurationSection cs = baseStatsYAML.getConfigurationSection(entityKey);
            EntityStatData data = new EntityStatData();
            if (cs.isConfigurationSection("base-values")) {
                ConfigurationSection attrCS = cs.getConfigurationSection("base-values");
                for (String k : attrCS.getKeys(false)) {
                    StrifeAttribute attr = StrifeAttribute.valueOf(k);
                    data.putBaseValue(attr, attrCS.getDouble(k));
                }
            }
            if (cs.isConfigurationSection("per-level")) {
                ConfigurationSection attrCS = cs.getConfigurationSection("per-level");
                for (String k : attrCS.getKeys(false)) {
                    StrifeAttribute attr = StrifeAttribute.valueOf(k);
                    data.putPerLevel(attr, attrCS.getDouble(k));
                }
            }
            if (cs.isConfigurationSection("per-bonus-level")) {
                ConfigurationSection attrCS = cs.getConfigurationSection("per-bonus-level");
                for (String k : attrCS.getKeys(false)) {
                    StrifeAttribute attr = StrifeAttribute.valueOf(k);
                    data.putPerBonusLevel(attr, attrCS.getDouble(k));
                }
            }
            getMonsterManager().addEntityData(entityType, data);
        }

        for (ChampionSaveData data : storage.load()) {
            championManager.addChampion(new Champion(data));
        }

        saveTask = new SaveTask(this);
        trackedPruneTask = new TrackedPruneTask(this);
        regenTask = new HealthRegenTask(this);
        bleedTask = new BleedTask(this);
        barrierTask = new BarrierTask(this);
        darkTask = new DarknessReductionTask();
        attackSpeedTask = new AttackSpeedTask(attackTickRate);
        blockTask = new BlockTask();

        commandHandler.registerCommands(new AttributesCommand(this));
        commandHandler.registerCommands(new LevelUpCommand(this));
        commandHandler.registerCommands(new StrifeCommand(this));

        levelingRate = new LevelingRate();
        Expression expr = new ExpressionBuilder(settings.getString("config.leveling.formula",
            "(5+(2*LEVEL)+(LEVEL^1.2))*LEVEL")).variable("LEVEL").build();
        for (int i = 0; i < 100; i++) {
            levelingRate.put(i, i, (int) Math.round(expr.setVariable("LEVEL", i).evaluate()));
        }

        trackedPruneTask.runTaskTimer(this,
            20L * 61, // Start save after 1 minute, 1 second cuz yolo
            20L * 60 // Run every 1 minute after that
        );
        saveTask.runTaskTimer(this,
            20L * 680, // Start save after 11 minutes, 20 seconds cuz yolo
            20L * 600 // Run every 10 minutes after that
        );
        regenTask.runTaskTimer(this,
            20L * 10, // Start timer after 10s
            20L * 2 // Run it every 2s after
        );
        bleedTask.runTaskTimer(this,
            20L * 10, // Start timer after 10s
           4L // Run it every 1/5th of a second after
        );
        barrierTask.runTaskTimer(this,
            201L, // Start timer after 10.05s
            4L // Run it every 1/5th of a second after
        );
        darkTask.runTaskTimer(this,
            20L * 10, // Start timer after 10s
            10L  // Run it every 0.5s after
        );
        attackSpeedTask.runTaskTimer(this, 5L, attackTickRate);
        blockTask.runTaskTimer(this, 5L, 5L);
        Bukkit.getPluginManager().registerEvents(new ExperienceListener(this), this);
        Bukkit.getPluginManager().registerEvents(new HealthListener(), this);
        Bukkit.getPluginManager().registerEvents(new CombatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new DOTListener(this), this);
        Bukkit.getPluginManager().registerEvents(new WandListener(this), this);
        Bukkit.getPluginManager().registerEvents(new BowListener(this), this);
        Bukkit.getPluginManager().registerEvents(new HeadDropListener(this), this);
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
        trackedPruneTask.cancel();
        regenTask.cancel();
        bleedTask.cancel();
        barrierTask.cancel();
        darkTask.cancel();
        HandlerList.unregisterAll(this);
        storage.save(championManager.getChampionSaveData());
        configYAML = null;
        baseStatsYAML = null;
        statsYAML = null;
        statManager = null;
        monsterManager = null;
        bleedManager = null;
        barrierManager = null;
        multiplierManager = null;
        storage = null;
        championManager = null;
        experienceManager = null;
        entityStatCache = null;
        saveTask = null;
        trackedPruneTask = null;
        regenTask = null;
        bleedTask = null;
        bleedTask = null;
        darkTask = null;
        commandHandler = null;
        settings = null;
    }

    public StrifeStatManager getStatManager() {
        return statManager;
    }

    public BarrierManager getBarrierManager() {
      return barrierManager;
    }

    public BleedManager getBleedManager() {
    return bleedManager;
  }

    public MonsterManager getMonsterManager() {
        return monsterManager;
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

    public StrifeExperienceManager getExpManager() {
        return experienceManager;
    }

    public EntityStatCache getEntityStatCache() {
        return entityStatCache;
    }

    public MasterConfiguration getSettings() {
        return settings;
    }

    public LevelupMenu getLevelupMenu() {
        return levelupMenu;
    }

    public StatsMenu getStatsMenu() { return statsMenu; }
}
