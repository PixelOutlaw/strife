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
package info.faceland.strife;

import com.comphenix.xp.lookup.LevelingRate;
import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.facecore.logging.PluginLogger;
import com.tealcube.minecraft.bukkit.facecore.plugin.FacePlugin;
import com.tealcube.minecraft.bukkit.shade.objecthunter.exp4j.Expression;
import com.tealcube.minecraft.bukkit.shade.objecthunter.exp4j.ExpressionBuilder;
import info.faceland.strife.api.StrifeExperienceManager;
import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.commands.AttributesCommand;
import info.faceland.strife.commands.LevelUpCommand;
import info.faceland.strife.commands.SpawnerCommand;
import info.faceland.strife.commands.StrifeCommand;
import info.faceland.strife.commands.UniqueEntityCommand;
import info.faceland.strife.data.*;
import info.faceland.strife.data.EntityAbilitySet.AbilityType;
import info.faceland.strife.listeners.*;
import info.faceland.strife.listeners.combat.BowListener;
import info.faceland.strife.listeners.combat.CombatListener;
import info.faceland.strife.listeners.combat.DOTListener;
import info.faceland.strife.listeners.DataListener;
import info.faceland.strife.listeners.combat.DogeListener;
import info.faceland.strife.listeners.combat.SwingListener;
import info.faceland.strife.managers.*;
import info.faceland.strife.menus.LevelupMenu;
import info.faceland.strife.menus.StatsMenu;
import info.faceland.strife.storage.DataStorage;
import info.faceland.strife.storage.JsonDataStorage;
import info.faceland.strife.tasks.*;
import info.faceland.strife.util.LogUtil;
import info.faceland.strife.util.LogUtil.LogLevel;
import io.pixeloutlaw.minecraft.spigot.config.MasterConfiguration;
import io.pixeloutlaw.minecraft.spigot.config.SmartYamlConfiguration;
import io.pixeloutlaw.minecraft.spigot.config.VersionedConfiguration;
import io.pixeloutlaw.minecraft.spigot.config.VersionedSmartYamlConfiguration;
import ninja.amp.ampmenus.MenuListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import se.ranzdo.bukkit.methodcommand.CommandHandler;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class StrifePlugin extends FacePlugin {

  private static StrifePlugin instance;

  private PluginLogger debugPrinter;
  private LogLevel logLevel;
  private CommandHandler commandHandler;
  private MasterConfiguration settings;
  private VersionedSmartYamlConfiguration configYAML;
  private VersionedSmartYamlConfiguration statsYAML;
  private VersionedSmartYamlConfiguration baseStatsYAML;
  private VersionedSmartYamlConfiguration uniqueEnemiesYAML;
  private VersionedSmartYamlConfiguration equipmentYAML;
  private VersionedSmartYamlConfiguration effectYAML;
  private VersionedSmartYamlConfiguration abilityYAML;
  private SmartYamlConfiguration spawnerYAML;

  private AttributeUpdateManager attributeUpdateManager;
  private StrifeStatManager statManager;
  private ChampionManager championManager;
  private StrifeExperienceManager experienceManager;
  private CraftExperienceManager craftExperienceManager;
  private EnchantExperienceManager enchantExperienceManager;
  private FishExperienceManager fishExperienceManager;
  private MiningExperienceManager miningExperienceManager;
  private AttackSpeedManager attackSpeedManager;
  private BlockManager blockManager;
  private BarrierManager barrierManager;
  private BleedManager bleedManager;
  private DarknessManager darknessManager;
  private RageManager rageManager;
  private MonsterManager monsterManager;
  private UniqueEntityManager uniqueEntityManager;
  private BossBarManager bossBarManager;
  private EntityEquipmentManager equipmentManager;
  private EffectManager effectManager;
  private AbilityManager abilityManager;
  private SpawnerManager spawnerManager;
  private MultiplierManager multiplierManager;

  private DataStorage storage;
  private EntityStatCache entityStatCache;
  private SaveTask saveTask;
  private TrackedPruneTask trackedPruneTask;
  private HealthRegenTask regenTask;
  private BleedTask bleedTask;
  private BarrierTask barrierTask;
  private TickBossBarsTask tickBossBarsTask;
  private PruneBossBarsTask pruneBossBarsTask;
  private DarknessReductionTask darkTask;
  private RageTask rageTask;
  private UniquePruneTask uniquePruneTask;
  private UniqueParticleTask uniqueParticleTask;
  private TimedAbilityTask timedAbilityTask;
  private SpawnerSpawnTask spawnerSpawnTask;
  private SpawnerLeashTask spawnerLeashTask;

  private LevelingRate levelingRate;
  private LevelingRate craftingRate;
  private LevelingRate enchantRate;
  private LevelingRate fishRate;
  private LevelingRate miningRate;
  private LevelupMenu levelupMenu;
  private StatsMenu statsMenu;

  private int maxSkillLevel;

  private static void setInstance(StrifePlugin plugin) {
    instance = plugin;
  }

  public static StrifePlugin getInstance() {
    return instance;
  }

  @Override
  public void enable() {
    setInstance(this);
    debugPrinter = new PluginLogger(this);
    configYAML = new VersionedSmartYamlConfiguration(new File(getDataFolder(), "config.yml"),
        getResource("config.yml"), VersionedConfiguration.VersionUpdateType.BACKUP_AND_UPDATE);
    statsYAML = new VersionedSmartYamlConfiguration(new File(getDataFolder(), "stats.yml"),
        getResource("stats.yml"), VersionedConfiguration.VersionUpdateType.BACKUP_AND_UPDATE);
    baseStatsYAML = new VersionedSmartYamlConfiguration(
        new File(getDataFolder(), "base-entity-stats.yml"),
        getResource("base-entity-stats.yml"),
        VersionedConfiguration.VersionUpdateType.BACKUP_AND_UPDATE);
    uniqueEnemiesYAML = new VersionedSmartYamlConfiguration(
        new File(getDataFolder(), "unique-enemies.yml"),
        getResource("unique-enemies.yml"),
        VersionedConfiguration.VersionUpdateType.BACKUP_AND_UPDATE);
    equipmentYAML = new VersionedSmartYamlConfiguration(new File(getDataFolder(), "equipment.yml"),
        getResource("equipment.yml"), VersionedConfiguration.VersionUpdateType.BACKUP_AND_UPDATE);
    effectYAML = new VersionedSmartYamlConfiguration(new File(getDataFolder(), "effects.yml"),
        getResource("effects.yml"), VersionedConfiguration.VersionUpdateType.BACKUP_AND_UPDATE);
    abilityYAML = new VersionedSmartYamlConfiguration(new File(getDataFolder(), "abilities.yml"),
        getResource("abilities.yml"), VersionedConfiguration.VersionUpdateType.BACKUP_AND_UPDATE);
    spawnerYAML = new SmartYamlConfiguration(new File(getDataFolder(), "spawners.yml"));

    if (configYAML.update()) {
      getLogger().info("Updating config.yml");
    }
    if (statsYAML.update()) {
      getLogger().info("Updating stats.yml");
    }
    if (baseStatsYAML.update()) {
      getLogger().info("Updating base-entity-stats.yml");
    }
    if (uniqueEnemiesYAML.update()) {
      getLogger().info("Updating unique-enemies.yml");
    }
    if (equipmentYAML.update()) {
      getLogger().info("Updating equipment.yml");
    }
    if (effectYAML.update()) {
      getLogger().info("Updating effects.yml");
    }
    if (abilityYAML.update()) {
      getLogger().info("Updating abilities.yml");
    }
    settings = MasterConfiguration.loadFromFiles(configYAML);

    statManager = new StrifeStatManager();
    blockManager = new BlockManager();
    bleedManager = new BleedManager();
    darknessManager = new DarknessManager();
    attributeUpdateManager = new AttributeUpdateManager();
    rageManager = new RageManager(attributeUpdateManager);
    barrierManager = new BarrierManager(attributeUpdateManager);
    monsterManager = new MonsterManager(this);
    uniqueEntityManager = new UniqueEntityManager(this);
    bossBarManager = new BossBarManager(this);
    equipmentManager = new EntityEquipmentManager();
    effectManager = new EffectManager();
    abilityManager = new AbilityManager(effectManager, uniqueEntityManager);
    attackSpeedManager = new AttackSpeedManager();
    spawnerManager = new SpawnerManager(uniqueEntityManager);
    multiplierManager = new MultiplierManager();
    storage = new JsonDataStorage(this);
    championManager = new ChampionManager(this);
    experienceManager = new ExperienceManager(this);
    craftExperienceManager = new CraftExperienceManager(this);
    enchantExperienceManager = new EnchantExperienceManager(this);
    fishExperienceManager = new FishExperienceManager(this);
    miningExperienceManager = new MiningExperienceManager(this);
    entityStatCache = new EntityStatCache(barrierManager, monsterManager);
    commandHandler = new CommandHandler(this);

    MenuListener.getInstance().register(this);

    try {
      logLevel = LogLevel.valueOf(settings.getString("config.log-level", "ERROR"));
    } catch (Exception e) {
      logLevel = LogLevel.ERROR;
      LogUtil.printError("You don't have an acceptable log level you dangus");
    }

    buildEquipment();
    buildLevelpointStats();
    buildBaseStats();

    buildEffects();
    buildAbilities();

    buildUniqueEnemies();
    loadSpawners();

    for (Player player : Bukkit.getOnlinePlayers()) {
      ChampionSaveData saveData = storage.load(player.getUniqueId());
      championManager.addChampion(new Champion(player, saveData));
    }

    saveTask = new SaveTask(this);
    trackedPruneTask = new TrackedPruneTask(this);
    regenTask = new HealthRegenTask(this);
    bleedTask = new BleedTask(bleedManager, barrierManager,
        settings.getDouble("config.mechanics.base-bleed-damage", 1D),
        settings.getDouble("config.mechanics.percent-bleed-damage", 0.1D));
    barrierTask = new BarrierTask(this);
    tickBossBarsTask = new TickBossBarsTask(bossBarManager);
    pruneBossBarsTask = new PruneBossBarsTask(bossBarManager);
    darkTask = new DarknessReductionTask(darknessManager);
    rageTask = new RageTask(rageManager, entityStatCache);
    uniquePruneTask = new UniquePruneTask(this);
    uniqueParticleTask = new UniqueParticleTask(uniqueEntityManager);
    spawnerLeashTask = new SpawnerLeashTask(spawnerManager);
    spawnerSpawnTask = new SpawnerSpawnTask(spawnerManager);
    timedAbilityTask = new TimedAbilityTask(abilityManager, uniqueEntityManager, entityStatCache);

    commandHandler.registerCommands(new AttributesCommand(this));
    commandHandler.registerCommands(new LevelUpCommand(this));
    commandHandler.registerCommands(new StrifeCommand(this));
    commandHandler.registerCommands(new UniqueEntityCommand(this));
    commandHandler.registerCommands(new SpawnerCommand(this));

    levelingRate = new LevelingRate();
    maxSkillLevel = settings.getInt("config.leveling.max-skill-level", 60);

    Expression normalExpr = new ExpressionBuilder(settings.getString("config.leveling.formula",
        "(5+(2*LEVEL)+(LEVEL^1.2))*LEVEL")).variable("LEVEL").build();
    for (int i = 0; i < 200; i++) {
      levelingRate.put(i, i, (int) Math.round(normalExpr.setVariable("LEVEL", i).evaluate()));
    }

    craftingRate = new LevelingRate();
    Expression craftExpr = new ExpressionBuilder(settings.getString("config.leveling.crafting",
        "(5+(2*LEVEL)+(LEVEL^1.2))*LEVEL")).variable("LEVEL").build();
    for (int i = 0; i < maxSkillLevel; i++) {
      craftingRate.put(i, i, (int) Math.round(craftExpr.setVariable("LEVEL", i).evaluate()));
    }

    enchantRate = new LevelingRate();
    Expression enchantExpr = new ExpressionBuilder(settings.getString("config.leveling.enchanting",
        "(5+(2*LEVEL)+(LEVEL^1.2))*LEVEL")).variable("LEVEL").build();
    for (int i = 0; i < maxSkillLevel; i++) {
      enchantRate.put(i, i, (int) Math.round(enchantExpr.setVariable("LEVEL", i).evaluate()));
    }

    fishRate = new LevelingRate();
    Expression fishExpr = new ExpressionBuilder(settings.getString("config.leveling.fishing",
        "(5+(2*LEVEL)+(LEVEL^1.2))*LEVEL")).variable("LEVEL").build();
    for (int i = 0; i < maxSkillLevel; i++) {
      fishRate.put(i, i, (int) Math.round(fishExpr.setVariable("LEVEL", i).evaluate()));
    }

    miningRate = new LevelingRate();
    Expression mineExpr = new ExpressionBuilder(settings.getString("config.leveling.mining",
        "(5+(2*LEVEL)+(LEVEL^1.2))*LEVEL")).variable("LEVEL").build();
    for (int i = 0; i < maxSkillLevel; i++) {
      miningRate.put(i, i, (int) Math.round(mineExpr.setVariable("LEVEL", i).evaluate()));
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
        20L * 9, // Start timer after 9s
        20L * 2 // Run it every 2s after
    );
    bleedTask.runTaskTimer(this,
        20L * 10, // Start timer after 10s
        settings.getLong("config.mechanics.ticks-per-bleed", 10L)
    );
    barrierTask.runTaskTimer(this,
        2201L, // Start timer after 11s
        4L // Run it every 1/5th of a second after
    );
    tickBossBarsTask.runTaskTimer(this,
        240L, // Start timer after 12s
        2L // Run it every 1/10th of a second after
    );
    pruneBossBarsTask.runTaskTimer(this,
        20L * 13, // Start timer after 13s
        20L * 60 * 7 // Run it every 7 minutes
    );
    darkTask.runTaskTimer(this,
        20L * 14, // Start timer after 14s
        10L  // Run it every 0.5s after
    );
    rageTask.runTaskTimer(this,
        20L * 10, // Start timer after 10s
        5L  // Run it every 0.25s after
    );
    uniquePruneTask.runTaskTimer(this,
        30 * 20L,
        30 * 20L
    );
    uniqueParticleTask.runTaskTimer(this,
        20 * 20L,
        2L
    );
    spawnerSpawnTask.runTaskTimer(this,
        20 * 20L, // Start timer after 20s
        6 * 20L // Run it every 6 seconds
    );
    timedAbilityTask.runTaskTimer(this,
        20 * 20L, // Start timer after 20s
        2 * 20L // Run it every 2 seconds
    );
    spawnerLeashTask.runTaskTimer(this,
        20 * 20L, // Start timer after 20s
        5 * 20L // Run it every 5s
    );
    Bukkit.getPluginManager().registerEvents(new EndermanListener(), this);
    Bukkit.getPluginManager().registerEvents(new ExperienceListener(this), this);
    Bukkit.getPluginManager().registerEvents(new HealthListener(), this);
    Bukkit.getPluginManager().registerEvents(new CombatListener(this), this);
    Bukkit.getPluginManager().registerEvents(
        new UniqueSplashListener(entityStatCache, blockManager, effectManager), this);
    Bukkit.getPluginManager().registerEvents(new DOTListener(this), this);
    Bukkit.getPluginManager().registerEvents(new SwingListener(this), this);
    Bukkit.getPluginManager().registerEvents(new BowListener(this), this);
    Bukkit.getPluginManager().registerEvents(new HeadDropListener(this), this);
    Bukkit.getPluginManager().registerEvents(new DataListener(this), this);
    Bukkit.getPluginManager().registerEvents(new SkillLevelUpListener(settings), this);
    Bukkit.getPluginManager().registerEvents(new AttributeUpdateListener(this), this);
    Bukkit.getPluginManager().registerEvents(new EntityMagicListener(), this);
    Bukkit.getPluginManager().registerEvents(new SpawnListener(this), this);
    Bukkit.getPluginManager().registerEvents(new SummonListener(uniqueEntityManager), this);
    Bukkit.getPluginManager().registerEvents(new TargetingListener(uniqueEntityManager), this);
    Bukkit.getPluginManager().registerEvents(new FallListener(), this);
    Bukkit.getPluginManager().registerEvents(new DogeListener(entityStatCache), this);
    if (Bukkit.getPluginManager().getPlugin("Bullion") != null) {
      Bukkit.getPluginManager().registerEvents(new BullionListener(this), this);
    }

    levelupMenu = new LevelupMenu(this, getStatManager().getStats());
    statsMenu = new StatsMenu(this);

    LogUtil.printInfo("+===================================+");
    LogUtil.printInfo("Successfully enabled Strife-v" + getDescription().getVersion());
    LogUtil.printInfo("+===================================+");
  }

  @Override
  public void disable() {
    saveSpawners();
    storage.saveAll();
    uniqueEntityManager.killAllSpawnedUniques();
    HandlerList.unregisterAll(this);

    saveTask.cancel();
    trackedPruneTask.cancel();
    regenTask.cancel();
    bleedTask.cancel();
    barrierTask.cancel();
    tickBossBarsTask.cancel();
    pruneBossBarsTask.cancel();
    darkTask.cancel();
    rageTask.cancel();
    uniqueParticleTask.cancel();
    uniquePruneTask.cancel();
    spawnerLeashTask.cancel();
    spawnerSpawnTask.cancel();
    timedAbilityTask.cancel();

    configYAML = null;
    baseStatsYAML = null;
    statsYAML = null;

    craftingRate = null;
    enchantRate = null;
    fishRate = null;
    miningRate = null;

    attributeUpdateManager = null;
    statManager = null;
    monsterManager = null;
    uniqueEntityManager = null;
    bossBarManager = null;
    equipmentManager = null;
    effectManager = null;
    abilityManager = null;
    blockManager = null;
    bleedManager = null;
    darknessManager = null;
    rageManager = null;
    barrierManager = null;
    multiplierManager = null;
    spawnerManager = null;

    storage = null;
    championManager = null;
    experienceManager = null;
    craftExperienceManager = null;
    enchantExperienceManager = null;
    fishExperienceManager = null;
    miningExperienceManager = null;
    entityStatCache = null;

    saveTask = null;
    trackedPruneTask = null;
    pruneBossBarsTask = null;
    tickBossBarsTask = null;
    regenTask = null;
    bleedTask = null;
    darkTask = null;
    rageTask = null;
    attackSpeedManager = null;
    uniqueParticleTask = null;
    uniquePruneTask = null;
    timedAbilityTask = null;
    spawnerSpawnTask = null;
    spawnerLeashTask = null;

    commandHandler = null;
    settings = null;

    LogUtil.printInfo("+===================================+");
    LogUtil.printInfo("Successfully disabled Strife-v" + getDescription().getVersion());
    LogUtil.printInfo("+===================================+");
  }

  private void buildAbilities() {
    for (String key : abilityYAML.getKeys(false)) {
      if (!abilityYAML.isConfigurationSection(key)) {
        continue;
      }
      ConfigurationSection cs = abilityYAML.getConfigurationSection(key);
      abilityManager.loadAbility(key, cs);
    }
    LogUtil.printDebug(
        "Loaded abilities: " + abilityManager.getLoadedAbilities().entrySet().toString());
  }

  private void buildEffects() {
    for (String key : effectYAML.getKeys(false)) {
      if (!effectYAML.isConfigurationSection(key)) {
        continue;
      }
      ConfigurationSection cs = effectYAML.getConfigurationSection(key);
      effectManager.loadEffect(key, cs);
    }
    LogUtil.printDebug("Loaded effects: " + effectManager.getLoadedEffects().entrySet().toString());
  }

  private void buildEquipment() {
    for (String itemStackKey : equipmentYAML.getKeys(false)) {
      if (!equipmentYAML.isConfigurationSection(itemStackKey)) {
        continue;
      }
      ConfigurationSection cs = equipmentYAML.getConfigurationSection(itemStackKey);
      equipmentManager.loadEquipmentItem(itemStackKey, cs);
    }
    LogUtil.printDebug("Loaded equipments: " + equipmentManager.getItemMap().entrySet().toString());
  }

  private void buildLevelpointStats() {
    for (String key : statsYAML.getKeys(false)) {
      if (!statsYAML.isConfigurationSection(key)) {
        continue;
      }
      ConfigurationSection cs = statsYAML.getConfigurationSection(key);
      statManager.loadStat(key, cs);
    }
    LogUtil.printDebug("Loaded levelpoints: " + statManager.getStats().toString());
  }

  private void buildBaseStats() {
    for (String entityKey : baseStatsYAML.getKeys(false)) {
      if (!baseStatsYAML.isConfigurationSection(entityKey)) {
        continue;
      }
      ConfigurationSection cs = baseStatsYAML.getConfigurationSection(entityKey);
      monsterManager.loadBaseStats(entityKey, cs);
    }
  }

  private void buildUniqueEnemies() {
    for (String entityNameKey : uniqueEnemiesYAML.getKeys(false)) {
      if (!uniqueEnemiesYAML.isConfigurationSection(entityNameKey)) {
        continue;
      }
      ConfigurationSection cs = uniqueEnemiesYAML.getConfigurationSection(entityNameKey);

      UniqueEntity uniqueEntity = new UniqueEntity();

      String type = cs.getString("type");
      try {
        uniqueEntity.setType(EntityType.valueOf(type));
      } catch (Exception e) {
        getLogger().severe("Failed to parse entity " + entityNameKey + ". Invalid type: " + type);
        continue;
      }

      uniqueEntity.setId(entityNameKey);
      uniqueEntity.setName(TextUtils.color(cs.getString("name", "&fSET &cA &9NAME")));
      uniqueEntity.setExperience(cs.getInt("experience", 0));
      uniqueEntity.setKnockbackImmune(cs.getBoolean("knockback-immune", false));
      uniqueEntity.setBaby(cs.getBoolean("baby", false));

      ConfigurationSection attrCS = cs.getConfigurationSection("attributes");
      Map<StrifeAttribute, Double> attributeMap = new HashMap<>();
      for (String k : attrCS.getKeys(false)) {
        StrifeAttribute attr = StrifeAttribute.valueOf(k);
        attributeMap.put(attr, attrCS.getDouble(k));
      }
      uniqueEntity.setAttributeMap(attributeMap);

      ConfigurationSection equipmentCS = cs.getConfigurationSection("equipment");
      if (equipmentCS != null) {
        uniqueEntity
            .setMainHandItem(equipmentManager.getItem(equipmentCS.getString("main-hand", "")));
        uniqueEntity
            .setOffHandItem(equipmentManager.getItem(equipmentCS.getString("off-hand", "")));
        uniqueEntity.setHelmetItem(equipmentManager.getItem(equipmentCS.getString("helmet", "")));
        uniqueEntity
            .setChestItem(equipmentManager.getItem(equipmentCS.getString("chestplate", "")));
        uniqueEntity.setLegsItem(equipmentManager.getItem(equipmentCS.getString("leggings", "")));
        uniqueEntity.setBootsItem(equipmentManager.getItem(equipmentCS.getString("boots", "")));
      }

      ConfigurationSection particleCS = cs.getConfigurationSection("particles");
      if (particleCS != null) {
        try {
          uniqueEntity.setParticle(Particle.valueOf(particleCS.getString("effect")));
        } catch (Exception e) {
          getLogger().severe("Particle for " + entityNameKey + " is invalid. Setting to FLAME");
          uniqueEntity.setParticle(Particle.FLAME);
        }
        uniqueEntity.setParticleCount(particleCS.getInt("count", 1));
        uniqueEntity.setParticleRadius((float) particleCS.getDouble("radius", 0));
      } else {
        uniqueEntity.setParticle(null);
      }

      ConfigurationSection abilityCS = cs.getConfigurationSection("abilities");
      uniqueEntity.setAbilitySet(null);
      if (abilityCS != null) {
        EntityAbilitySet abilitySet = new EntityAbilitySet();
        for (int i = 1; i <= 5; i++) {
          List<String> phaseBegin = abilityCS.getStringList("phase" + i + ".phase-begin");
          List<String> phaseOnHit = abilityCS.getStringList("phase" + i + ".on-hit");
          List<String> phaseWhenHit = abilityCS.getStringList("phase" + i + ".when-hit");
          List<String> phaseTimer = abilityCS.getStringList("phase" + i + ".timer");
          if (!phaseBegin.isEmpty()) {
            List<Ability> abilities = setupPhase(phaseBegin, i);
            if (!abilities.isEmpty()) {
              abilitySet.addAbilityPhase(i, AbilityType.PHASE_SHIFT, abilities);
            }
          }
          if (!phaseOnHit.isEmpty()) {
            List<Ability> abilities = setupPhase(phaseOnHit, i);
            if (!abilities.isEmpty()) {
              abilitySet.addAbilityPhase(i, AbilityType.ON_HIT, abilities);
            }
          }
          if (!phaseWhenHit.isEmpty()) {
            List<Ability> abilities = setupPhase(phaseWhenHit, i);
            if (!abilities.isEmpty()) {
              abilitySet.addAbilityPhase(i, AbilityType.WHEN_HIT, abilities);
            }
          }
          if (!phaseTimer.isEmpty()) {
            List<Ability> abilities = setupPhase(phaseTimer, i);
            if (!abilities.isEmpty()) {
              abilitySet.addAbilityPhase(i, AbilityType.TIMER, abilities);
            }
          }
        }
        uniqueEntity.setAbilitySet(abilitySet);
      }
      uniqueEntityManager.addUniqueEntity(entityNameKey, uniqueEntity);
    }
  }

  private List<Ability> setupPhase(List<String> strings, int phase) {
    List<Ability> abilities = new ArrayList<>();
    for (String s : strings) {
      Ability ability = abilityManager.getAbility(s);
      if (ability == null) {
        getLogger().warning("Failed to add phase" + phase + " ability " + s + " - Not found");
        continue;
      }
      abilities.add(ability);
    }
    return abilities;
  }

  public void loadSpawners() {
    Map<String, Spawner> spawners = new HashMap<>();
    for (String spawnerId : spawnerYAML.getKeys(false)) {
      if (!spawnerYAML.isConfigurationSection(spawnerId)) {
        continue;
      }
      ConfigurationSection cs = spawnerYAML.getConfigurationSection(spawnerId);

      String uniqueId = cs.getString("unique");
      if (!uniqueEntityManager.isLoadedUnique(uniqueId)) {
        LogUtil.printWarning("Skipping spawner " + spawnerId + " with invalid unique " + uniqueId);
        continue;
      }
      UniqueEntity uniqueEntity = uniqueEntityManager.getLoadedUniquesMap().get(uniqueId);

      int respawnSeconds = cs.getInt("respawn-seconds", 30);
      double leashRange = cs.getDouble("leash-dist", 10);

      double xPos = cs.getDouble("location.x");
      double yPos = cs.getDouble("location.y");
      double zPos = cs.getDouble("location.z");
      World world = Bukkit.getWorld(cs.getString("location.world"));

      Location location = new Location(world, xPos, yPos, zPos);

      spawners.put(spawnerId, new Spawner(uniqueEntity, location, respawnSeconds, leashRange));
      spawnerManager.setSpawnerMap(spawners);
    }
  }

  private void saveSpawners() {
    for (String spawnerId : spawnerYAML.getKeys(false)) {
      Spawner spawner = spawnerManager.getSpawnerMap().get(spawnerId);
      if (spawner == null) {
        spawnerYAML.getConfigurationSection(spawnerId).getParent().set(spawnerId, null);
        LogUtil.printDebug("Spawner " + spawnerId + " has been removed.");
      }
    }
    for (String spawnerId : spawnerManager.getSpawnerMap().keySet()) {
      Spawner spawner = spawnerManager.getSpawnerMap().get(spawnerId);
      spawnerYAML.set(spawnerId + ".unique", spawner.getUniqueEntity().getId());
      spawnerYAML.set(spawnerId + ".respawn-seconds", spawner.getRespawnSeconds());
      spawnerYAML.set(spawnerId + ".leash-dist", spawner.getLeashRange());
      spawnerYAML.set(spawnerId + ".location.world", spawner.getLocation().getWorld().getName());
      spawnerYAML.set(spawnerId + ".location.x", spawner.getLocation().getX());
      spawnerYAML.set(spawnerId + ".location.y", spawner.getLocation().getY());
      spawnerYAML.set(spawnerId + ".location.z", spawner.getLocation().getZ());
      LogUtil.printDebug("Saved spawner " + spawnerId + ".");
    }
    spawnerYAML.save();
  }

  public AttributeUpdateManager getAttributeUpdateManager() {
    return attributeUpdateManager;
  }

  public AttackSpeedManager getAttackSpeedManager() {
    return attackSpeedManager;
  }

  public StrifeStatManager getStatManager() {
    return statManager;
  }

  public BlockManager getBlockManager() {
    return blockManager;
  }

  public BarrierManager getBarrierManager() {
    return barrierManager;
  }

  public BleedManager getBleedManager() {
    return bleedManager;
  }

  public DarknessManager getDarknessManager() {
    return darknessManager;
  }

  public RageManager getRageManager() {
    return rageManager;
  }

  public MonsterManager getMonsterManager() {
    return monsterManager;
  }

  public UniqueEntityManager getUniqueEntityManager() {
    return uniqueEntityManager;
  }

  public BossBarManager getBossBarManager() {
    return bossBarManager;
  }

  public EffectManager getEffectManager() {
    return effectManager;
  }

  public AbilityManager getAbilityManager() {
    return abilityManager;
  }

  public SpawnerManager getSpawnerManager() {
    return spawnerManager;
  }

  public MultiplierManager getMultiplierManager() {
    return multiplierManager;
  }

  public CraftExperienceManager getCraftExperienceManager() {
    return craftExperienceManager;
  }

  public EnchantExperienceManager getEnchantExperienceManager() {
    return enchantExperienceManager;
  }

  public FishExperienceManager getFishExperienceManager() {
    return fishExperienceManager;
  }

  public MiningExperienceManager getMiningExperienceManager() {
    return miningExperienceManager;
  }

  public StrifeExperienceManager getExperienceManager() {
    return experienceManager;
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

  public EntityStatCache getEntityStatCache() {
    return entityStatCache;
  }

  public MasterConfiguration getSettings() {
    return settings;
  }

  public LevelupMenu getLevelupMenu() {
    return levelupMenu;
  }

  public StatsMenu getStatsMenu() {
    return statsMenu;
  }

  public LevelingRate getLevelingRate() {
    return levelingRate;
  }

  public int getMaxSkillLevel() {
    return maxSkillLevel;
  }

  public LevelingRate getCraftingRate() {
    return craftingRate;
  }

  public LevelingRate getEnchantRate() {
    return enchantRate;
  }

  public LevelingRate getFishRate() {
    return fishRate;
  }

  public LevelingRate getMiningRate() {
    return miningRate;
  }

  public LogLevel getLogLevel() {
    return logLevel;
  }
}
