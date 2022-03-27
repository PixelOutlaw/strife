package land.face.strife.managers;

import static org.bukkit.attribute.Attribute.GENERIC_FOLLOW_RANGE;

import com.tealcube.minecraft.bukkit.facecore.utilities.ChunkUtil;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import io.pixeloutlaw.minecraft.spigot.config.VersionedSmartYamlConfiguration;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.UniqueEntity;
import land.face.strife.data.VagabondClass;
import land.face.strife.data.ability.EntityAbilitySet;
import land.face.strife.data.ability.EntityAbilitySet.TriggerAbilityType;
import land.face.strife.data.champion.StrifeAttribute;
import land.face.strife.data.effects.Effect;
import land.face.strife.data.effects.StrifeParticle;
import land.face.strife.events.UniqueSpawnEvent;
import land.face.strife.events.VagabondEquipEvent;
import land.face.strife.events.VagabondSpawnEvent;
import land.face.strife.patch.GoalPatcher;
import land.face.strife.stats.StrifeStat;
import land.face.strife.tasks.ItemPassengerTask;
import land.face.strife.util.DisguiseUtil;
import land.face.strife.util.ItemUtil;
import land.face.strife.util.LogUtil;
import land.face.strife.util.PlayerDataUtil;
import land.face.strife.util.SpecialStatusUtil;
import land.face.strife.util.StatUtil;
import lombok.Getter;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Hoglin;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Raider;
import org.bukkit.entity.Shulker;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.entity.Vindicator;
import org.bukkit.entity.Wolf;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class VagabondManager {

  private final StrifePlugin plugin;
  private final Map<String, VagabondClass> loadedVagabondClasses = new HashMap<>();
  private final List<String> vagabondNames = new ArrayList<>();
  @Getter
  private int minimumLevel;
  @Getter
  private float spawnChance;

  private final String vagabond = ChatColor.DARK_RED + "< " + ChatColor.RED + "Vagabond" + ChatColor.DARK_RED +  " >";

  private final Random random = new Random();

  public VagabondManager(StrifePlugin plugin) {
    this.plugin = plugin;
    spawnChance = (float) plugin.getSettings().getDouble("config.vagabonds.spawn-chance");
    minimumLevel = plugin.getSettings().getInt("config.vagabonds.minimum-level");
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

    PlayerDisguise playerDisguise = new PlayerDisguise("vagabond1", vagabondNames
        .get(random.nextInt(vagabondNames.size())));
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

    AttributeInstance attributeInstance = entity.getAttribute(GENERIC_FOLLOW_RANGE);
    if (attributeInstance != null) {
      attributeInstance.setBaseValue(32);
      for (AttributeModifier mod : attributeInstance.getModifiers()) {
        attributeInstance.removeModifier(mod);
      }
    }

    entity.setCanPickupItems(false);

    entity.setCustomName(vagabond);
    entity.setCustomNameVisible(true);

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
    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(entity, EntityType.PLAYER);

    Map<StrifeAttribute, Integer> vagabondAttributes = new HashMap<>();
    for (String attr : mobClass.getLevelsPerAttribute().keySet()) {
      int minValue = mobClass.getStartLevelPerAttribute().getOrDefault(attr, 0);
      if (level < minValue) {
        continue;
      }
      StrifeAttribute realAttr = plugin.getAttributeManager().getAttribute(attr);
      if (realAttr == null) {
        Bukkit.getLogger().warning("Invalid attribute " + attr +
            " for vagabond class " + mobClass.getId());
        continue;
      }
      vagabondAttributes.put(realAttr,
          (level - minValue) / mobClass.getLevelsPerAttribute().get(attr));
    }
    Map<StrifeStat, Float> attributeStats = StatUtil.buildStatsFromAttributes(vagabondAttributes);

    boolean dualWield = ItemUtil.isDualWield(entity.getEquipment());
    Map<StrifeStat, Float> equipmentStats = new HashMap<>();
    for (EquipmentSlot slot : EquipmentSlot.values()) {
      switch (slot) {
        case HAND, OFF_HAND -> {
          ItemStack stack = entity.getEquipment().getItem(slot);
          if (stack == null || stack.getType() == Material.AIR) {
            continue;
          }
          Map<StrifeStat, Float> newMap = plugin.getStatUpdateManager()
              .getItemStats(stack, dualWield ? 0.8f : 1.0f);
          equipmentStats = StatUpdateManager.combineMaps(equipmentStats, newMap);
        }
        default -> {
          ItemStack stack = entity.getEquipment().getItem(slot);
          if (stack == null || stack.getType() == Material.AIR) {
            continue;
          }
          Map<StrifeStat, Float> newMap = plugin.getStatUpdateManager().getItemStats(stack);
          equipmentStats = StatUpdateManager.combineMaps(equipmentStats, newMap);
        }
      }
    }

    mob.setStats(StatUpdateManager.combineMaps(
        mob.getBaseStats(),
        attributeStats,
        equipmentStats
    ));

    ChunkUtil.setDespawnOnUnload(mob.getEntity());
    mob.setCharmImmune(true);

    // TODO: abilities
    // mob.setAbilitySet(new EntityAbilitySet(uniqueEntity.getAbilitySet()));

    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> {
      plugin.getStatUpdateManager().updateVanillaAttributes(mob);
      StatUtil.getStat(mob, StrifeStat.BARRIER);
      mob.restoreBarrier(200000);
      plugin.getAbilityManager().abilityCast(mob, TriggerAbilityType.PHASE_SHIFT);
      plugin.getAbilityManager().startAbilityTimerTask(mob);
    }, 0L);

    VagabondSpawnEvent vagabondSpawnEvent = new VagabondSpawnEvent(mob);
    Bukkit.getPluginManager().callEvent(vagabondSpawnEvent);
  }

  public void loadClasses(ConfigurationSection vSection) {
    vagabondNames.addAll(vSection.getStringList("player-names"));
    spawnChance = (float) vSection.getDouble("spawn-chance");
    minimumLevel = vSection.getInt("minimum-level", 1);
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
