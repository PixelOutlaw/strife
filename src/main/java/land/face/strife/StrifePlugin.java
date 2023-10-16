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

import static com.comphenix.protocol.PacketType.Play.Server.ENTITY_METADATA;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.comphenix.xp.lookup.LevelingRate;
import com.tealcube.minecraft.bukkit.facecore.logging.PluginLogger;
import com.tealcube.minecraft.bukkit.facecore.plugin.FacePlugin;
import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.ShaderStyle;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import com.tealcube.minecraft.bukkit.shade.acf.PaperCommandManager;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import com.tealcube.minecraft.bukkit.shade.objecthunter.exp4j.Expression;
import com.tealcube.minecraft.bukkit.shade.objecthunter.exp4j.ExpressionBuilder;
import io.pixeloutlaw.minecraft.spigot.config.MasterConfiguration;
import io.pixeloutlaw.minecraft.spigot.config.SmartYamlConfiguration;
import io.pixeloutlaw.minecraft.spigot.config.VersionedConfiguration;
import io.pixeloutlaw.minecraft.spigot.config.VersionedSmartYamlConfiguration;
import java.io.File;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import land.face.dinvy.DeluxeInvyPlugin;
import land.face.learnin.LearninBooksPlugin;
import land.face.learnin.objects.LoadedKnowledge;
import land.face.strife.commands.AbilityMacroCommand;
import land.face.strife.commands.AgilityCommand;
import land.face.strife.commands.GodCommand;
import land.face.strife.commands.InspectCommand;
import land.face.strife.commands.LevelUpCommand;
import land.face.strife.commands.MountCommand;
import land.face.strife.commands.PrayerCommand;
import land.face.strife.commands.SpawnerCommand;
import land.face.strife.commands.StrifeCommand;
import land.face.strife.commands.ToggleXpCommand;
import land.face.strife.data.LevelPath;
import land.face.strife.data.LevelPath.Path;
import land.face.strife.data.LoadedMount;
import land.face.strife.data.Spawner;
import land.face.strife.data.UniqueEntity;
import land.face.strife.data.ability.Ability;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.effects.CreateModelAnimation;
import land.face.strife.data.effects.Effect;
import land.face.strife.data.effects.Riptide;
import land.face.strife.data.effects.ShootBlock;
import land.face.strife.data.effects.TriggerLoreAbility;
import land.face.strife.data.effects.Wait;
import land.face.strife.hooks.SnazzyPartiesHook;
import land.face.strife.listeners.BlockChangeListener;
import land.face.strife.listeners.ChatListener;
import land.face.strife.listeners.CitizenModelListener;
import land.face.strife.listeners.CombatListener;
import land.face.strife.listeners.CreeperExplodeListener;
import land.face.strife.listeners.CurrencyChangeListener;
import land.face.strife.listeners.DOTListener;
import land.face.strife.listeners.DataListener;
import land.face.strife.listeners.DeathListener;
import land.face.strife.listeners.DeluxeEquipListener;
import land.face.strife.listeners.DogeListener;
import land.face.strife.listeners.DoubleJumpListener;
import land.face.strife.listeners.EndermanListener;
import land.face.strife.listeners.EntityMagicListener;
import land.face.strife.listeners.ExperienceListener;
import land.face.strife.listeners.FallListener;
import land.face.strife.listeners.FishingListener;
import land.face.strife.listeners.HeadLoadListener;
import land.face.strife.listeners.HealingListener;
import land.face.strife.listeners.InventoryListener;
import land.face.strife.listeners.JoinAndLeaveListener;
import land.face.strife.listeners.LaunchAndLandListener;
import land.face.strife.listeners.LoreAbilityListener;
import land.face.strife.listeners.MinionListener;
import land.face.strife.listeners.MoneyDropListener;
import land.face.strife.listeners.MountListener;
import land.face.strife.listeners.PlayerInputListener;
import land.face.strife.listeners.PotionListener;
import land.face.strife.listeners.RuneChangeListener;
import land.face.strife.listeners.ShootListener;
import land.face.strife.listeners.SkillLevelUpListener;
import land.face.strife.listeners.SpawnListener;
import land.face.strife.listeners.StatUpdateListener;
import land.face.strife.listeners.SwingListener;
import land.face.strife.listeners.TargetingListener;
import land.face.strife.listeners.ConsumeItemListener;
import land.face.strife.managers.AbilityIconManager;
import land.face.strife.managers.AbilityManager;
import land.face.strife.managers.AgilityManager;
import land.face.strife.managers.AttackSpeedManager;
import land.face.strife.managers.BlockManager;
import land.face.strife.managers.BoostManager;
import land.face.strife.managers.BossBarManager;
import land.face.strife.managers.BuffManager;
import land.face.strife.managers.ChampionManager;
import land.face.strife.managers.ChaserManager;
import land.face.strife.managers.CorruptionManager;
import land.face.strife.managers.CounterManager;
import land.face.strife.managers.DamageManager;
import land.face.strife.managers.DisplayManager;
import land.face.strife.managers.EffectManager;
import land.face.strife.managers.EntityEquipmentManager;
import land.face.strife.managers.ExperienceManager;
import land.face.strife.managers.GuiManager;
import land.face.strife.managers.IndicatorManager;
import land.face.strife.managers.LoreAbilityManager;
import land.face.strife.managers.LoreAbilityManager.TriggerType;
import land.face.strife.managers.MobModManager;
import land.face.strife.managers.MonsterManager;
import land.face.strife.managers.PathManager;
import land.face.strife.managers.PlayerMountManager;
import land.face.strife.managers.PrayerManager;
import land.face.strife.managers.RuneManager;
import land.face.strife.managers.SkillExperienceManager;
import land.face.strife.managers.SoulManager;
import land.face.strife.managers.SpawnerManager;
import land.face.strife.managers.StatUpdateManager;
import land.face.strife.managers.StealthManager;
import land.face.strife.managers.StrifeAttributeManager;
import land.face.strife.managers.StrifeMobManager;
import land.face.strife.managers.TopBarManager;
import land.face.strife.managers.UniqueEntityManager;
import land.face.strife.managers.VagabondManager;
import land.face.strife.managers.WSEManager;
import land.face.strife.menus.abilities.AbilityMenu;
import land.face.strife.menus.abilities.AbilitySubmenu;
import land.face.strife.menus.abilities.ReturnButton;
import land.face.strife.menus.abilities.SubmenuSelectButton;
import land.face.strife.menus.levelup.LevelupMenu;
import land.face.strife.menus.levelup.PathMenu;
import land.face.strife.menus.revive.ReviveMenu;
import land.face.strife.menus.stats.StatsMenu;
import land.face.strife.menus.xpbottle.XpBottleMenu;
import land.face.strife.stats.AbilitySlot;
import land.face.strife.storage.DataStorage;
import land.face.strife.storage.FlatfileStorage;
import land.face.strife.tasks.BoostTickTask;
import land.face.strife.tasks.EnergyTask;
import land.face.strife.tasks.EveryTickTask;
import land.face.strife.tasks.IndicatorTask;
import land.face.strife.tasks.ParticleTask;
import land.face.strife.tasks.SaveTask;
import land.face.strife.tasks.StealthParticleTask;
import land.face.strife.tasks.StrifeMobTracker;
import land.face.strife.tasks.VirtualEntityTask;
import land.face.strife.util.DOTUtil;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.ItemUtil;
import land.face.strife.util.LogUtil;
import land.face.strife.util.LogUtil.LogLevel;
import land.face.strife.util.PlayerDataUtil;
import land.face.strife.util.StatUtil;
import lombok.Getter;
import lombok.Setter;
import ninja.amp.ampmenus.MenuListener;
import org.black_ixx.playerpoints.PlayerPoints;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class StrifePlugin extends FacePlugin {

  private static StrifePlugin instance;
  public static SecureRandom RNG;
  @Getter
  private static boolean glowEnabled;

  private PluginLogger debugPrinter;
  private LogLevel logLevel;
  private MasterConfiguration settings;
  @Getter
  private VersionedSmartYamlConfiguration attributesYAML, baseStatsYAML, equipmentYAML, conditionYAML, effectYAML,
          pathYAML, loreAbilityYAML, buffsYAML, modsYAML, globalBoostsYAML, mountsYAML, prayerYAML, displaysYaml;
  private SmartYamlConfiguration spawnerYAML;

  @Getter
  private StrifeMobManager strifeMobManager;
  @Getter
  private StatUpdateManager statUpdateManager;
  @Getter
  private StrifeAttributeManager attributeManager;
  @Getter
  private ChampionManager championManager;
  @Getter
  private IndicatorManager indicatorManager;
  @Getter
  private PrayerManager prayerManager;
  @Getter
  private TopBarManager topBarManager;
  @Getter
  private ExperienceManager experienceManager;
  @Getter
  private SkillExperienceManager skillExperienceManager;
  @Getter
  private AttackSpeedManager attackSpeedManager;
  @Getter
  private BlockManager blockManager;
  @Getter
  private RuneManager runeManager;
  @Getter
  private CounterManager counterManager;
  @Getter
  private CorruptionManager corruptionManager;
  @Getter
  private MonsterManager monsterManager;
  @Getter
  private StealthManager stealthManager;
  @Getter
  private UniqueEntityManager uniqueEntityManager;
  @Getter
  private VagabondManager vagabondManager;
  @Getter
  private BossBarManager bossBarManager;
  @Getter
  private PlayerMountManager playerMountManager;
  @Getter
  private DamageManager damageManager;
  @Getter
  private ChaserManager chaserManager;
  @Getter
  private EntityEquipmentManager equipmentManager;
  @Getter
  private DisplayManager displayManager;
  @Getter
  private EffectManager effectManager;
  @Getter
  private AbilityManager abilityManager;
  @Getter
  private LoreAbilityManager loreAbilityManager;
  @Getter
  private AbilityIconManager abilityIconManager;
  @Getter
  private GuiManager guiManager;
  @Getter
  private BuffManager buffManager;
  @Getter
  private PathManager pathManager;
  @Getter
  private SpawnerManager spawnerManager;
  @Getter
  private MobModManager mobModManager;
  @Getter
  private BoostManager boostManager;
  @Getter
  private SoulManager soulManager;
  @Getter
  private WSEManager wseManager;
  @Getter
  private AgilityManager agilityManager;
  @Getter
  private CitizenModelListener citizenModelListener;

  private DataStorage storage;

  private final List<BukkitTask> taskList = new ArrayList<>();
  private ParticleTask particleTask;

  private LevelingRate levelingRate;

  private AbilityMenu abilitySubcategoryMenu;
  private Map<String, AbilitySubmenu> abilitySubmenus;
  @Getter
  private LevelupMenu levelupMenu;
  private final Map<Path, PathMenu> pathMenus = new HashMap<>();
  @Getter
  private StatsMenu statsMenu;
  @Getter
  private ReviveMenu reviveMenu;

  @Getter @Setter
  private XpBottleMenu smallBottleMenu, mediumBottleMenu, bigBottleMenu, giantBottleMenu;

  private PaperCommandManager commandManager;

  private int maxSkillLevel;

  public SnazzyPartiesHook snazzyPartiesHook;
  @Getter
  private DeluxeInvyPlugin deluxeInvyPlugin;
  @Getter
  private PlayerPoints playerPointsPlugin;
  @Getter
  private com.tealcube.minecraft.bukkit.shade.effectlib.effectlib.EffectManager effectLibManager;

  public static float WALK_COST;
  public static float WALK_COST_PERCENT;
  public static float RUN_COST;
  public static float RUN_COST_PERCENT;

  public static final DecimalFormat INT_FORMAT = new DecimalFormat("#,###,###,###,###");

  public static StrifePlugin getInstance() {
    return instance;
  }

  public StrifePlugin() {
    instance = this;
  }

  @Override
  public void enable() {
    instance = this;
    RNG = new SecureRandom();
    debugPrinter = new PluginLogger(this);
    try {
      logLevel = LogLevel.valueOf(settings.getString("config.log-level", "ERROR"));
    } catch (Exception e) {
      logLevel = LogLevel.ERROR;
      LogUtil.printError("DANGUS ALERT! Bad log level! Acceptable values: " + Arrays.toString(LogLevel.values()));
    }

    List<VersionedSmartYamlConfiguration> configurations = new ArrayList<>();
    VersionedSmartYamlConfiguration configYAML;
    configurations.add(configYAML = defaultSettingsLoad("config.yml"));
    VersionedSmartYamlConfiguration langYAML;
    configurations.add(langYAML = defaultSettingsLoad("language.yml"));
    configurations.add(attributesYAML = defaultSettingsLoad("attributes.yml"));
    configurations.add(baseStatsYAML = defaultSettingsLoad("base-entity-stats.yml"));
    configurations.add(equipmentYAML = defaultSettingsLoad("equipment.yml"));
    configurations.add(conditionYAML = defaultSettingsLoad("conditions.yml"));
    configurations.add(effectYAML = defaultSettingsLoad("effects.yml"));
    configurations.add(pathYAML = defaultSettingsLoad("paths.yml"));
    configurations.add(loreAbilityYAML = defaultSettingsLoad("lore-abilities.yml"));
    configurations.add(buffsYAML = defaultSettingsLoad("buffs.yml"));
    configurations.add(modsYAML = defaultSettingsLoad("mob-mods.yml"));
    configurations.add(globalBoostsYAML = defaultSettingsLoad("global-boosts.yml"));
    configurations.add(mountsYAML = defaultSettingsLoad("mounts.yml"));
    configurations.add(prayerYAML = defaultSettingsLoad("prayer.yml"));
    configurations.add(displaysYaml = defaultSettingsLoad("display-vfx.yml"));

    SmartYamlConfiguration agilityYAML = new SmartYamlConfiguration(
        new File(getDataFolder(), "agility-locations.yml"));

    for (VersionedSmartYamlConfiguration config : configurations) {
      if (config.update()) {
        getLogger().info("Updating " + config.getFileName());
      }
    }

    List<SmartYamlConfiguration> uniques = uniqueFileLoad();
    spawnerYAML = new SmartYamlConfiguration(new File(getDataFolder(), "spawners.yml"));

    settings = MasterConfiguration.loadFromFiles(configYAML, langYAML);
    storage = new FlatfileStorage(this);
    commandManager = new PaperCommandManager(this);

    attributeManager = new StrifeAttributeManager(attributesYAML);

    effectManager = new EffectManager(this);
    abilityManager = new AbilityManager(this);
    loreAbilityManager = new LoreAbilityManager(abilityManager, effectManager);

    championManager = new ChampionManager(this);
    uniqueEntityManager = new UniqueEntityManager(this);
    vagabondManager = new VagabondManager(this);
    bossBarManager = new BossBarManager(this);
    playerMountManager = new PlayerMountManager(this);
    damageManager = new DamageManager(this);
    chaserManager = new ChaserManager(this);
    experienceManager = new ExperienceManager(this);
    skillExperienceManager = new SkillExperienceManager(this);
    strifeMobManager = new StrifeMobManager(this);
    blockManager = new BlockManager(this);
    runeManager = new RuneManager(this);
    counterManager = new CounterManager(this);
    corruptionManager = new CorruptionManager(this);
    attackSpeedManager = new AttackSpeedManager(this);
    indicatorManager = new IndicatorManager(this);
    prayerManager = new PrayerManager(this);
    topBarManager = new TopBarManager(this);
    equipmentManager = new EntityEquipmentManager();
    buildEquipment();
    displayManager = new DisplayManager(this);
    boostManager = new BoostManager(this);
    soulManager = new SoulManager(this);
    statUpdateManager = new StatUpdateManager(this);
    monsterManager = new MonsterManager(championManager);
    stealthManager = new StealthManager(this);
    wseManager = new WSEManager();
    agilityManager = new AgilityManager(this, agilityYAML);
    spawnerManager = new SpawnerManager(this);
    mobModManager = new MobModManager(settings, equipmentManager);
    abilityIconManager = new AbilityIconManager(this);
    guiManager = new GuiManager(this);
    buffManager = new BuffManager();
    pathManager = new PathManager();

    MenuListener.getInstance().register(this);

    snazzyPartiesHook = new SnazzyPartiesHook();

    WALK_COST = (float) settings.getDouble("config.mechanics.energy.walk-cost-flat", 3)
        * EnergyTask.TICK_MULT;
    WALK_COST_PERCENT = (float) settings.getDouble("config.mechanics.energy.walk-regen-percent",
        0.75);
    RUN_COST = (float) settings.getDouble("config.mechanics.energy.run-cost-flat", 10)
        * EnergyTask.TICK_MULT;
    RUN_COST_PERCENT = (float) settings.getDouble("config.mechanics.energy.run-regen-percent",
        0.25);

    buildConditions();
    buildEffects();
    buildAbilities();
    buildLoreAbilities();

    buildBuffs();
    buildBaseStats();
    loadBoosts();
    loadScheduledBoosts();
    buildPaths();

    uniqueEntityManager.loadUniques(uniques);
    vagabondManager.loadClasses(configYAML.getConfigurationSection("vagabonds"));
    buildMobMods();
    loadSpawners();
    loadMounts();
    prayerManager.setupGodPrayers();

    SaveTask saveTask = new SaveTask(this);
    StrifeMobTracker strifeMobTracker = new StrifeMobTracker(this);
    StealthParticleTask stealthParticleTask = new StealthParticleTask(stealthManager);
    BoostTickTask boostTickTask = new BoostTickTask(boostManager);
    VirtualEntityTask virtualEntityTask = new VirtualEntityTask();
    Bukkit.getScheduler().runTaskLater(this, () -> {
      new EveryTickTask(this);
    }, 200L);
    IndicatorTask indicatorTask = new IndicatorTask(this);
    particleTask = new ParticleTask();
    //regenTask = new RegenTask(this);

    commandManager.registerCommand(new InspectCommand(this));
    commandManager.registerCommand(new LevelUpCommand(this));
    commandManager.registerCommand(new MountCommand(this));
    commandManager.registerCommand(new PrayerCommand(this));
    commandManager.registerCommand(new GodCommand(this));
    commandManager.registerCommand(new StrifeCommand(this));
    commandManager.registerCommand(new ToggleXpCommand(this));
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
        .registerCompletion("skills",
            c -> Stream.of(LifeSkillType.types).map(Enum::name).collect(Collectors.toList()));
    commandManager.getCommandCompletions()
        .registerCompletion("spawners", c -> spawnerManager.getSpawnerMap().keySet());
    commandManager.getCommandCompletions()
        .registerCompletion("abilities", c -> abilityManager.getLoadedAbilities().keySet());
    commandManager.getCommandCompletions()
        .registerCompletion("buffs", c -> buffManager.getLoadedBuffIds());

    levelingRate = new LevelingRate();
    maxSkillLevel = settings.getInt("config.leveling.max-skill-level", 60);

    effectLibManager = new com.tealcube.minecraft.bukkit.shade.effectlib.effectlib.EffectManager(this);

    Expression normalExpr = new ExpressionBuilder(settings.getString("config.leveling.formula",
        "(5+(2*LEVEL)+(LEVEL^1.2))*LEVEL")).variable("LEVEL").build();
    for (int i = 0; i < 200; i++) {
      levelingRate.put(i, i, (int) Math.round(normalExpr.setVariable("LEVEL", i).evaluate()));
    }
    taskList.add(Bukkit.getScheduler().runTaskTimer(this,
        () -> championManager.tickPassiveLoreAbilities(),
        20L * 10, // Start save after 10s
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
    taskList.add(stealthParticleTask.runTaskTimer(this,
        20L * 3, // Start timer after 10s
        3L
    ));
    taskList.add(boostTickTask.runTaskTimer(this,
        20L,
        20L
    ));
    taskList.add(particleTask.runTaskTimer(this,
        2L,
        1L
    ));
    taskList.add(virtualEntityTask.runTaskTimer(this,
        20L, // Start timer after 3s
        1L // Run it every tick
    ));
    taskList.add(indicatorTask.runTaskTimer(this,
        20L,
        1L
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
    Bukkit.getPluginManager().registerEvents(new ConsumeItemListener(this), this);
    Bukkit.getPluginManager().registerEvents(new DOTListener(this), this);
    Bukkit.getPluginManager().registerEvents(new EndermanListener(), this);
    Bukkit.getPluginManager().registerEvents(new SwingListener(this), this);
    Bukkit.getPluginManager().registerEvents(new ShootListener(this), this);
    Bukkit.getPluginManager().registerEvents(new ChatListener(), this);
    Bukkit.getPluginManager().registerEvents(new DataListener(this), this);
    Bukkit.getPluginManager().registerEvents(new JoinAndLeaveListener(this), this);
    Bukkit.getPluginManager().registerEvents(new CurrencyChangeListener(this), this);
    Bukkit.getPluginManager().registerEvents(new DeathListener(this), this);
    Bukkit.getPluginManager().registerEvents(new SkillLevelUpListener(settings), this);
    Bukkit.getPluginManager().registerEvents(new StatUpdateListener(this), this);
    Bukkit.getPluginManager().registerEvents(new EntityMagicListener(this), this);
    Bukkit.getPluginManager().registerEvents(new SpawnListener(this), this);
    // Disabled when invy is controlled by another plugin
    // Bukkit.getPluginManager().registerEvents(new ShearsEquipListener(), this);
    Bukkit.getPluginManager().registerEvents(new MinionListener(this), this);
    Bukkit.getPluginManager().registerEvents(new RuneChangeListener(this), this);
    Bukkit.getPluginManager().registerEvents(new BlockChangeListener(this), this);
    Bukkit.getPluginManager().registerEvents(new TargetingListener(this), this);
    Bukkit.getPluginManager().registerEvents(new FallListener(this), this);
    Bukkit.getPluginManager().registerEvents(new MountListener(this), this);
    Bukkit.getPluginManager().registerEvents(new PlayerInputListener(this), this);
    Bukkit.getPluginManager().registerEvents(new PotionListener(this), this);
    Bukkit.getPluginManager().registerEvents(new LaunchAndLandListener(this), this);
    Bukkit.getPluginManager().registerEvents(new DoubleJumpListener(this), this);
    Bukkit.getPluginManager().registerEvents(new FishingListener(this), this);
    Bukkit.getPluginManager().registerEvents(new DogeListener(strifeMobManager), this);
    Bukkit.getPluginManager().registerEvents(new LoreAbilityListener(strifeMobManager), this);
    Bukkit.getPluginManager().registerEvents(new InventoryListener(this), this);
    citizenModelListener = new CitizenModelListener(this);
    Bukkit.getPluginManager().registerEvents(citizenModelListener, this);

    Bukkit.getScheduler().runTaskLater(this, () ->
        citizenModelListener.reloadModels(configYAML.getConfigurationSection("npc-model-data-fuck-model-engine")),
        200L);

    if (Bukkit.getPluginManager().getPlugin("Bullion") != null) {
      Bukkit.getPluginManager().registerEvents(new MoneyDropListener(this), this);
    }
    if (Bukkit.getPluginManager().getPlugin("HeadDatabase") != null) {
      Bukkit.getPluginManager().registerEvents(new HeadLoadListener(this), this);
    }
    if (Bukkit.getPluginManager().getPlugin("DeluxeInvy") != null) {
      deluxeInvyPlugin = (DeluxeInvyPlugin) Bukkit.getPluginManager().getPlugin("DeluxeInvy");
      Bukkit.getPluginManager().registerEvents(new DeluxeEquipListener(this), this);
    }
    if (Bukkit.getPluginManager().getPlugin("PlayerPoints") != null) {
      playerPointsPlugin = (PlayerPoints) Bukkit.getPluginManager().getPlugin("PlayerPoints");
    }
    glowEnabled = Bukkit.getPluginManager().getPlugin("GlowApi") != null;

    ReturnButton returnButton = new ReturnButton(this, Material.PAPER,
        PaletteUtil.color("|yellow||b|<< Go Back"));
    ConfigurationSection abilityMenus = configYAML.getConfigurationSection("ability-menus");
    abilitySubmenus = new HashMap<>();
    List<SubmenuSelectButton> pickerItems = new ArrayList<>();
    assert abilityMenus != null;
    for (String menuId : abilityMenus.getKeys(false)) {
      List<String> abilities = abilityMenus.getStringList(menuId + ".abilities");
      String title = abilityMenus.getString(menuId + ".title", "CONFIG ME");
      List<Ability> abilityList = new ArrayList<>();
      for (String ability : abilities) {
        Ability loopAbility = abilityManager.getAbility(ability);
        if (loopAbility == null) {
          Bukkit.getLogger().warning("[Strife] Ability " + ability + " not in section " + menuId);
          continue;
        }
        abilityList.add(loopAbility);
      }
      abilityList.forEach((a)-> a.setHidden(false));
      AbilitySubmenu menu = new AbilitySubmenu(this, menuId, "&f扚&0" + title, abilityList, returnButton);
      abilitySubmenus.put(menuId, menu);

      String name = abilityMenus.getString(menuId + ".name", "CONFIGURE ME");
      List<String> lore = abilityMenus.getStringList(menuId + ".lore");
      Material material = Material.valueOf(abilityMenus.getString(menuId + ".material", "BARRIER"));
      int slot = abilityMenus.getInt(menuId + ".slot", 0);
      int modelData = abilityMenus.getInt(menuId + ".model-data", 0);
      SubmenuSelectButton subMenuIcon = new SubmenuSelectButton(menu, material, modelData, name, lore, slot);
      pickerItems.add(subMenuIcon);
    }

    smallBottleMenu = new XpBottleMenu(this,
        FaceColor.RAINBOW.shaded(ShaderStyle.WAVE) + "Small Xp Bottle", 2000, 50);
    mediumBottleMenu = new XpBottleMenu(this,
        FaceColor.RAINBOW.shaded(ShaderStyle.WAVE) + "Medium Xp Bottle", 2001, 200);
    bigBottleMenu = new XpBottleMenu(this,
        FaceColor.RAINBOW.shaded(ShaderStyle.WAVE) + "Big Xp Bottle", 2002, 1000);
    giantBottleMenu = new XpBottleMenu(this,
        FaceColor.RAINBOW.shaded(ShaderStyle.WAVE) + "Huge Xp Bottle", 2003, 10000);

    String pickerName = configYAML.getString("ability-menu-title", "Picker");
    int size = configYAML.getInt("ability-menu-size", 36);
    abilitySubcategoryMenu = new AbilityMenu(this, pickerName, pickerItems, size);
    String title = PaletteUtil.color(getSettings().getString("language.attribute-menu.title", "Level Up!"));
    levelupMenu = new LevelupMenu(this, title, getAttributeManager().getAttributes());
    statsMenu = new StatsMenu(this);
    for (Path path : LevelPath.PATH_VALUES) {
      pathMenus.put(path, new PathMenu(this, path));
    }
    reviveMenu = new ReviveMenu(this);

    DamageUtil.refresh(this);
    PlayerDataUtil.refresh(this);
    StatUtil.refreshPlugin(this);
    DOTUtil.refresh(this);

    ItemUtil.pickDestroyKeys.clear();
    ItemUtil.hoeDestroyKeys.clear();
    ItemUtil.axeDestroyKeys.clear();
    ItemUtil.shearsDestroyKeys.clear();
    ItemUtil.pickDestroyKeys.addAll(configYAML.getStringList("pick-destroy-keys"));
    ItemUtil.hoeDestroyKeys.addAll(configYAML.getStringList("hoe-destroy-keys"));
    ItemUtil.axeDestroyKeys.addAll(configYAML.getStringList("axe-destroy-keys"));
    ItemUtil.shearsDestroyKeys.addAll(configYAML.getStringList("shears-destroy-keys"));

    Riptide.buildNMSEnum();
    Riptide.startTask(this);

    ProtocolLibrary.getProtocolManager()
        .addPacketListener(new PacketAdapter(this, ENTITY_METADATA) {
          public void onPacketSending(PacketEvent event) {
            try {
              int entityId = event.getPacket().getIntegers().read(0);
              if (entityId < 0) {
                return;
              }
              Entity entity = event.getPacket()
                  .getEntityModifier(event.getPlayer().getWorld()).read(0);
              if (entity instanceof LivingEntity && Riptide
                  .isRiptideAnimationPlaying((LivingEntity) entity)) {
                StructureModifier<List<WrappedWatchableObject>> watcher = event.getPacket()
                    .getWatchableCollectionModifier();
                for (WrappedWatchableObject watch : watcher.read(0)) {
                  if (watch.getIndex() == 5) {
                    watch.setValue(Riptide.RIPTIDE_POSE_ENUM);
                  }
                  if (watch.getIndex() == 7) {
                    watch.setValue((byte) 4);
                  }
                }
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        });

    LogUtil.printInfo("[Strife] Loaded " + uniqueEntityManager.getLoadedUniquesMap().size() + " mobs");
    LogUtil.printInfo("[Strife] Loaded " + effectManager.getLoadedEffects().size() + " effects");
    LogUtil.printInfo("[Strife] Loaded " + abilityManager.getLoadedAbilities().size() + " abilities");


    for (Player player : Bukkit.getOnlinePlayers()) {
      topBarManager.setupPlayer(player);
      championManager.getChampion(player).recombineCache(this);
      statUpdateManager.updateAllAttributes(player);
      boostManager.updateGlobalBoostStatus(player);
      abilityManager.loadPlayerCooldowns(player);
      guiManager.setupGui(player);
      attackSpeedManager.getAttackMultiplier(strifeMobManager.getStatMob(player), 1);
      bossBarManager.createBars(player);
      Bukkit.getScheduler().runTaskLater(this,
          () -> abilityIconManager.setAllAbilityIcons(player), 10L);
      LogUtil.printInfo("[Strife] Refreshed player " + player.getName());
    }

    LogUtil.printInfo("[Strife] Successfully enabled Strife-v" + getDescription().getVersion());
  }

  @Override
  public void disable() {
    storage.saveAll();
    commandManager.unregisterCommands();
    saveSpawners();
    boostManager.saveBoosts();

    HandlerList.unregisterAll(this);
    Bukkit.getScheduler().cancelTasks(this);
    ProtocolLibrary.getProtocolManager().removePacketListeners(this);

    strifeMobManager.despawnAllTempEntities();
    bossBarManager.clearBars();
    agilityManager.saveLocations();
    spawnerManager.cancelAll();
    corruptionManager.endTasks();
    blockManager.endTasks();
    runeManager.endTasks();
    particleTask.clearParticles();
    playerMountManager.clearAll();

    LearninBooksPlugin.instance.getKnowledgeManager().purgeKnowledge("strife");

    Spawner.SPAWNER_OFFSET = 0;
    for (Entity e : CreateModelAnimation.CURRENT_MODELS) {
      e.remove();
    }

    for (Player player : Bukkit.getOnlinePlayers()) {
      abilityIconManager.removeIconItem(player, AbilitySlot.SLOT_A);
      abilityIconManager.removeIconItem(player, AbilitySlot.SLOT_B);
      abilityIconManager.removeIconItem(player, AbilitySlot.SLOT_C);
      abilityIconManager.removeIconItem(player, AbilitySlot.SLOT_D);
      strifeMobManager.saveEnergy(player);
      strifeMobManager.despawnMinions(player);
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

  private List<SmartYamlConfiguration> uniqueFileLoad() {
    List<SmartYamlConfiguration> uniques = new ArrayList<>();
    File folder = new File(getDataFolder(), "uniques");
    File[] listOfFiles = folder.listFiles();
    for (File f : Objects.requireNonNull(listOfFiles)) {
      uniques.add(new SmartYamlConfiguration(f));
    }
    return uniques;
  }

  private void buildAbilities() {
    List<SmartYamlConfiguration> abilityConfigs = new ArrayList<>();
    File folder = new File(getDataFolder(), "abilities");
    File[] listOfFiles = folder.listFiles();
    for (File f : Objects.requireNonNull(listOfFiles)) {
      abilityConfigs.add(new SmartYamlConfiguration(f));
    }
    for (SmartYamlConfiguration abilityYAML : abilityConfigs) {
      for (String key : abilityYAML.getKeys(false)) {
        if (!abilityYAML.isConfigurationSection(key)) {
          continue;
        }
        ConfigurationSection cs = abilityYAML.getConfigurationSection(key);
        try {
          abilityManager.loadAbility(key, cs);
        } catch (Exception e) {
          Bukkit.getLogger().warning("[Strife] Failed to load ability " + key);
          Bukkit.getLogger().warning(e.toString());
        }
      }
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
    Effect.setPlugin(this);
    for (int i = 1; i <= 100; i++) {
      Wait wait = new Wait();
      wait.setTickDelay(i);
      effectManager.loadEffect("wait-" + i, wait);
    }
    LogUtil.printDebug("Loaded base 100 waits");
    for (String key : effectYAML.getKeys(false)) {
      if (!effectYAML.isConfigurationSection(key)) {
        continue;
      }
      LogUtil.printDebug("Loading effect: " + key + "...");
      ConfigurationSection cs = effectYAML.getConfigurationSection(key);
      assert cs != null;
      effectManager.loadEffect(key, cs);
    }
    for (TriggerType type : TriggerType.values()) {
      String id = "TRIGGER_" + type.name();
      TriggerLoreAbility triggerLoreAbility = new TriggerLoreAbility(type);
      triggerLoreAbility.setId(id);
      triggerLoreAbility.setFriendly(true);
      triggerLoreAbility.setForceTargetCaster(true);
      effectManager.getLoadedEffects().put(id, triggerLoreAbility);
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

  public void buildMobMods() {
    List<LoadedKnowledge> knowledges = new ArrayList<>();
    for (String key : modsYAML.getKeys(false)) {
      if (!modsYAML.isConfigurationSection(key)) {
        continue;
      }
      ConfigurationSection cs = modsYAML.getConfigurationSection(key);
      assert cs != null;
      mobModManager.loadMobMod(key, cs);
      ConfigurationSection section = cs.getConfigurationSection("knowledge");
      if (section != null) {
        LoadedKnowledge lk = PlayerDataUtil.loadModKnowledge(
            "mod-" + key, 300 + knowledges.size(), section);
        lk.setSource("strife");
        knowledges.add(lk);
      }
    }
    LearninBooksPlugin.instance.getKnowledgeManager().addExternalKnowledge(knowledges);
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
        double xDir = cs.getDouble("location.x-dir", 0);
        double yDir = cs.getDouble("location.y-dir", 0);
        double zDir = cs.getDouble("location.z-dir", 0);
        loc.setDirection(new Vector(xDir, yDir, zDir));
      } else {
        LogUtil.printWarning("Spawner " + spawnerId + " has invalid location");
      }

      if (loc == null) {
        Bukkit.getLogger().warning("[Strife] Spawner " + spawnerId + " has invalid location!");
        continue;
      }

      Spawner spawner = new Spawner(spawnerId, uniqueEntity, uniqueId, amount, loc,
          respawnSeconds, leashRange);

      spawner.setStartTime(cs.getInt("start-time", -1));
      spawner.setEndTime(cs.getInt("end-time", -1));

      spawners.put(spawnerId, spawner);
    }
    spawnerManager.setSpawnerMap(spawners);
  }

  public void loadMounts() {
    try {
      for (String key : mountsYAML.getConfigurationSection("mounts").getKeys(false)) {
        ConfigurationSection mountSection = mountsYAML.getConfigurationSection("mounts." + key);
        Color color = Color.valueOf(mountSection.getString("color", "BROWN"));
        Style style = Style.valueOf(mountSection.getString("style", "NONE"));
        int customModelData = mountSection.getInt("model-id", -1);
        if (customModelData == -1) {
          Bukkit.getLogger().warning("[Strife] (Mounts) Invalid model-id for " + key);
          continue;
        }
        String meModel = mountSection.getString("me-model", null);
        String name = PaletteUtil.color(mountSection.getString("name", "No Name"));
        List<String> lore = PaletteUtil.color(mountSection.getStringList("lore"));
        float speed = (float) mountSection.getDouble("speed");
        float walkAnimationSpeed = (float) mountSection.getDouble("walk-animation-speed", 1.0);
        boolean flying = mountSection.getBoolean("flying", false);
        String flyani = mountSection.getString("fly-animation", null);
        String launch = mountSection.getString("launch-animation", null);
        String land = mountSection.getString("land-animation", null);
        playerMountManager.loadMount(
            new LoadedMount(key, customModelData, name, lore, meModel, color, style, speed,
                walkAnimationSpeed, flying, flyani, launch, land, true));
        playerMountManager.loadMount(
            new LoadedMount("untrad-" + key, 1000 + customModelData, name, lore, meModel,
                color, style, speed, walkAnimationSpeed, flying, flyani, launch, land, false));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void saveSpawners() {
    for (String spawnerId : spawnerYAML.getKeys(false)) {
      Spawner spawner = spawnerManager.getSpawnerMap().get(spawnerId);
      if (spawner == null) {
        Objects.requireNonNull(
                Objects.requireNonNull(spawnerYAML.getConfigurationSection(spawnerId)).getParent())
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
      spawnerYAML.set(spawnerId + ".location.x-dir", spawner.getLocation().getDirection().getX());
      spawnerYAML.set(spawnerId + ".location.y-dir", spawner.getLocation().getDirection().getY());
      spawnerYAML.set(spawnerId + ".location.z-dir", spawner.getLocation().getDirection().getZ());

      spawnerYAML.set(spawnerId + ".start-time", spawner.getStartTime());
      spawnerYAML.set(spawnerId + ".end-time", spawner.getEndTime());
      LogUtil.printDebug("Saved spawner " + spawnerId + ".");
    }
    spawnerYAML.save();
  }

  public SnazzyPartiesHook getSnazzyPartiesHook() {
    return snazzyPartiesHook;
  }

  public DataStorage getStorage() {
    return storage;
  }

  public ParticleTask getParticleTask() {
    return particleTask;
  }

  public MasterConfiguration getSettings() {
    return settings;
  }

  public AbilityMenu getAbilityPicker() {
    return abilitySubcategoryMenu;
  }

  public AbilitySubmenu getSubmenu(String name) {
    return abilitySubmenus.get(name);
  }

  public PathMenu getPathMenu(Path path) {
    return pathMenus.get(path);
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
