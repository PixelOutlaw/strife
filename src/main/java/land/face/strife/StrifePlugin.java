/*
  The MIT License Copyright (c) 2015 Teal Cube Games
  <p>
  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
  documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
  rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
  permit persons to whom the Software is furnished to do so, subject to the following conditions:
  <p>
  The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
  Software.
  <p>
  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
  WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
  OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package land.face.strife;

import co.aikar.commands.PaperCommandManager;
import com.comphenix.xp.lookup.LevelingRate;
import com.tealcube.minecraft.bukkit.facecore.logging.PluginLogger;
import com.tealcube.minecraft.bukkit.facecore.plugin.FacePlugin;
import com.tealcube.minecraft.bukkit.shade.objecthunter.exp4j.Expression;
import com.tealcube.minecraft.bukkit.shade.objecthunter.exp4j.ExpressionBuilder;
import io.pixeloutlaw.minecraft.spigot.config.MasterConfiguration;
import io.pixeloutlaw.minecraft.spigot.config.SmartYamlConfiguration;
import io.pixeloutlaw.minecraft.spigot.config.VersionedConfiguration;
import io.pixeloutlaw.minecraft.spigot.config.VersionedSmartYamlConfiguration;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import land.face.strife.api.StrifeExperienceManager;
import land.face.strife.commands.AbilityMacroCommand;
import land.face.strife.commands.AgilityCommand;
import land.face.strife.commands.InspectCommand;
import land.face.strife.commands.LevelUpCommand;
import land.face.strife.commands.SpawnerCommand;
import land.face.strife.commands.StrifeCommand;
import land.face.strife.data.LevelPath;
import land.face.strife.data.LevelPath.Path;
import land.face.strife.data.Spawner;
import land.face.strife.data.UniqueEntity;
import land.face.strife.data.ability.Ability;
import land.face.strife.data.ability.EntityAbilitySet;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.effects.Effect;
import land.face.strife.data.effects.ShootBlock;
import land.face.strife.data.effects.StrifeParticle;
import land.face.strife.hooks.SnazzyPartiesHook;
import land.face.strife.listeners.BullionListener;
import land.face.strife.listeners.ChatListener;
import land.face.strife.listeners.CombatListener;
import land.face.strife.listeners.CreeperExplodeListener;
import land.face.strife.listeners.DOTListener;
import land.face.strife.listeners.DataListener;
import land.face.strife.listeners.DeathListener;
import land.face.strife.listeners.DogeListener;
import land.face.strife.listeners.DoubleJumpListener;
import land.face.strife.listeners.EndermanListener;
import land.face.strife.listeners.EntityHider;
import land.face.strife.listeners.EntityHider.Policy;
import land.face.strife.listeners.EntityMagicListener;
import land.face.strife.listeners.EvokerFangEffectListener;
import land.face.strife.listeners.ExperienceListener;
import land.face.strife.listeners.FallListener;
import land.face.strife.listeners.FishingListener;
import land.face.strife.listeners.HeadLoadListener;
import land.face.strife.listeners.HealingListener;
import land.face.strife.listeners.InventoryListener;
import land.face.strife.listeners.LaunchAndLandListener;
import land.face.strife.listeners.LoreAbilityListener;
import land.face.strife.listeners.MinionListener;
import land.face.strife.listeners.MoneyDropListener;
import land.face.strife.listeners.MoveListener;
import land.face.strife.listeners.ShootListener;
import land.face.strife.listeners.SkillLevelUpListener;
import land.face.strife.listeners.SpawnListener;
import land.face.strife.listeners.StatUpdateListener;
import land.face.strife.listeners.SwingListener;
import land.face.strife.listeners.TargetingListener;
import land.face.strife.listeners.UniqueSplashListener;
import land.face.strife.managers.AbilityIconManager;
import land.face.strife.managers.AbilityManager;
import land.face.strife.managers.AgilityManager;
import land.face.strife.managers.AttackSpeedManager;
import land.face.strife.managers.BarrierManager;
import land.face.strife.managers.BleedManager;
import land.face.strife.managers.BlockManager;
import land.face.strife.managers.BoostManager;
import land.face.strife.managers.BossBarManager;
import land.face.strife.managers.BuffManager;
import land.face.strife.managers.ChampionManager;
import land.face.strife.managers.ChaserManager;
import land.face.strife.managers.CombatStatusManager;
import land.face.strife.managers.CorruptionManager;
import land.face.strife.managers.CounterManager;
import land.face.strife.managers.DamageManager;
import land.face.strife.managers.EffectManager;
import land.face.strife.managers.EnergyManager;
import land.face.strife.managers.EntityEquipmentManager;
import land.face.strife.managers.ExperienceManager;
import land.face.strife.managers.IndicatorManager;
import land.face.strife.managers.LoreAbilityManager;
import land.face.strife.managers.MinionManager;
import land.face.strife.managers.MobModManager;
import land.face.strife.managers.MonsterManager;
import land.face.strife.managers.PathManager;
import land.face.strife.managers.RageManager;
import land.face.strife.managers.SkillExperienceManager;
import land.face.strife.managers.SoulManager;
import land.face.strife.managers.SpawnerManager;
import land.face.strife.managers.StatUpdateManager;
import land.face.strife.managers.StealthManager;
import land.face.strife.managers.StrifeAttributeManager;
import land.face.strife.managers.StrifeMobManager;
import land.face.strife.managers.UniqueEntityManager;
import land.face.strife.managers.WSEManager;
import land.face.strife.menus.abilities.AbilityPickerMenu;
import land.face.strife.menus.abilities.AbilityPickerPickerItem;
import land.face.strife.menus.abilities.AbilityPickerPickerMenu;
import land.face.strife.menus.levelup.ConfirmationMenu;
import land.face.strife.menus.levelup.LevelupMenu;
import land.face.strife.menus.levelup.PathMenu;
import land.face.strife.menus.stats.StatsMenu;
import land.face.strife.stats.AbilitySlot;
import land.face.strife.stats.StrifeStat;
import land.face.strife.storage.DataStorage;
import land.face.strife.storage.FlatfileStorage;
import land.face.strife.tasks.AbilityTickTask;
import land.face.strife.tasks.BarrierTask;
import land.face.strife.tasks.BoostTickTask;
import land.face.strife.tasks.BossBarsTask;
import land.face.strife.tasks.CombatStatusTask;
import land.face.strife.tasks.DamageOverTimeTask;
import land.face.strife.tasks.EnergyRegenTask;
import land.face.strife.tasks.EveryTickTask;
import land.face.strife.tasks.ForceAttackSpeed;
import land.face.strife.tasks.IndicatorTask;
import land.face.strife.tasks.MinionDecayTask;
import land.face.strife.tasks.ParticleTask;
import land.face.strife.tasks.RegenTask;
import land.face.strife.tasks.SaveTask;
import land.face.strife.tasks.SpawnerSpawnTask;
import land.face.strife.tasks.StealthParticleTask;
import land.face.strife.tasks.StrifeMobTracker;
import land.face.strife.tasks.VirtualEntityTask;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.LogUtil;
import land.face.strife.util.LogUtil.LogLevel;
import land.face.strife.util.PlayerDataUtil;
import land.face.strife.util.StatUtil;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import ninja.amp.ampmenus.MenuListener;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitTask;

public class StrifePlugin extends FacePlugin {

  private static StrifePlugin instance;

  private PluginLogger debugPrinter;
  private LogLevel logLevel;
  private MasterConfiguration settings;
  private VersionedSmartYamlConfiguration attributesYAML;
  private VersionedSmartYamlConfiguration baseStatsYAML;
  private VersionedSmartYamlConfiguration uniqueEnemiesYAML;
  private VersionedSmartYamlConfiguration equipmentYAML;
  private VersionedSmartYamlConfiguration conditionYAML;
  private VersionedSmartYamlConfiguration effectYAML;
  private VersionedSmartYamlConfiguration pathYAML;
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
  private CounterManager counterManager;
  private BarrierManager barrierManager;
  private BleedManager bleedManager;
  private CorruptionManager corruptionManager;
  private RageManager rageManager;
  private MonsterManager monsterManager;
  private StealthManager stealthManager;
  private UniqueEntityManager uniqueEntityManager;
  private BossBarManager bossBarManager;
  private MinionManager minionManager;
  private DamageManager damageManager;
  private ChaserManager chaserManager;
  private EntityEquipmentManager equipmentManager;
  private EffectManager effectManager;
  private AbilityManager abilityManager;
  private LoreAbilityManager loreAbilityManager;
  private AbilityIconManager abilityIconManager;
  private BuffManager buffManager;
  private PathManager pathManager;
  private CombatStatusManager combatStatusManager;
  private SpawnerManager spawnerManager;
  private MobModManager mobModManager;
  private BoostManager boostManager;
  private SoulManager soulManager;
  private EnergyManager energyManager;
  private WSEManager wseManager;
  private AgilityManager agilityManager;

  private EntityHider entityHider;

  private DataStorage storage;

  private final List<BukkitTask> taskList = new ArrayList<>();
  private ParticleTask particleTask;
  private DamageOverTimeTask damageOverTimeTask;
  private EnergyRegenTask energyRegenTask;
  private RegenTask regenTask;

  private LevelingRate levelingRate;

  private AbilityPickerPickerMenu abilitySubcategoryMenu;
  private Map<String, AbilityPickerMenu> abilitySubmenus;
  private LevelupMenu levelupMenu;
  private final Map<Path, PathMenu> pathMenus = new HashMap<>();
  private ConfirmationMenu confirmMenu;
  private StatsMenu statsMenu;

  private int maxSkillLevel;

  public SnazzyPartiesHook snazzyPartiesHook;

  public static StrifePlugin getInstance() {
    return instance;
  }

  public StrifePlugin() {
    instance = this;
  }

  @Override
  public void enable() {
    debugPrinter = new PluginLogger(this);

    List<VersionedSmartYamlConfiguration> configurations = new ArrayList<>();
    VersionedSmartYamlConfiguration configYAML;
    configurations.add(configYAML = defaultSettingsLoad("config.yml"));
    VersionedSmartYamlConfiguration langYAML;
    configurations.add(langYAML = defaultSettingsLoad("language.yml"));
    configurations.add(attributesYAML = defaultSettingsLoad("attributes.yml"));
    configurations.add(baseStatsYAML = defaultSettingsLoad("base-entity-stats.yml"));
    configurations.add(uniqueEnemiesYAML = defaultSettingsLoad("unique-enemies.yml"));
    configurations.add(equipmentYAML = defaultSettingsLoad("equipment.yml"));
    configurations.add(conditionYAML = defaultSettingsLoad("conditions.yml"));
    configurations.add(effectYAML = defaultSettingsLoad("effects.yml"));
    configurations.add(abilityYAML = defaultSettingsLoad("abilities.yml"));
    configurations.add(pathYAML = defaultSettingsLoad("paths.yml"));
    configurations.add(loreAbilityYAML = defaultSettingsLoad("lore-abilities.yml"));
    configurations.add(buffsYAML = defaultSettingsLoad("buffs.yml"));
    configurations.add(modsYAML = defaultSettingsLoad("mob-mods.yml"));
    configurations.add(globalBoostsYAML = defaultSettingsLoad("global-boosts.yml"));

    spawnerYAML = new SmartYamlConfiguration(new File(getDataFolder(), "spawners.yml"));
    SmartYamlConfiguration agilityYAML = new SmartYamlConfiguration(new File(getDataFolder(), "agility-locations.yml"));

    for (VersionedSmartYamlConfiguration config : configurations) {
      if (config.update()) {
        getLogger().info("Updating " + config.getFileName());
      }
    }

    settings = MasterConfiguration.loadFromFiles(configYAML, langYAML);
    storage = new FlatfileStorage(this);
    PaperCommandManager commandManager = new PaperCommandManager(this);

    championManager = new ChampionManager(this);
    uniqueEntityManager = new UniqueEntityManager(this);
    bossBarManager = new BossBarManager(this);
    minionManager = new MinionManager();
    damageManager = new DamageManager(this);
    chaserManager = new ChaserManager(this);
    experienceManager = new ExperienceManager(this);
    skillExperienceManager = new SkillExperienceManager(this);
    strifeMobManager = new StrifeMobManager(this);
    abilityManager = new AbilityManager(this);
    attributeManager = new StrifeAttributeManager();
    blockManager = new BlockManager();
    counterManager = new CounterManager(this);
    bleedManager = new BleedManager(this);
    corruptionManager = new CorruptionManager(this);
    attackSpeedManager = new AttackSpeedManager(this);
    indicatorManager = new IndicatorManager(this);
    equipmentManager = new EntityEquipmentManager();
    boostManager = new BoostManager(this);
    soulManager = new SoulManager(this);
    energyManager = new EnergyManager(this);
    barrierManager = new BarrierManager();
    statUpdateManager = new StatUpdateManager(strifeMobManager);
    rageManager = new RageManager();
    monsterManager = new MonsterManager(championManager);
    stealthManager = new StealthManager(this);
    effectManager = new EffectManager(this);
    wseManager = new WSEManager();
    agilityManager = new AgilityManager(this, agilityYAML);
    spawnerManager = new SpawnerManager(this);
    mobModManager = new MobModManager(this);
    loreAbilityManager = new LoreAbilityManager(abilityManager, effectManager);
    abilityIconManager = new AbilityIconManager(this);
    buffManager = new BuffManager();
    pathManager = new PathManager();
    combatStatusManager = new CombatStatusManager(this);

    MenuListener.getInstance().register(this);

    snazzyPartiesHook = new SnazzyPartiesHook();

    try {
      logLevel = LogLevel.valueOf(settings.getString("config.log-level", "ERROR"));
    } catch (Exception e) {
      logLevel = LogLevel.ERROR;
      LogUtil.printError("DANGUS ALERT! Bad log level! Acceptable values: " + Arrays.toString(LogLevel.values()));
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
    buildPaths();
    buildLoreAbilities();

    buildUniqueEnemies();
    buildMobMods();
    loadSpawners();

    SaveTask saveTask = new SaveTask(this);
    StrifeMobTracker strifeMobTracker = new StrifeMobTracker(this);
    StealthParticleTask stealthParticleTask = new StealthParticleTask(stealthManager);
    ForceAttackSpeed forceAttackSpeed = new ForceAttackSpeed();
    BarrierTask barrierTask = new BarrierTask(this);
    BossBarsTask bossBarsTask = new BossBarsTask(bossBarManager);
    MinionDecayTask minionDecayTask = new MinionDecayTask(minionManager);
    BoostTickTask boostTickTask = new BoostTickTask(boostManager);
    SpawnerSpawnTask spawnerSpawnTask = new SpawnerSpawnTask(spawnerManager);
    AbilityTickTask iconDuraTask = new AbilityTickTask(abilityManager);
    VirtualEntityTask virtualEntityTask = new VirtualEntityTask();
    CombatStatusTask combatStatusTask = new CombatStatusTask(combatStatusManager);
    EveryTickTask everyTickTask = new EveryTickTask();
    IndicatorTask indicatorTask = new IndicatorTask(this);
    damageOverTimeTask = new DamageOverTimeTask(this);
    particleTask = new ParticleTask();
    energyRegenTask = new EnergyRegenTask(this);
    regenTask = new RegenTask(this);

    commandManager.registerCommand(new InspectCommand(this));
    commandManager.registerCommand(new LevelUpCommand(this));
    commandManager.registerCommand(new StrifeCommand(this));
    commandManager.registerCommand(new SpawnerCommand(this));
    commandManager.registerCommand(new AbilityMacroCommand(this));
    commandManager.registerCommand(new AgilityCommand(this));

    commandManager.getCommandCompletions()
        .registerCompletion("uniques", c -> uniqueEntityManager.getLoadedUniquesMap().keySet());
    commandManager.getCommandCompletions()
        .registerCompletion("loreabilities", c -> loreAbilityManager.getLoreAbilityIds());
    commandManager.getCommandCompletions()
        .registerCompletion("boosts", c -> boostManager.getLoadedBoostIds());
    commandManager.getCommandCompletions()
        .registerCompletion("skills", c -> Stream.of(LifeSkillType.types).map(Enum::name).collect(Collectors.toList()));
    commandManager.getCommandCompletions()
        .registerCompletion("spawners", c -> spawnerManager.getSpawnerMap().keySet());

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
    taskList.add(strifeMobTracker.runTaskTimer(this,
        20L * 61, // Start save after 1 minute
        20L * 120 // Run every 2 minutes after that
    ));
    taskList.add(saveTask.runTaskTimer(this,
        20L * 680, // Start save after 11 minutes, 20 seconds cuz yolo
        20L * 600 // Run every 10 minutes after that
    ));
    taskList.add(energyRegenTask.runTaskTimer(this,
        20L, // Start timer after 1s
        1L
    ));
    taskList.add(regenTask.runTaskTimer(this,
        20L * 9, // Start timer after 9s
        RegenTask.REGEN_TICK_RATE
    ));
    taskList.add(stealthParticleTask.runTaskTimer(this,
        20L * 3, // Start timer after 10s
        3L
    ));
    taskList.add(barrierTask.runTaskTimer(this,
        11 * 20L, // Start timer after 11s
        2L
    ));
    taskList.add(damageOverTimeTask.runTaskTimer(this,
        20L, // Start timer after 11s
        5L // Run it every 5 ticks
    ));
    taskList.add(bossBarsTask.runTaskTimer(this,
        240L, // Start timer after 12s
        2L // Run it every 1/10th of a second after
    ));
    taskList.add(minionDecayTask.runTaskTimer(this,
        220L, // Start timer after 11s
        11L
    ));
    taskList.add(boostTickTask.runTaskTimer(this,
        20L,
        20L
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
    taskList.add(virtualEntityTask.runTaskTimer(this,
        20L, // Start timer after 3s
        1L // Run it every tick
    ));
    taskList.add(indicatorTask.runTaskTimer(this,
        20L,
        1L
    ));
    taskList.add(everyTickTask.runTaskTimer(this,
        20L,
        1L
    ));
    taskList.add(combatStatusTask.runTaskTimer(this,
        3 * 20L + 2L, // Start timer after 3s
        20L
    ));
    taskList.add(Bukkit.getScheduler().runTaskTimer(this,
        () -> boostManager.checkBoostSchedule(),
        60L,
        20L * 1860
    ));

    agilityManager.loadAgilityContainers();
    boostManager.loadBoosts();

    Bukkit.getPluginManager().registerEvents(new ExperienceListener(this), this);
    Bukkit.getPluginManager().registerEvents(new HealingListener(), this);
    Bukkit.getPluginManager().registerEvents(new CombatListener(this), this);
    Bukkit.getPluginManager().registerEvents(new CreeperExplodeListener(this), this);
    Bukkit.getPluginManager().registerEvents(new UniqueSplashListener(this), this);
    Bukkit.getPluginManager().registerEvents(new EvokerFangEffectListener(strifeMobManager, effectManager), this);
    Bukkit.getPluginManager().registerEvents(new DOTListener(this), this);
    Bukkit.getPluginManager().registerEvents(new EndermanListener(), this);
    Bukkit.getPluginManager().registerEvents(new SwingListener(this), this);
    Bukkit.getPluginManager().registerEvents(new ShootListener(this), this);
    Bukkit.getPluginManager().registerEvents(new ChatListener(), this);
    Bukkit.getPluginManager().registerEvents(new MoveListener(), this);
    Bukkit.getPluginManager().registerEvents(new DataListener(this), this);
    Bukkit.getPluginManager().registerEvents(new DeathListener(this), this);
    Bukkit.getPluginManager().registerEvents(new SkillLevelUpListener(settings), this);
    Bukkit.getPluginManager().registerEvents(new StatUpdateListener(this), this);
    Bukkit.getPluginManager().registerEvents(new EntityMagicListener(this), this);
    Bukkit.getPluginManager().registerEvents(new SpawnListener(this), this);
    Bukkit.getPluginManager().registerEvents(new MoneyDropListener(this), this);
    Bukkit.getPluginManager().registerEvents(new MinionListener(strifeMobManager, minionManager), this);
    Bukkit.getPluginManager().registerEvents(new TargetingListener(this), this);
    Bukkit.getPluginManager().registerEvents(new FallListener(this), this);
    Bukkit.getPluginManager().registerEvents(new LaunchAndLandListener(this), this);
    Bukkit.getPluginManager().registerEvents(new DoubleJumpListener(this), this);
    Bukkit.getPluginManager().registerEvents(new FishingListener(this), this);
    Bukkit.getPluginManager().registerEvents(new DogeListener(strifeMobManager), this);
    Bukkit.getPluginManager().registerEvents(new LoreAbilityListener(strifeMobManager, loreAbilityManager), this);
    Bukkit.getPluginManager().registerEvents(new InventoryListener(this), this);
    entityHider = new EntityHider(this, Policy.BLACKLIST);

    if (Bukkit.getPluginManager().getPlugin("Bullion") != null) {
      Bukkit.getPluginManager().registerEvents(new BullionListener(this), this);
    }
    if (Bukkit.getPluginManager().getPlugin("HeadDatabase") != null) {
      Bukkit.getPluginManager().registerEvents(new HeadLoadListener(this), this);
    }

    ConfigurationSection abilityMenus = configYAML.getConfigurationSection("ability-menus");
    abilitySubmenus = new HashMap<>();
    List<AbilityPickerPickerItem> pickerItems = new ArrayList<>();
    assert abilityMenus != null;
    for (String menuId : abilityMenus.getKeys(false)) {
      List<String> abilities = abilityMenus.getStringList(menuId + ".abilities");
      String title = abilityMenus.getString(menuId + ".title", "CONFIG ME");
      List<Ability> abilityList = abilities.stream().map(a -> abilityManager.getAbility(a))
          .collect(Collectors.toList());
      AbilityPickerMenu menu = new AbilityPickerMenu(this, title, abilityList);
      menu.setId(menuId);
      abilitySubmenus.put(menuId, menu);

      String name = abilityMenus.getString(menuId + ".name", "CONFIGURE ME");
      List<String> lore = abilityMenus.getStringList(menuId + ".lore");
      Material material = Material.valueOf(abilityMenus.getString(menuId + ".material", "BARRIER"));
      int slot = abilityMenus.getInt(menuId + ".slot", 0);
      AbilityPickerPickerItem subMenuIcon = new AbilityPickerPickerItem(menu, material, name, lore,
          slot);
      pickerItems.add(subMenuIcon);
    }

    String pickerName = configYAML.getString("ability-menu-title", "Picker");
    abilitySubcategoryMenu = new AbilityPickerPickerMenu(this, pickerName, pickerItems);
    levelupMenu = new LevelupMenu(this, getAttributeManager().getAttributes());
    confirmMenu = new ConfirmationMenu(this);
    statsMenu = new StatsMenu();
    for (Path path : LevelPath.PATH_VALUES) {
      pathMenus.put(path, new PathMenu(this, path));
    }

    for (Player player : Bukkit.getOnlinePlayers()) {
      statUpdateManager.updateVanillaAttributes(player);
      abilityManager.loadPlayerCooldowns(player);
      abilityIconManager.setAllAbilityIcons(player);
    }
    getChampionManager().updateAll();

    DamageUtil.refresh();

    LogUtil.printInfo("Loaded " + uniqueEntityManager.getLoadedUniquesMap().size() + " mobs");
    LogUtil.printInfo("Loaded " + effectManager.getLoadedEffects().size() + " effects");
    LogUtil.printInfo("Loaded " + abilityManager.getLoadedAbilities().size() + " abilities");
    LogUtil.printInfo("Successfully enabled Strife-v" + getDescription().getVersion());
  }

  @Override
  public void disable() {
    saveSpawners();
    boostManager.saveBoosts();
    storage.saveAll();

    entityHider.close();
    HandlerList.unregisterAll(this);
    Bukkit.getScheduler().cancelTasks(this);

    strifeMobManager.despawnAllTempEntities();
    bossBarManager.clearBars();
    agilityManager.saveLocations();
    spawnerManager.cancelAll();
    rageManager.endRageTasks();
    bleedManager.endBleedTasks();
    corruptionManager.endCorruptTasks();
    particleTask.clearParticles();

    Spawner.SPAWNER_OFFSET = 0;

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

  private void buildPaths() {
    for (String key : pathYAML.getKeys(false)) {
      if (!pathYAML.isConfigurationSection(key)) {
        continue;
      }
      ConfigurationSection cs = pathYAML.getConfigurationSection(key);
      pathManager.loadPath(key, cs);
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
      assert cs != null;
      effectManager.loadEffect(key, cs);
    }
  }

  private void buildConditions() {
    for (String key : conditionYAML.getKeys(false)) {
      if (!conditionYAML.isConfigurationSection(key)) {
        continue;
      }
      ConfigurationSection cs = conditionYAML.getConfigurationSection(key);
      assert cs != null;
      effectManager.loadCondition(key, cs);
    }
  }

  private void buildLoreAbilities() {
    for (String key : loreAbilityYAML.getKeys(false)) {
      if (!loreAbilityYAML.isConfigurationSection(key)) {
        continue;
      }
      ConfigurationSection cs = loreAbilityYAML.getConfigurationSection(key);
      assert cs != null;
      loreAbilityManager.loadLoreAbility(key, cs);
    }
  }

  public void buildEquipment() {
    for (String itemStackKey : equipmentYAML.getKeys(false)) {
      if (!equipmentYAML.isConfigurationSection(itemStackKey)) {
        continue;
      }
      ConfigurationSection cs = equipmentYAML.getConfigurationSection(itemStackKey);
      assert cs != null;
      equipmentManager.loadEquipmentItem(itemStackKey, cs);
    }
  }

  private void buildBuffs() {
    for (String buffId : buffsYAML.getKeys(false)) {
      if (!buffsYAML.isConfigurationSection(buffId)) {
        continue;
      }
      ConfigurationSection cs = buffsYAML.getConfigurationSection(buffId);
      assert cs != null;
      buffManager.loadBuff(buffId, cs);
    }
  }

  private void buildLevelpointStats() {
    for (String key : attributesYAML.getKeys(false)) {
      if (!attributesYAML.isConfigurationSection(key)) {
        continue;
      }
      ConfigurationSection cs = attributesYAML.getConfigurationSection(key);
      assert cs != null;
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
      assert cs != null;
      mobModManager.loadMobMod(key, cs);
    }
  }

  private void loadScheduledBoosts() {
    ConfigurationSection cs = globalBoostsYAML.getConfigurationSection("scheduled-boosts");
    assert cs != null;
    boostManager.loadScheduledBoosts(cs);
  }

  private void loadBoosts() {
    ConfigurationSection cs = globalBoostsYAML.getConfigurationSection("boost-templates");
    assert cs != null;
    boostManager.loadStatBoosts(cs);
  }

  private void buildUniqueEnemies() {
    for (String entityNameKey : uniqueEnemiesYAML.getKeys(false)) {
      if (!uniqueEnemiesYAML.isConfigurationSection(entityNameKey)) {
        continue;
      }
      ConfigurationSection cs = uniqueEnemiesYAML.getConfigurationSection(entityNameKey);

      UniqueEntity uniqueEntity = new UniqueEntity();

      assert cs != null;
      String type = cs.getString("type");
      try {
        uniqueEntity.setType(EntityType.valueOf(type));
      } catch (Exception e) {
        getLogger().severe("Failed to parse entity " + entityNameKey + ". Invalid type: " + type);
        continue;
      }

      uniqueEntity.setId(entityNameKey);
      uniqueEntity.setName(StringExtensionsKt.chatColorize(
          Objects.requireNonNull(cs.getString("name", "&fSET &cA &9NAME"))));
      uniqueEntity.setBonusExperience(cs.getInt("bonus-experience", 0));
      uniqueEntity.setDisplaceMultiplier(cs.getDouble("displace-multiplier", 1.0));
      uniqueEntity.setExperienceMultiplier((float) cs.getDouble("experience-multiplier", 1));
      uniqueEntity.setCharmImmune(cs.getBoolean("charm-immune", false));
      uniqueEntity.setBurnImmune(cs.getBoolean("burn-immune", false));
      uniqueEntity.setFallImmune(cs.getBoolean("fall-immune", false));
      uniqueEntity.setIgnoreSneak(cs.getBoolean("ignore-sneak", false));
      uniqueEntity.setMaxMods(cs.getInt("max-mods", 3));
      uniqueEntity.setRemoveFollowMods(cs.getBoolean("remove-range-modifiers", false));
      if (uniqueEntity.getType() == EntityType.CREEPER) {
        uniqueEntity.setPowered(cs.getBoolean("powered", false));
      }
      uniqueEntity.setShowName(cs.getBoolean("show-name", true));
      uniqueEntity.setMount(cs.getString("mount-id", ""));
      uniqueEntity.setFollowRange(cs.getInt("follow-range", -1));
      uniqueEntity.setSize(cs.getInt("size", -1));
      uniqueEntity.getFactions().addAll(cs.getStringList("factions"));
      uniqueEntity.setBaby(cs.getBoolean("baby", false));
      uniqueEntity.setAngry(cs.getBoolean("angry", false));
      uniqueEntity.setZombificationImmune(cs.getBoolean("zombification-immune", true));
      uniqueEntity.setArmsRaised(cs.getBoolean("arms-raised", true));
      if (uniqueEntity.getType() == EntityType.VILLAGER
          || uniqueEntity.getType() == EntityType.ZOMBIE_VILLAGER) {
        String prof = cs.getString("profession");
        if (prof != null) {
          uniqueEntity.setProfession(Profession.valueOf(prof.toUpperCase()));
        }
      }
      uniqueEntity.setBaseLevel(cs.getInt("base-level", -1));

      Disguise disguise = PlayerDataUtil.parseDisguise(cs.getConfigurationSection("disguise"),
          uniqueEntity.getName(), uniqueEntity.getMaxMods() > 0);

      if (disguise != null) {
        uniqueEntityManager.cacheDisguise(uniqueEntity, disguise);
      }

      ConfigurationSection statCs = cs.getConfigurationSection("stats");
      Map<StrifeStat, Float> attributeMap = StatUtil.getStatMapFromSection(statCs);
      uniqueEntity.setAttributeMap(attributeMap);

      uniqueEntity.setEquipment(equipmentManager
          .buildEquipmentFromConfigSection(cs.getConfigurationSection("equipment")));

      String passengerItem = cs.getString("item-passenger", "");
      if (StringUtils.isNotBlank(passengerItem)) {
        uniqueEntity.setItemPassenger(equipmentManager.getItem(passengerItem));
      }

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

      Spawner spawner = new Spawner(spawnerId, uniqueEntity, uniqueId, amount, loc,
          respawnSeconds, leashRange);
      spawners.put(spawnerId, spawner);
      spawnerManager.setSpawnerMap(spawners);
    }
  }

  private void saveSpawners() {
    for (String spawnerId : spawnerYAML.getKeys(false)) {
      Spawner spawner = spawnerManager.getSpawnerMap().get(spawnerId);
      if (spawner == null) {
        Objects.requireNonNull(Objects.requireNonNull(spawnerYAML.getConfigurationSection(spawnerId)).getParent())
            .set(spawnerId, null);
        LogUtil.printDebug("Spawner " + spawnerId + " has been removed.");
      }
    }
    if (spawnerManager.getSpawnerMap().size() == 0) {
      Bukkit.getLogger().warning("Spawner map size in memory is 0. Not saving.");
      return;
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

  public SnazzyPartiesHook getSnazzyPartiesHook() {
    return snazzyPartiesHook;
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

  public CounterManager getCounterManager() {
    return counterManager;
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

  public StealthManager getStealthManager() {
    return stealthManager;
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

  public DamageManager getDamageManager() {
    return damageManager;
  }

  public ChaserManager getChaserManager() {
    return chaserManager;
  }

  public SpawnerManager getSpawnerManager() {
    return spawnerManager;
  }

  public BoostManager getBoostManager() {
    return boostManager;
  }

  public SoulManager getSoulManager() {
    return soulManager;
  }

  public EnergyManager getEnergyManager() {
    return energyManager;
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

  public PathManager getPathManager() {
    return pathManager;
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

  public StrifeMobManager getStrifeMobManager() {
    return strifeMobManager;
  }

  public WSEManager getWseManager() {
    return wseManager;
  }

  public AgilityManager getAgilityManager() {
    return agilityManager;
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

  public AbilityPickerPickerMenu getAbilityPicker() {
    return abilitySubcategoryMenu;
  }

  public AbilityPickerMenu getSubmenu(String name) {
    return abilitySubmenus.get(name);
  }

  public StatsMenu getStatsMenu() {
    return statsMenu;
  }

  public PathMenu getPathMenu(Path path) {
    return pathMenus.get(path);
  }

  public EnergyRegenTask getEnergyRegenTask() {
    return energyRegenTask;
  }

  public DamageOverTimeTask getDamageOverTimeTask() {
    return damageOverTimeTask;
  }

  public RegenTask getRegenTask() {
    return regenTask;
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

  public EntityHider getEntityHider() {
    return entityHider;
  }

  public void debug(Level level, String... messages) {
    if (debugPrinter != null) {
      debugPrinter.log(level, Arrays.asList(messages));
    }
  }
}
