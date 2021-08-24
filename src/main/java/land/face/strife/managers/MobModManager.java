package land.face.strife.managers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import io.pixeloutlaw.minecraft.spigot.config.MasterConfiguration;
import land.face.strife.StrifePlugin;
import land.face.strife.data.MobMod;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.ability.EntityAbilitySet;
import land.face.strife.data.champion.EquipmentCache;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.ItemUtil;
import land.face.strife.util.LogUtil;
import land.face.strife.util.SpecialStatusUtil;
import land.face.strife.util.StatUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class MobModManager {

  private final EntityEquipmentManager equipmentManager;
  private final Map<String, MobMod> loadedMods = new HashMap<>();

  private double MOB_MOD_UP_CHANCE;
  public static int MOB_MOD_MAX_MODS;

  private static Random random = new Random();

  public MobModManager(MasterConfiguration settings, EntityEquipmentManager equipmentManager) {
    this.equipmentManager = equipmentManager;
    MOB_MOD_UP_CHANCE = settings.getDouble("config.leveled-monsters.add-mod-chance", 0.1);
    MOB_MOD_MAX_MODS = settings.getInt("config.leveled-monsters.max-mob-mods", 4);
  }

  public void doModApplication(StrifeMob mob, int max) {
    String prefix = "";
    int prefixWeight = Integer.MAX_VALUE;
    Set<MobMod> mods = getRandomMods(mob.getEntity(), mob.getEntity().getLocation(), getModCount(max));
    for (MobMod mod : mods) {
      applyMobMod(mob, mod);
      if (mod.getWeight() < prefixWeight && StringUtils.isNotBlank(mod.getPrefix())) {
        prefix = mod.getPrefix() + " ";
        prefixWeight = mod.getWeight();
      }
      SpecialStatusUtil.setDespawnOnUnload(mob.getEntity());
      mob.getMods().add(mod.getId());
    }
    mob.getEntity()
        .setCustomName(getPrefixColor(mods.size()) + prefix + ChatColor.WHITE + mob.getEntity().getCustomName());
  }

  public void applyMobMod(StrifeMob strifeMob, MobMod mobMod) {
    if (mobMod.getAbilitySet() != null) {
      if (strifeMob.getAbilitySet() == null) {
        strifeMob.setAbilitySet(new EntityAbilitySet(mobMod.getAbilitySet()));
      } else {
        EntityAbilitySet.mergeAbilitySets(mobMod.getAbilitySet(), strifeMob.getAbilitySet());
      }
    }
    if (!mobMod.getEquipment().isEmpty()) {
      Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () ->
              ItemUtil.equipMob(mobMod.getEquipment(), strifeMob.getEntity(), false, true), 5L);
    }
    int level = StatUtil.getMobLevel(strifeMob.getEntity());
    Map<StrifeStat, Float> stats = StatUpdateManager.combineMaps(strifeMob.getBaseStats(), mobMod.getBaseStats());
    for (StrifeStat stat : mobMod.getPerLevelStats().keySet()) {
      if (stats.containsKey(stat)) {
        stats.put(stat, stats.get(stat) + mobMod.getPerLevelStats().get(stat) * level);
        continue;
      }
      stats.put(stat, mobMod.getPerLevelStats().get(stat) * level);
    }
    strifeMob.setStats(stats);
  }

  public Set<MobMod> getRandomMods(LivingEntity livingEntity, Location location, int amount) {
    Set<MobMod> mods = new HashSet<>();
    if (amount == 0) {
      return mods;
    }
    for (MobMod mod : loadedMods.values()) {
      if (isModApplicable(mod, livingEntity, location)) {
        mods.add(mod);
      }
    }
    Set<MobMod> returnMods = new HashSet<>();
    if (mods.size() < amount) {
      returnMods.addAll(mods);
      return returnMods;
    }
    while (amount > 0) {
      MobMod selectedMod = null;
      int maxWeight = 0;
      for (MobMod mod : mods) {
        maxWeight += mod.getWeight();
      }
      int randWeight = random.nextInt(maxWeight + 1);
      int curWeight = 0;
      for (MobMod mod : mods) {
        curWeight += mod.getWeight();
        if (randWeight < curWeight) {
          selectedMod = mod;
          break;
        }
      }
      if (selectedMod == null) {
        LogUtil.printDebug("No mods found when attempting to select a mod...");
        amount--;
        continue;
      }
      mods.remove(selectedMod);
      returnMods.add(selectedMod);
      amount--;
    }
    return returnMods;
  }

  private ChatColor getPrefixColor(int modCount) {
    switch (modCount) {
      case 0:
        return ChatColor.WHITE;
      case 1:
        return ChatColor.BLUE;
      case 2:
        return ChatColor.DARK_PURPLE;
      case 3:
        return ChatColor.RED;
      default:
        return ChatColor.WHITE;
    }
  }

  private int getModCount(int max) {
    int mods = 0;
    max = Math.min(max, MOB_MOD_MAX_MODS);
    for (int i = 0; i < max; i++) {
      if (random.nextDouble() < MOB_MOD_UP_CHANCE) {
        mods++;
        continue;
      }
      break;
    }
    return mods;
  }

  public void loadMobMod(String id, ConfigurationSection cs) {
    MobMod mod = new MobMod();
    mod.setId(id);
    mod.setPrefix(cs.getString("prefix"));
    mod.setWeight(cs.getInt("weight", 0));
    mod.setBaseStats(StatUtil.getStatMapFromSection(cs.getConfigurationSection("base-stats")));
    mod.setPerLevelStats(
        StatUtil.getStatMapFromSection(cs.getConfigurationSection("per-level-stats")));
    for (EquipmentSlot slot : EquipmentCache.ITEM_SLOTS) {
      String equipmentId = cs.getString("equipment." + slot, null);
      if (equipmentId != null) {
        ItemStack stack = equipmentManager.getItem(equipmentId);
        if (stack == null) {
          continue;
        }
        mod.getEquipment().put(slot, stack);
      }
    }
    mod.setAbilitySet(new EntityAbilitySet(cs.getConfigurationSection("abilities")));
    for (String s : cs.getStringList("required-biome")) {
      mod.addValidBiome(Biome.valueOf(s));
    }
    for (String s : cs.getStringList("required-entity-type")) {
      mod.addValidEntity(EntityType.valueOf(s));
    }
    loadedMods.put(id, mod);
  }

  private boolean isModApplicable(MobMod mod, LivingEntity le, Location location) {
    if (le == null) {
      LogUtil.printError("Cannot set mods on a null entity... something is very wrong!");
      return false;
    }
    if (mod.getValidEntities() != null && !mod.getValidEntities().isEmpty()) {
      if (!mod.getValidEntities().contains(le.getType())) {
        return false;
      }
    }
    if (mod.getValidBiomes() != null && !mod.getValidBiomes().isEmpty()) {
      if (!mod.getValidBiomes().contains(location.getBlock().getBiome())) {
        return false;
      }
    }
    return true;
  }
}
