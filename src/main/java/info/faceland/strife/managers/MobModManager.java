package info.faceland.strife.managers;

import info.faceland.strife.data.MobMod;
import info.faceland.strife.data.StrifeMob;
import info.faceland.strife.data.ability.EntityAbilitySet;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.util.ItemUtil;
import info.faceland.strife.util.LogUtil;
import info.faceland.strife.util.StatUtil;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public class MobModManager {

  private final Map<String, MobMod> loadedMods = new HashMap<>();
  private static Random random = new Random();

  public void applyMobMod(StrifeMob strifeMob, MobMod mobMod) {
    if (mobMod.getAbilitySet() != null) {
      if (strifeMob.getAbilitySet() == null) {
        strifeMob.setAbilitySet(new EntityAbilitySet(mobMod.getAbilitySet()));
      } else {
        EntityAbilitySet.mergeAbilitySets(mobMod.getAbilitySet(), strifeMob.getAbilitySet());
      }
    }
    if (!mobMod.getEquipment().isEmpty()) {
      ItemUtil.delayedEquip(mobMod.getEquipment(), strifeMob.getEntity());
    }
    int level = StatUtil.getMobLevel(strifeMob.getEntity());
    Map<StrifeStat, Float> stats = StatUpdateManager
        .combineMaps(strifeMob.getBaseStats(), mobMod.getBaseStats());
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

  public void loadMobMod(String id, ConfigurationSection cs) {
    MobMod mod = new MobMod();
    mod.setId(id);
    mod.setPrefix(cs.getString("prefix"));
    mod.setSuffix(cs.getString("suffix"));
    mod.setWeight(cs.getInt("weight", 0));
    mod.setBaseStats(StatUtil.getStatMapFromSection(cs.getConfigurationSection("base-stats")));
    mod.setPerLevelStats(StatUtil.getStatMapFromSection(cs.getConfigurationSection("per-level-stats")));
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
