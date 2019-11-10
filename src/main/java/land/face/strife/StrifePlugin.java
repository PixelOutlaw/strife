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
package land.face.strife;

import com.comphenix.xp.lookup.LevelingRate;
import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.facecore.logging.PluginLogger;
import com.tealcube.minecraft.bukkit.facecore.plugin.FacePlugin;
import com.tealcube.minecraft.bukkit.shade.objecthunter.exp4j.Expression;
import com.tealcube.minecraft.bukkit.shade.objecthunter.exp4j.ExpressionBuilder;
import io.pixeloutlaw.minecraft.spigot.config.MasterConfiguration;
import io.pixeloutlaw.minecraft.spigot.config.SmartYamlConfiguration;
import io.pixeloutlaw.minecraft.spigot.config.VersionedConfiguration;
import io.pixeloutlaw.minecraft.spigot.config.VersionedSmartYamlConfiguration;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Collectors;
import land.face.strife.api.StrifeExperienceManager;
import land.face.strife.commands.AbilityMacroCommand;
import land.face.strife.commands.AttributesCommand;
import land.face.strife.commands.LevelUpCommand;
import land.face.strife.commands.SpawnerCommand;
import land.face.strife.commands.StrifeCommand;
import land.face.strife.commands.UniqueEntityCommand;
import land.face.strife.data.Spawner;
import land.face.strife.data.UniqueEntity;
import land.face.strife.data.ability.Ability;
import land.face.strife.data.ability.EntityAbilitySet;
import land.face.strife.data.effects.Effect;
import land.face.strife.data.effects.ShootBlock;
import land.face.strife.data.effects.StrifeParticle;
import land.face.strife.listeners.BullionListener;
import land.face.strife.listeners.CombatListener;
import land.face.strife.listeners.CreeperEffectListener;
import land.face.strife.listeners.DOTListener;
import land.face.strife.listeners.DataListener;
import land.face.strife.listeners.DeathListener;
import land.face.strife.listeners.DogeListener;
import land.face.strife.listeners.EntityMagicListener;
import land.face.strife.listeners.EvokerFangEffectListener;
import land.face.strife.listeners.ExperienceListener;
import land.face.strife.listeners.FallListener;
import land.face.strife.listeners.HeadDropListener;
import land.face.strife.listeners.HealingListener;
import land.face.strife.listeners.InventoryListener;
import land.face.strife.listeners.LoreAbilityListener;
import land.face.strife.listeners.MinionListener;
import land.face.strife.listeners.MoveListener;
import land.face.strife.listeners.ShootListener;
import land.face.strife.listeners.SkillLevelUpListener;
import land.face.strife.listeners.SneakAttackListener;
import land.face.strife.listeners.SpawnListener;
import land.face.strife.listeners.StatUpdateListener;
import land.face.strife.listeners.StrifeDamageListener;
import land.face.strife.listeners.TargetingListener;
import land.face.strife.listeners.UniqueSplashListener;
import land.face.strife.listeners.WandListener;
import land.face.strife.managers.AbilityIconManager;
import land.face.strife.managers.AbilityManager;
import land.face.strife.managers.AttackSpeedManager;
import land.face.strife.managers.BarrierManager;
import land.face.strife.managers.BleedManager;
import land.face.strife.managers.BlockManager;
import land.face.strife.managers.BossBarManager;
import land.face.strife.managers.BuffManager;
import land.face.strife.managers.ChampionManager;
import land.face.strife.managers.CombatStatusManager;
import land.face.strife.managers.CorruptionManager;
import land.face.strife.managers.EffectManager;
import land.face.strife.managers.EntityEquipmentManager;
import land.face.strife.managers.ExperienceManager;
import land.face.strife.managers.GlobalBoostManager;
import land.face.strife.managers.IndicatorManager;
import land.face.strife.managers.LoreAbilityManager;
import land.face.strife.managers.MinionManager;
import land.face.strife.managers.MobModManager;
import land.face.strife.managers.MonsterManager;
import land.face.strife.managers.RageManager;
import land.face.strife.managers.SkillExperienceManager;
import land.face.strife.managers.SneakManager;
import land.face.strife.managers.SoulManager;
import land.face.strife.managers.SpawnerManager;
import land.face.strife.managers.StatUpdateManager;
import land.face.strife.managers.StrifeAttributeManager;
import land.face.strife.managers.StrifeMobManager;
import land.face.strife.managers.UniqueEntityManager;
import land.face.strife.managers.WSEManager;
import land.face.strife.menus.abilities.AbilityPickerMenu;
import land.face.strife.menus.abilities.AbilityPickerMenu.AbilityMenuType;
import land.face.strife.menus.levelup.ConfirmationMenu;
import land.face.strife.menus.levelup.LevelupMenu;
import land.face.strife.menus.stats.StatsMenu;
import land.face.strife.stats.AbilitySlot;
import land.face.strife.stats.StrifeStat;
import land.face.strife.storage.DataStorage;
import land.face.strife.storage.FlatfileStorage;
import land.face.strife.tasks.AbilityTickTask;
import land.face.strife.tasks.BarrierTask;
import land.face.strife.tasks.BossBarsTask;
import land.face.strife.tasks.CombatStatusTask;
import land.face.strife.tasks.ForceAttackSpeed;
import land.face.strife.tasks.GlobalMultiplierTask;
import land.face.strife.tasks.IndicatorTask;
import land.face.strife.tasks.MinionDecayTask;
import land.face.strife.tasks.ParticleTask;
import land.face.strife.tasks.PruneBossBarsTask;
import land.face.strife.tasks.RegenTask;
import land.face.strife.tasks.SaveTask;
import land.face.strife.tasks.SneakTask;
import land.face.strife.tasks.SpawnerSpawnTask;
import land.face.strife.tasks.TrackedPruneTask;
import land.face.strife.tasks.WorldSpaceEffectTask;
import land.face.strife.util.LogUtil;
import land.face.strife.util.LogUtil.LogLevel;
import land.face.strife.util.StatUtil;
import ninja.amp.ampmenus.MenuListener;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitTask;
import se.ranzdo.bukkit.methodcommand.CommandHandler;

public class StrifePlugin extends FacePlugin {

  private static StrifePlugin instance;

  private PluginLogger debugPrinter;
  private LogLevel logLevel;
  private CommandHandler commandHandler;
  private MasterConfiguration settings;
  private VersionedSmartYamlConfiguration configYAML;
  private VersionedSmartYamlConfiguration langYAML;
  private VersionedSmartYamlConfiguration attributesYAML;
  private VersionedSmartYamlConfiguration baseStatsYAML;
  private VersionedSmartYamlConfiguration uniqueEnemiesYAML;
  private VersionedSmartYamlConfiguration equipmentYAML;
  private VersionedSmartYamlConfiguration conditionYAML;
  private VersionedSmartYamlConfiguration effectYAML;
  private VersionedSmartYamlConfiguration abilityYAML;
  private VersionedSmartYamlConfiguration loreAbilityYAML;
  private VersionedSmartYamlConfiguration buffsYAML;
  private VersionedSmartYamlConfiguration modsYAML;
  private VersionedSmartYamlConfiguration globalBoostsYAML;
  private SmartYamlConfiguration spawnerYAML;

  private StrifeMobManager strifeMobManager;
  private StatUpdateManager statUpdateManager;
  private StrifeAttributeManager attributeManager;
  private ChampionManager championManager;
  private IndicatorManager indicatorManager;
  private StrifeExperienceManager experienceManager;
  private SkillExperienceManager skillExperienceManager;
  private AttackSpeedManager attackSpeedManager;
  private BlockManager blockManager;
  private BarrierManager barrierManager;
  private BleedManager bleedManager;
  private CorruptionManager corruptionManager;
  private RageManager rageManager;
  private MonsterManager monsterManager;
  private UniqueEntityManager uniqueEntityManager;
  private SneakManager sneakManager;
  private BossBarManager bossBarManager;
  private MinionManager minionManager;
  private EntityEquipmentManager equipmentManager;
  private EffectManager effectManager;
  private AbilityManager abilityManager;
  private LoreAbilityManager loreAbilityManager;
  private AbilityIconManager abilityIconManager;
  private BuffManager buffManager;
  private CombatStatusManager combatStatusManager;
  private SpawnerManager spawnerManager;
  private MobModManager mobModManager;
  private GlobalBoostManager globalBoostManager;
  private SoulManager soulManager;
  private WSEManager wseManager;

  private DataStorage storage;

  private List<BukkitTask> taskList = new ArrayList<>();
  private ParticleTask particleTask;

  private LevelingRate levelingRate;

  private Map<AbilityMenuType, AbilityPickerMenu> abilityMenus;
  private LevelupMenu levelupMenu;
  private ConfirmationMenu confirmMenu;
  private StatsMenu statsMenu;

  private int maxSkillLevel;

  public static StrifePlugin getInstance() {
    return instance;
  }

  @Override
  public void enable() {
    instance = this;
    debugPrinter = new PluginLogger(this);

    List<VersionedSmartYamlConfiguration> configurations = new ArrayList<>();
    configurations.add(configYAML = defaultSettingsLoad("config.yml"));
    configurations.add(langYAML = defaultSettingsLoad("language.yml"));
    configurations.add(attributesYAML = defaultSettingsLoad("attributes.yml"));
    configurations.add(baseStatsYAML = defaultSettingsLoad("base-entity-stats.yml"));
    configurations.add(uniqueEnemiesYAML = defaultSettingsLoad("unique-enemies.yml"));
    configurations.add(equipmentYAML = defaultSettingsLoad("equipment.yml"));
    configurations.add(conditionYAML = defaultSettingsLoad("conditions.yml"));
    configurations.add(effectYAML = defaultSettingsLoad("effects.yml"));
    configurations.add(abilityYAML = defaultSettingsLoad("abilities.yml"));
    configurations.add(loreAbilityYAML = defaultSettingsLoad("lore-abilities.yml"));
    configurations.add(buffsYAML = defaultSettingsLoad("buffs.yml"));
    configurations.add(modsYAML = defaultSettingsLoad("mob-mods.yml"));
    configurations.add(globalBoostsYAML = defaultSettingsLoad("global-boosts.yml"));

    spawnerYAML = new SmartYamlConfiguration(new File(getDataFolder(), "spawners.yml"));

    for (VersionedSmartYamlConfiguration config : configurations) {
      if (config.update()) {
        getLogger().info("Updating " + config.getFileName());
      }
    }

    settings = MasterConfiguration.loadFromFiles(configYAML, langYAML);

    storage = new FlatfileStorage(this);
    championManager = new ChampionManager(this);
    uniqueEntityManager = new UniqueEntityManager(this);
    bossBarManager = new BossBarManager(this);
    minionManager = new MinionManager();
    sneakManager = new SneakManager();
    experienceManager = new ExperienceManager(this);
    skillExperienceManager = new SkillExperienceManager(this);
    strifeMobManager = new StrifeMobManager(this);
    abilityManager = new AbilityManager(this);
    commandHandler = new CommandHandler(this);
    attributeManager = new StrifeAttributeManager();
    blockManager = new BlockManager();
    bleedManager = new BleedManager(this);
    corruptionManager = new CorruptionManager(this);
    attackSpeedManager = new AttackSpeedManager();
    indicatorManager = new IndicatorManager();
    equipmentManager = new EntityEquipmentManager();
    globalBoostManager = new GlobalBoostManager();
    soulManager = new SoulManager(this);
    barrierManager = new BarrierManager();
    statUpdateManager = new StatUpdateManager(strifeMobManager);
    rageManager = new RageManager();
    monsterManager = new MonsterManager(championManager);
    effectManager = new EffectManager(attributeManager, strifeMobManager);
    wseManager = new WSEManager(effectManager);
    spawnerManager = new SpawnerManager(uniqueEntityManager);
    mobModManager = new MobModManager(this);
    loreAbilityManager = new LoreAbilityManager(abilityManager, effectManager);
    abilityIconManager = new AbilityIconManager(this);
    buffManager = new BuffManager();
    combatStatusManager = new CombatStatusManager(this);

    MenuListener.getInstance().register(this);

    try {
      logLevel = LogLevel.valueOf(settings.getString("config.log-level", "ERROR"));
    } catch (Exception e) {
      logLevel = LogLevel.ERROR;
      LogUtil.printError("DANGUS ALERT! Bad log level! Acceptable values: " + LogLevel.values());
    }

    buildBuffs();
    buildEquipment();
    buildLevelpointStats();
    buildBaseStats();
    loadBoosts();
    loadScheduledBoosts();

    buildConditions();
    buildEffects();
    buildAbilities();
    buildLoreAbilities();

    buildUniqueEnemies();
    buildMobMods();
    loadSpawners();

    SaveTask saveTask = new SaveTask(this);
    TrackedPruneTask trackedPruneTask = new TrackedPruneTask(this);
    RegenTask regenTask = new RegenTask(this);
    SneakTask sneakTask = new SneakTask(sneakManager);
    ForceAttackSpeed forceAttackSpeed = new ForceAttackSpeed();
    BarrierTask barrierTask = new BarrierTask(this);
    BossBarsTask bossBarsTask = new BossBarsTask(bossBarManager);
    MinionDecayTask minionDecayTask = new MinionDecayTask(minionManager);
    GlobalMultiplierTask globalMultiplierTask = new GlobalMultiplierTask(globalBoostManager);
    PruneBossBarsTask pruneBossBarsTask = new PruneBossBarsTask(bossBarManager);
    SpawnerSpawnTask spawnerSpawnTask = new SpawnerSpawnTask(spawnerManager);
    AbilityTickTask iconDuraTask = new AbilityTickTask(abilityManager);
    WorldSpaceEffectTask worldSpaceEffectTask = new WorldSpaceEffectTask(wseManager);
    CombatStatusTask combatStatusTask = new CombatStatusTask(combatStatusManager);
    IndicatorTask indicatorTask = new IndicatorTask(this);
    particleTask = new ParticleTask();

    commandHandler.registerCommands(new AttributesCommand(this));
    commandHandler.registerCommands(new LevelUpCommand(this));
    commandHandler.registerCommands(new StrifeCommand(this));
    commandHandler.registerCommands(new UniqueEntityCommand(this));
    commandHandler.registerCommands(new SpawnerCommand(this));
    commandHandler.registerCommands(new AbilityMacroCommand(this));

    levelingRate = new LevelingRate();
    maxSkillLevel = settings.getInt("config.leveling.max-skill-level", 60);

    Expression normalExpr = new ExpressionBuilder(settings.getString("config.leveling.formula",
        "(5+(2*LEVEL)+(LEVEL^1.2))*LEVEL")).variable("LEVEL").build();
    for (int i = 0; i < 200; i++) {
      levelingRate.put(i, i, (int) Math.round(normalExpr.setVariable("LEVEL", i).evaluate()));
    }

    taskList.add(forceAttackSpeed.runTaskTimer(this,
        20L,
        1L
    ));
    taskList.add(Bukkit.getScheduler().runTaskTimer(this,
        () -> championManager.tickPassiveLoreAbilities(),
        20L * 5, // Start save after 5s
        9L // Run slightly more often than every 0.5s to catch odd rounding
    ));
    taskList.add(trackedPruneTask.runTaskTimer(this,
        20L * 61, // Start save after 1 minute
        20L * 60 // Run every 1 minute after that
    ));
    taskList.add(saveTask.runTaskTimer(this,
        20L * 680, // Start save after 11 minutes, 20 seconds cuz yolo
        20L * 600 // Run every 10 minutes after that
    ));
    taskList.add(regenTask.runTaskTimer(this,
        20L * 9, // Start timer after 9s
        RegenTask.REGEN_TICK_RATE
    ));
    taskList.add(sneakTask.runTaskTimer(this,
        20L * 10, // Start timer after 10s
        10L // Run every 1/2 second
    ));
    taskList.add(barrierTask.runTaskTimer(this,
        11 * 20L, // Start timer after 11s
        4L // Run it every 1/5th of a second after
    ));
    taskList.add(bossBarsTask.runTaskTimer(this,
        240L, // Start timer after 12s
        2L // Run it every 1/10th of a second after
    ));
    taskList.add(minionDecayTask.runTaskTimer(this,
        220L, // Start timer after 11s
        11L
    ));
    taskList.add(globalMultiplierTask.runTaskTimer(this,
        20L * 15, // Start timer after 15s
        20L * 60 // Run it every minute after
    ));
    taskList.add(pruneBossBarsTask.runTaskTimer(this,
        20L * 13, // Start timer after 13s
        20L * 60 * 7 // Run it every 7 minutes
    ));
    taskList.add(particleTask.runTaskTimer(this,
        2L,
        1L
    ));
    taskList.add(spawnerSpawnTask.runTaskTimer(this,
        9 * 20L, // Start timer after 9s
        2 * 20L // Run it every 2 seconds
    ));
    taskList.add(iconDuraTask.runTaskTimer(this,
        3 * 20L, // Start timer after 3s
        AbilityTickTask.ABILITY_TICK_RATE
    ));
    taskList.add(worldSpaceEffectTask.runTaskTimer(this,
        20L, // Start timer after 3s
        1L // Run it every tick
    ));
    taskList.add(indicatorTask.runTaskTimer(this,
        20L,
        1L
    ));
    taskList.add(combatStatusTask.runTaskTimer(this,
        3 * 20L + 2L, // Start timer after 3s
        20L
    ));

    globalBoostManager.startScheduledEvents();

    //Bukkit.getPluginManager().registerEvents(new EndermanListener(), this);
    Bukkit.getPluginManager().registerEvents(new ExperienceListener(this), this);
    Bukkit.getPluginManager().registerEvents(new HealingListener(), this);
    Bukkit.getPluginManager().registerEvents(new CombatListener(this), this);
    Bukkit.getPluginManager().registerEvents(new CreeperEffectListener(this), this);
    Bukkit.getPluginManager().registerEvents(new StrifeDamageListener(this), this);
    Bukkit.getPluginManager().registerEvents(
        new UniqueSplashListener(strifeMobManager, blockManager, effectManager), this);
    Bukkit.getPluginManager().registerEvents(
        new EvokerFangEffectListener(strifeMobManager, effectManager), this);
    Bukkit.getPluginManager().registerEvents(new DOTListener(this), this);
    Bukkit.getPluginManager().registerEvents(new WandListener(this), this);
    Bukkit.getPluginManager().registerEvents(new ShootListener(this), this);
    Bukkit.getPluginManager().registerEvents(new HeadDropListener(strifeMobManager), this);
    Bukkit.getPluginManager().registerEvents(new MoveListener(), this);
    Bukkit.getPluginManager().registerEvents(new DataListener(this), this);
    Bukkit.getPluginManager().registerEvents(new DeathListener(this), this);
    Bukkit.getPluginManager().registerEvents(new SkillLevelUpListener(settings), this);
    Bukkit.getPluginManager().registerEvents(new StatUpdateListener(this), this);
    Bukkit.getPluginManager().registerEvents(new EntityMagicListener(), this);
    Bukkit.getPluginManager().registerEvents(new SpawnListener(this), this);
    Bukkit.getPluginManager().registerEvents(
        new MinionListener(strifeMobManager, minionManager), this);
    Bukkit.getPluginManager().registerEvents(new TargetingListener(this), this);
    Bukkit.getPluginManager().registerEvents(new FallListener(), this);
    Bukkit.getPluginManager().registerEvents(new SneakAttackListener(this), this);
    Bukkit.getPluginManager().registerEvents(new DogeListener(strifeMobManager), this);
    Bukkit.getPluginManager().registerEvents(
        new LoreAbilityListener(strifeMobManager, championManager, loreAbilityManager), this);
    Bukkit.getPluginManager().registerEvents(new InventoryListener(this), this);
    if (Bukkit.getPluginManager().getPlugin("Bullion") != null) {
      Bukkit.getPluginManager().registerEvents(new BullionListener(this), this);
    }

    // TODO: Clean up ability menus
    abilityMenus = new HashMap<>();
    for (AbilityMenuType menuType : AbilityMenuType.values()) {
      List<String> abilities = settings.getStringList("config.ability-menus." + menuType);
      List<Ability> abilityList = abilities.stream().map(a -> abilityManager.getAbility(a))
          .collect(Collectors.toList());
      abilityMenus.put(menuType, new AbilityPickerMenu(this, menuType.name(), abilityList));
    }
    levelupMenu = new LevelupMenu(this, getAttributeManager().getAttributes());
    confirmMenu = new ConfirmationMenu(this);
    statsMenu = new StatsMenu();

    for (Player player : Bukkit.getOnlinePlayers()) {
      getChampionManager().updateAll(championManager.getChampion(player));
      statUpdateManager.updateAttributes(player);
      abilityManager.loadPlayerCooldowns(player);
      abilityIconManager.setAllAbilityIcons(player);
    }

    LogUtil.printInfo("Loaded " + uniqueEntityManager.getLoadedUniquesMap().size() + " mobs");
    LogUtil.printInfo("Loaded " + effectManager.getLoadedEffects().size() + " effects");
    LogUtil.printInfo("Loaded " + abilityManager.getLoadedAbilities().size() + " abilities");
    LogUtil.printInfo("Successfully enabled Strife-v" + getDescription().getVersion());
  }

  @Override
  public void disable() {
    saveSpawners();
    storage.saveAll();

    HandlerList.unregisterAll(this);
    Bukkit.getScheduler().cancelTasks(this);

    strifeMobManager.despawnAllTempEntities();
    bossBarManager.removeAllBars();
    spawnerManager.cancelAll();
    rageManager.endRageTasks();
    abilityManager.cancelTimerTimers();
    bleedManager.endBleedTasks();
    corruptionManager.endCorruptTasks();
    particleTask.clearParticles();

    for (Player player : Bukkit.getOnlinePlayers()) {
      abilityIconManager.removeIconItem(player, AbilitySlot.SLOT_A);
      abilityIconManager.removeIconItem(player, AbilitySlot.SLOT_B);
      abilityIconManager.removeIconItem(player, AbilitySlot.SLOT_C);
    }

    ShootBlock.clearTimers();

    for (BukkitTask task : taskList) {
      task.cancel();
    }

    LogUtil.printInfo("Successfully disabled Strife-v" + getDescription().getVersion());
  }

  private VersionedSmartYamlConfiguration defaultSettingsLoad(String name) {
    return new VersionedSmartYamlConfiguration(new File(getDataFolder(), name),
        getResource(name), VersionedConfiguration.VersionUpdateType.BACKUP_AND_UPDATE);
  }

  private void buildAbilities() {
    for (String key : abilityYAML.getKeys(false)) {
      if (!abilityYAML.isConfigurationSection(key)) {
        continue;
      }
      ConfigurationSection cs = abilityYAML.getConfigurationSection(key);
      abilityManager.loadAbility(key, cs);
    }
  }

  private void buildEffects() {
    LogUtil.printDebug("Starting effect load!");
    for (String key : effectYAML.getKeys(false)) {
      if (!effectYAML.isConfigurationSection(key)) {
        continue;
      }
      LogUtil.printDebug("Loading effect: " + key + "...");
      ConfigurationSection cs = effectYAML.getConfigurationSection(key);
      effectManager.loadEffect(key, cs);
    }
  }

  private void buildConditions() {
    for (String key : conditionYAML.getKeys(false)) {
      if (!conditionYAML.isConfigurationSection(key)) {
        continue;
      }
      ConfigurationSection cs = conditionYAML.getConfigurationSection(key);
      effectManager.loadCondition(key, cs);
    }
  }

  private void buildLoreAbilities() {
    for (String key : loreAbilityYAML.getKeys(false)) {
      if (!loreAbilityYAML.isConfigurationSection(key)) {
        continue;
      }
      ConfigurationSection cs = loreAbilityYAML.getConfigurationSection(key);
      loreAbilityManager.loadLoreAbility(key, cs);
    }
  }

  private void buildEquipment() {
    for (String itemStackKey : equipmentYAML.getKeys(false)) {
      if (!equipmentYAML.isConfigurationSection(itemStackKey)) {
        continue;
      }
      ConfigurationSection cs = equipmentYAML.getConfigurationSection(itemStackKey);
      equipmentManager.loadEquipmentItem(itemStackKey, cs);
    }
  }

  private void buildBuffs() {
    for (String buffId : buffsYAML.getKeys(false)) {
      if (!buffsYAML.isConfigurationSection(buffId)) {
        continue;
      }
      ConfigurationSection cs = buffsYAML.getConfigurationSection(buffId);
      buffManager.loadBuff(buffId, cs);
    }
  }

  private void buildLevelpointStats() {
    for (String key : attributesYAML.getKeys(false)) {
      if (!attributesYAML.isConfigurationSection(key)) {
        continue;
      }
      ConfigurationSection cs = attributesYAML.getConfigurationSection(key);
      attributeManager.loadStat(key, cs);
    }
  }

  private void buildBaseStats() {
    monsterManager.loadBaseStats("default", baseStatsYAML.getConfigurationSection("default"));
    for (String entityKey : baseStatsYAML.getKeys(false)) {
      if (!baseStatsYAML.isConfigurationSection(entityKey)) {
        continue;
      }
      ConfigurationSection cs = baseStatsYAML.getConfigurationSection(entityKey);
      monsterManager.loadBaseStats(entityKey, cs);
    }
  }

  private void buildMobMods() {
    for (String key : modsYAML.getKeys(false)) {
      if (!modsYAML.isConfigurationSection(key)) {
        continue;
      }
      ConfigurationSection cs = modsYAML.getConfigurationSection(key);
      mobModManager.loadMobMod(key, cs);
    }
  }

  private void loadScheduledBoosts() {
    ConfigurationSection cs = globalBoostsYAML.getConfigurationSection("scheduled-boosts");
    globalBoostManager.loadScheduledBoosts(cs);
  }

  private void loadBoosts() {
    ConfigurationSection cs = globalBoostsYAML.getConfigurationSection("boost-templates");
    globalBoostManager.loadStatBoosts(cs);
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
      uniqueEntity.setBonusExperience(cs.getInt("bonus-experience", 0));
      uniqueEntity.setExperienceMultiplier((float) cs.getDouble("experience-multiplier", 1));
      uniqueEntity.setKnockbackImmune(cs.getBoolean("knockback-immune", false));
      uniqueEntity.setCharmImmune(cs.getBoolean("charm-immune", true));
      uniqueEntity.setBurnImmune(cs.getBoolean("burn-immune", false));
      uniqueEntity.setIgnoreSneak(cs.getBoolean("ignore-sneak", false));
      uniqueEntity.setAllowMods(cs.getBoolean("allow-mob-mods", true));
      uniqueEntity.setAllowMods(cs.getBoolean("remove-range-modifiers", false));
      uniqueEntity.setShowName(cs.getBoolean("show-name", true));
      uniqueEntity.setMount(cs.getString("mount-id", ""));
      uniqueEntity.setFollowRange(cs.getInt("follow-range", -1));
      uniqueEntity.setSize(cs.getInt("size", 0));
      uniqueEntity.getUniqueAllies().addAll(cs.getStringList("friendly-uniques"));
      uniqueEntity.setBaby(cs.getBoolean("baby", false));
      uniqueEntity.setBaseLevel(cs.getInt("base-level", -1));

      String disguise = cs.getString("disguise", null);
      if (StringUtils.isNotBlank(disguise)) {
        uniqueEntityManager
            .cacheDisguise(uniqueEntity, disguise, cs.getString("disguise-player", null));
      }

      ConfigurationSection statCs = cs.getConfigurationSection("stats");
      Map<StrifeStat, Float> attributeMap = StatUtil.getStatMapFromSection(statCs);
      uniqueEntity.setAttributeMap(attributeMap);

      ConfigurationSection equipmentCS = cs.getConfigurationSection("equipment");
      uniqueEntity.setEquipment(equipmentManager.buildEquipmentFromConfigSection(equipmentCS));

      String particle = cs.getString("particle", "");
      if (StringUtils.isNotBlank(particle)) {
        Effect effect = effectManager.getEffect(particle);
        if (effect instanceof StrifeParticle) {
          uniqueEntity.setStrifeParticle((StrifeParticle) effect);
        }
      }

      ConfigurationSection abilityCS = cs.getConfigurationSection("abilities");
      uniqueEntity.setAbilitySet(null);
      if (abilityCS != null) {
        uniqueEntity.setAbilitySet(new EntityAbilitySet(abilityCS));
      }
      uniqueEntityManager.addUniqueEntity(entityNameKey, uniqueEntity);
    }
  }

  public void loadSpawners() {
    Map<String, Spawner> spawners = new HashMap<>();
    for (String spawnerId : spawnerYAML.getKeys(false)) {
      if (!spawnerYAML.isConfigurationSection(spawnerId)) {
        continue;
      }
      ConfigurationSection cs = spawnerYAML.getConfigurationSection(spawnerId);
      if (cs == null) {
        continue;
      }
      UniqueEntity uniqueEntity = null;
      String uniqueId = cs.getString("unique");
      if (uniqueEntityManager.isLoadedUnique(uniqueId)) {
        uniqueEntity = uniqueEntityManager.getLoadedUniquesMap().get(uniqueId);
      } else {
        LogUtil.printWarning("Spawner " + spawnerId + " has invalid unique " + uniqueId);
      }

      int respawnSeconds = cs.getInt("respawn-seconds", 30);
      int amount = cs.getInt("amount", 1);
      double leashRange = cs.getDouble("leash-dist", 10);

      Location loc = null;
      String world = cs.getString("location.world", "");
      if (StringUtils.isNotBlank(world) && Bukkit.getWorld(world) != null) {
        double xPos = cs.getDouble("location.x");
        double yPos = cs.getDouble("location.y");
        double zPos = cs.getDouble("location.z");
        loc = new Location(Bukkit.getWorld(world), xPos, yPos, zPos);
      } else {
        LogUtil.printWarning("Spawner " + spawnerId + " has invalid location");
      }

      Spawner spawner = new Spawner(uniqueEntity, uniqueId, amount, loc,
          respawnSeconds, leashRange);
      spawners.put(spawnerId, spawner);
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
      spawnerYAML.set(spawnerId + ".unique", spawner.getUniqueId());
      spawnerYAML.set(spawnerId + ".amount", spawner.getAmount());
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

  public StatUpdateManager getStatUpdateManager() {
    return statUpdateManager;
  }

  public AttackSpeedManager getAttackSpeedManager() {
    return attackSpeedManager;
  }

  public StrifeAttributeManager getAttributeManager() {
    return attributeManager;
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

  public CorruptionManager getCorruptionManager() {
    return corruptionManager;
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

  public EntityEquipmentManager getEquipmentManager() {
    return equipmentManager;
  }

  public BossBarManager getBossBarManager() {
    return bossBarManager;
  }

  public MinionManager getMinionManager() {
    return minionManager;
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

  public GlobalBoostManager getGlobalBoostManager() {
    return globalBoostManager;
  }

  public SoulManager getSoulManager() {
    return soulManager;
  }

  public SkillExperienceManager getSkillExperienceManager() {
    return skillExperienceManager;
  }

  public StrifeExperienceManager getExperienceManager() {
    return experienceManager;
  }

  public LoreAbilityManager getLoreAbilityManager() {
    return loreAbilityManager;
  }

  public AbilityIconManager getAbilityIconManager() {
    return abilityIconManager;
  }

  public BuffManager getBuffManager() {
    return buffManager;
  }

  public MobModManager getMobModManager() {
    return mobModManager;
  }

  public CombatStatusManager getCombatStatusManager() {
    return combatStatusManager;
  }

  public DataStorage getStorage() {
    return storage;
  }

  public ChampionManager getChampionManager() {
    return championManager;
  }

  public IndicatorManager getIndicatorManager() {
    return indicatorManager;
  }

  public SneakManager getSneakManager() {
    return sneakManager;
  }

  public StrifeMobManager getStrifeMobManager() {
    return strifeMobManager;
  }

  public WSEManager getWseManager() {
    return wseManager;
  }

  public ParticleTask getParticleTask() {
    return particleTask;
  }

  public MasterConfiguration getSettings() {
    return settings;
  }

  public LevelupMenu getLevelupMenu() {
    return levelupMenu;
  }

  public ConfirmationMenu getConfirmationMenu() {
    return confirmMenu;
  }

  public AbilityPickerMenu getAbilityPicker(AbilityMenuType type) {
    return abilityMenus.get(type);
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

  public LogLevel getLogLevel() {
    return logLevel;
  }

  public void debug(Level level, String... messages) {
    if (debugPrinter != null) {
      debugPrinter.log(level, Arrays.asList(messages));
    }
  }
}
