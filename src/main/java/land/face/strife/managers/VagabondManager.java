package land.face.strife.managers;

import com.tealcube.minecraft.bukkit.facecore.utilities.ChunkUtil;
import info.faceland.loot.data.pdc.EquipmentData;
import info.faceland.loot.data.pdc.ItemDetails;
import info.faceland.loot.listeners.VagabondEquipListener;
import info.faceland.loot.utils.PersistHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import land.face.dinvy.windows.equipment.EquipmentMenu.DeluxeSlot;
import land.face.strife.StrifePlugin;
import land.face.strife.data.ItemDataBundle;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.VagabondClass;
import land.face.strife.data.ability.Ability;
import land.face.strife.data.ability.EntityAbilitySet;
import land.face.strife.data.ability.EntityAbilitySet.Phase;
import land.face.strife.data.ability.EntityAbilitySet.TriggerAbilityType;
import land.face.strife.data.champion.StrifeAttribute;
import land.face.strife.events.VagabondEquipEvent;
import land.face.strife.events.VagabondSpawnEvent;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.ItemUtil;
import land.face.strife.util.PlayerDataUtil;
import land.face.strife.util.SpecialStatusUtil;
import land.face.strife.util.StatUtil;
import lombok.Getter;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Skeleton;
import org.bukkit.inventory.ItemStack;

public class VagabondManager {

  private final StrifePlugin plugin;
  private final Map<String, VagabondClass> loadedVagabondClasses = new HashMap<>();
  private final List<String> vagabondNames = new ArrayList<>();
  @Getter
  private int minLevel;
  @Getter
  private float spawnChance;

  private final String vagabond = ChatColor.GOLD + "< " + ChatColor.GRAY + "Vagabond" + ChatColor.GOLD +  " >";

  public VagabondManager(StrifePlugin plugin) {
    this.plugin = plugin;
    spawnChance = (float) plugin.getSettings().getDouble("config.vagabonds.spawn-chance");
    minLevel = plugin.getSettings().getInt("config.vagabonds.minimum-level");
  }

  public Entity spawnVagabond(int level, Location location) {
    return Objects.requireNonNull(location.getWorld()).spawn(location,
        Skeleton.class, e -> vagabondSetup(e, null, level));
  }

  public Entity spawnVagabond(int level, String className, Location location) {
    return Objects.requireNonNull(location.getWorld()).spawn(location,
        Skeleton.class, e -> vagabondSetup(e, className, level));
  }

  private void vagabondSetup(Skeleton entity, String className, int level) {

    PlayerDisguise playerDisguise = new PlayerDisguise("vagabond1", vagabondNames.get(StrifePlugin.RNG.nextInt(vagabondNames.size())));
    playerDisguise.setReplaceSounds(true);
    playerDisguise.setName(vagabond);
    playerDisguise.setDynamicName(false);

    DisguiseAPI.disguiseEntity(entity, playerDisguise);

    entity.setRemoveWhenFarAway(true);
    ChunkUtil.setDespawnOnUnload(entity);

    // TODO: Goals
    // GoalPatcher.removeGoals((Mob) entity, uniqueEntity.getRemoveGoals());
    // GoalPatcher.addGoals((Mob) entity, uniqueEntity);

    SpecialStatusUtil.setWeakAggro(entity);

    AttributeInstance attributeInstance = entity.getAttribute(Attribute.FOLLOW_RANGE);
    if (attributeInstance != null) {
      attributeInstance.setBaseValue(27);
      for (AttributeModifier mod : attributeInstance.getModifiers()) {
        attributeInstance.removeModifier(mod);
      }
    }

    entity.setCanPickupItems(false);
    entity.setCustomName(vagabond);
    entity.setCustomNameVisible(true);
    ChunkUtil.setDespawnOnUnload(entity);

    VagabondClass mobClass;
    if (className == null || !loadedVagabondClasses.containsKey(className)) {
      mobClass = PlayerDataUtil.getRandomFromCollection(loadedVagabondClasses.values());
    } else  {
      mobClass = loadedVagabondClasses.get(className);
    }

    entity.getEquipment().clear();
    VagabondEquipEvent vagabondEquipEvent = new VagabondEquipEvent(entity, mobClass.getId(), level);
    Bukkit.getPluginManager().callEvent(vagabondEquipEvent);

    SpecialStatusUtil.setMobLevel(entity, level);
    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(entity);
    mob.setElite(true);

    Map<StrifeAttribute, Integer> vagabondAttributes = new HashMap<>();
    for (String attr : mobClass.getLevelsPerAttribute().keySet()) {
      int minValue = mobClass.getStartLevelPerAttribute().getOrDefault(attr, 0);
      if (level < minValue) {
        continue;
      }
      StrifeAttribute realAttr = plugin.getAttributeManager().getAttribute(attr);
      if (realAttr == null) {
        Bukkit.getLogger().warning("Invalid attribute " + attr + " for vagabond class " + mobClass.getId());
        continue;
      }
      vagabondAttributes.put(realAttr, (level - minValue) / mobClass.getLevelsPerAttribute().get(attr));
    }
    Map<StrifeStat, Float> attributeStats = StatUtil.buildStatsFromAttributes(vagabondAttributes);

    boolean dualWield = ItemUtil.isDualWield(entity.getEquipment());
    Map<StrifeStat, Float> equipmentStats = new HashMap<>();

    var vagabondData = VagabondEquipListener.VAGABONDS.get(entity);
    if (vagabondData == null) {
      entity.remove();
      return;
    }

    for (var dSlot : DeluxeSlot.values()) {
      String slot = dSlot.toString();
      var stack = vagabondData.getItems().get(slot);
      if (stack == null || stack.getType() == Material.AIR) {
        continue;
      }
      var statBundle = getItemData(stack);
      if (statBundle != null) {
        Map<StrifeStat, Float> newMap = new HashMap<>(statBundle.getStats());
        if (slot.equals("OFF_HAND") && dualWield) {
          statBundle.getStats().forEach((key, value) -> newMap.put(key, value * 0.65f));
        }
        equipmentStats = StatUpdateManager.combineMaps(equipmentStats, newMap);
      }
    }
    var handStack = vagabondData.getItems().get("HAND");
    if (handStack != null && handStack.getType() != Material.AIR) {
      var statBundle = getItemData(handStack);
      if (statBundle != null) {
        Map<StrifeStat, Float> newMap = new HashMap<>(statBundle.getStats());
        if (dualWield) {
          statBundle.getStats().forEach((key, value) -> newMap.put(key, value * 0.65f));
        }
        equipmentStats = StatUpdateManager.combineMaps(equipmentStats, newMap);
      }
    }

    Map<StrifeStat, Float> baseStats = plugin.getMonsterManager()
        .getBaseStats(EntityType.PLAYER, level);

    Map<StrifeStat, Float> totalStats = StatUpdateManager.combineMaps(
        baseStats,
        attributeStats,
        equipmentStats
    );
    totalStats.put(StrifeStat.MOVEMENT_SPEED, totalStats.get(StrifeStat.MOVEMENT_SPEED) * 2.5f);
    mob.setStats(totalStats);

    EntityAbilitySet entityAbilitySet = new EntityAbilitySet();
    Set<Ability> newAbilities = new HashSet<>();
    newAbilities.add(plugin.getAbilityManager().getAbility(
        PlayerDataUtil.getRandomFromCollection(mobClass.getPossibleAbilitiesA())));
    newAbilities.add(plugin.getAbilityManager().getAbility(
        PlayerDataUtil.getRandomFromCollection(mobClass.getPossibleAbilitiesB())));
    newAbilities.add(plugin.getAbilityManager().getAbility(
        PlayerDataUtil.getRandomFromCollection(mobClass.getPossibleAbilitiesC())));
    Map<Phase, Set<Ability>> abilities = entityAbilitySet.getAbilities(TriggerAbilityType.TIMER);
    for (Phase phase : EntityAbilitySet.PHASES) {
      abilities.put(phase, newAbilities);
    }
    mob.setAbilitySet(entityAbilitySet);

    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> {
      StatUtil.getStat(mob, StrifeStat.BARRIER);
      mob.restoreBarrier(200000);
      plugin.getStatUpdateManager().updateAllAttributes(mob);
      plugin.getAbilityManager().abilityCast(mob, TriggerAbilityType.PHASE_SHIFT);
      plugin.getAbilityManager().startAbilityTimerTask(mob);
    }, 1L);

    VagabondSpawnEvent vagabondSpawnEvent = new VagabondSpawnEvent(mob);
    Bukkit.getPluginManager().callEvent(vagabondSpawnEvent);
  }

  private ItemDataBundle getItemData(ItemStack stack) {
    var bundle = new ItemDataBundle();
    EquipmentData data = PersistHelper.getEquipmentData(stack);
    if (data != null) {
      ItemDetails details = data.buildItemDetails();
      bundle.setStats(details.getStats());
      bundle.setTraits(details.getTraits());
      bundle.setAbilities(details.getLoreAbilities());
      return bundle;
    }
    return null;
  }

  public void loadClasses(ConfigurationSection vSection) {
    vagabondNames.addAll(vSection.getStringList("player-names"));
    spawnChance = (float) vSection.getDouble("spawn-chance");
    minLevel = vSection.getInt("minimum-level", 1);
    ConfigurationSection classesSection = vSection.getConfigurationSection("classes");
    for (String key : classesSection.getKeys(false)) {
      ConfigurationSection classSection = classesSection.getConfigurationSection(key);
      VagabondClass vClass = new VagabondClass(key);
      vClass.getPossibleAbilitiesA().addAll(classSection.getStringList("ability-pool-a"));
      vClass.getPossibleAbilitiesB().addAll(classSection.getStringList("ability-pool-b"));
      vClass.getPossibleAbilitiesC().addAll(classSection.getStringList("ability-pool-c"));
      ConfigurationSection attrSection = classSection.getConfigurationSection("attributes");
      if (attrSection == null) {
        Bukkit.getLogger().warning("[Strife] No attribute for vagabond class " + key);
        Bukkit.getLogger().warning("[Strife] Skipping...");
        continue;
      }
      for (String attr : attrSection.getKeys(false)) {
        vClass.getLevelsPerAttribute().put(attr, attrSection.getInt(attr + ".every-x-levels", 0));
        vClass.getStartLevelPerAttribute().put(attr, attrSection.getInt(attr + ".min-level", 0));
      }
      loadedVagabondClasses.put(key, vClass);
    }
  }
}
