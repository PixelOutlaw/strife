package land.face.strife.util;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import land.face.learnin.objects.LoadedKnowledge;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.conditions.Condition;
import land.face.strife.data.conditions.Condition.CompareTarget;
import land.face.strife.data.conditions.Condition.Comparison;
import land.face.strife.data.conditions.Condition.ConditionUser;
import land.face.strife.data.pojo.SkillLevelData;
import land.face.strife.listeners.SwingListener;
import land.face.strife.stats.StrifeStat;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerDataUtil {

  private static final Map<UUID, Set<Player>> NEARBY_PLAYER_CACHE = new HashMap<>();
  private static StrifePlugin plugin;
  
  public static void refresh(StrifePlugin refreshedPlugin) {
    plugin = refreshedPlugin;
  }
  
  public static LoadedKnowledge loadMobKnowledge(String key, int weight,
      ConfigurationSection knowledgeSection) {
    String name = PaletteUtil.color(knowledgeSection.getString("name",
        FaceColor.GRAY.getColor() + key + "(WORK IN PROGRESS)"));
    String lore1 = PaletteUtil.color("|red||b||ul|Mob Info (Offense)\n\n" +
        knowledgeSection.getString("lore1", "|black|Sadly, there's nothing here!"));
    String lore2 = PaletteUtil.color("|red||b||ul|Mob Info (Defense)\n\n" +
        knowledgeSection.getString("lore2", "|black|Sadly, there's nothing here!"));
    String lore3 = PaletteUtil.color("|red||b||ul|Mob Info (Stats)\n\n" +
        knowledgeSection.getString("lore3", "|black|Sadly, there's nothing here!"));
    List<String> desc = new ArrayList<>();
    desc.add("");
    desc.add("|white|Knowledge Type: |red|Combat");
    desc.add("|white|Enemy Level: |yellow|" + weight);
    desc.add("");
    desc.add("|gray|Click to view what you've");
    desc.add("|gray|learned from this enemy!");
    desc.add("");
    int threshold1 = knowledgeSection.getInt("rank-1", 10);
    int threshold2 = knowledgeSection.getInt("rank-2", 100);
    int threshold3 = knowledgeSection.getInt("rank-3", 1000);
    String category = knowledgeSection.getString("type", "");
    LoadedKnowledge mobKnowledge = new LoadedKnowledge(
        key, name, weight, threshold1, threshold2, threshold3,
        lore1, lore2, lore3, PaletteUtil.color(desc));
    mobKnowledge.setSource("strife");
    switch (category) {
      case "boss" -> mobKnowledge.setCategory("bosses");
      case "elite" -> mobKnowledge.setCategory("elites");
      default -> mobKnowledge.setCategory("mobs");
    }
    return mobKnowledge;
  }

  public static LoadedKnowledge loadModKnowledge(String key, int weight,
      ConfigurationSection knowledgeSection) {
    String name = PaletteUtil.color(knowledgeSection.getString("name", FaceColor.GRAY.getColor() + key + "(WORK IN PROGRESS)"));
    String lore1 = PaletteUtil.color(knowledgeSection.getString("lore1", "|black|Sadly, there's nothing here!"));
    String lore2 = PaletteUtil.color(knowledgeSection.getString("lore2", "|black|Sadly, there's nothing here!"));
    String lore3 = PaletteUtil.color(knowledgeSection.getString("lore3", "|black|Sadly, there's nothing here!"));
    List<String> desc = PaletteUtil.color(knowledgeSection.getStringList("desc"));
    int threshold1 = knowledgeSection.getInt("rank-1", 10);
    int threshold2 = knowledgeSection.getInt("rank-2", 100);
    int threshold3 = knowledgeSection.getInt("rank-3", 1000);
    LoadedKnowledge mobKnowledge = new LoadedKnowledge(
        key, name, weight, threshold1, threshold2, threshold3,
        lore1, lore2, lore3, PaletteUtil.color(desc));
    mobKnowledge.setSource("strife");
    mobKnowledge.setCategory("mods");
    return mobKnowledge;
  }

  public static boolean isGlowEnabled(Player player) {
    return plugin.getChampionManager().getChampion(player).getSaveData().isGlowEnabled();
  }

  public static boolean isGlowEnabled(StrifeMob mob) {
    return mob.getChampion() != null && mob.getChampion().getSaveData().isGlowEnabled();
  }

  public static boolean isGlowEnabled(Champion champion) {
    return champion.getSaveData().isGlowEnabled();
  }

  public static void restoreHealth(LivingEntity le, double amount) {
    DamageUtil.restoreHealth(le, amount);
  }

  public static void restoreEnergy(LivingEntity le, float amount) {
    if (amount < 0.1) {
      return;
    }
    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(le);
    if (mob != null) {
      StatUtil.changeEnergy(mob, amount);
    }
  }

  public static void restoreHealthOverTime(LivingEntity le, float amount, int ticks) {
    if (amount < 0.1) {
      return;
    }
    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(le);
    if (mob != null) {
      mob.addHealingOverTime(amount, ticks);
    }
  }

  public static void restoreEnergyOverTime(LivingEntity le, float amount, int ticks) {
    if (amount <= 0.1) {
      return;
    }
    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(le);
    if (mob != null) {
      mob.addEnergyOverTime(amount, ticks);
    }
  }

  public static void swingHand(LivingEntity entity, EquipmentSlot slot, long delay) {
    if (delay == 0) {
      swing(entity, slot);
      return;
    }
    Bukkit.getScheduler().runTaskLater(plugin, () -> swing(entity, slot), delay);
  }

  private static void swing(LivingEntity entity, EquipmentSlot slot) {
    if (slot == EquipmentSlot.HAND) {
      SwingListener.spoofSwing(entity.getUniqueId());
      entity.swingMainHand();
      Bukkit.getScheduler().runTaskLater(plugin, () ->
          SwingListener.removeSwing(entity.getUniqueId()), 1L);
    } else if (slot == EquipmentSlot.OFF_HAND) {
      SwingListener.spoofSwing(entity.getUniqueId());
      entity.swingOffHand();
      Bukkit.getScheduler().runTaskLater(plugin, () ->
          SwingListener.removeSwing(entity.getUniqueId()), 1L);
    }
  }

  public static Set<Player> getCachedNearbyPlayers(LivingEntity le) {
    if (NEARBY_PLAYER_CACHE.containsKey(le.getUniqueId())) {
      return NEARBY_PLAYER_CACHE.get(le.getUniqueId());
    }
    Set<Player> players = new HashSet<>();
    for (org.bukkit.entity.Entity entity : le.getWorld()
        .getNearbyEntities(le.getLocation(), 40, 40, 40, entity -> entity instanceof Player)) {
      players.add((Player) entity);
    }
    NEARBY_PLAYER_CACHE.put(le.getUniqueId(), players);
    return players;
  }

  public static void clearNearbyPlayerCache() {
    NEARBY_PLAYER_CACHE.clear();
  }

  public static boolean areConditionsMet(StrifeMob caster, StrifeMob target, Set<Condition> conditions) {
    for (Condition condition : conditions) {
      EntityType casterType = caster.getEntity().getType();
      if (casterType == EntityType.PLAYER && condition.getConditionUser() == ConditionUser.MOB) {
        continue;
      }
      if (casterType != EntityType.PLAYER && condition.getConditionUser() == ConditionUser.PLAYER) {
        continue;
      }
      if (target == null) {
        if (condition.getCompareTarget() == CompareTarget.OTHER) {
          LogUtil.printDebug("-- Skipping " + condition + " - null target, OTHER compareTarget");
          continue;
        }
      }
      if (condition.isMet(caster, target) == condition.isInverted()) {
        LogUtil.printDebug("-- Skipping, condition " + condition + " not met!");
        return false;
      }
    }
    return true;
  }

  public static void updatePlayerEquipment(Player player) {
    plugin.getStrifeMobManager().updateEquipmentStats(StrifePlugin
        .getInstance().getStrifeMobManager().getStatMob(player));
  }

  public static void playExpSound(Player player) {
    player.playSound(player.getEyeLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.2f,
        0.8f + (float) StrifePlugin.RNG.nextFloat() * 0.4f);
  }

  // TODO: Something less stupid, this shouldn't be in this Util
  public static boolean conditionCompare(Comparison comparison, double val1, double val2) {
    return switch (comparison) {
      case GREATER_THAN -> val1 > val2;
      case LESS_THAN -> val1 < val2;
      case EQUAL -> val1 == val2;
      case NONE -> throw new IllegalArgumentException("Compare condition is NONE! Invalid usage!");
    };
  }

  public static int getMaxItemDestroyLevel(Player player) {
    return getMaxItemDestroyLevel(
        plugin.getChampionManager().getChampion(player)
            .getLifeSkillLevel(LifeSkillType.CRAFTING));
  }

  private static int getMaxItemDestroyLevel(int craftLvl) {
    return 10 + (int) Math.floor((double) craftLvl / 3) * 5;
  }

  public static int getMaxCraftItemLevel(Player player) {
    return getMaxCraftItemLevel(
        plugin.getChampionManager().getChampion(player)
            .getLifeSkillLevel(LifeSkillType.CRAFTING));
  }

  public static int getMaxCraftItemLevel(int craftLvl) {
    return 5 + (int) Math.floor((double) craftLvl / 5) * 8;
  }

  public static String getName(LivingEntity livingEntity) {
    if (livingEntity instanceof Player) {
      return ((Player) livingEntity).getDisplayName();
    }
    return livingEntity.getCustomName() == null ? livingEntity.getName()
        : livingEntity.getCustomName();
  }

  public static SkillLevelData getSkillLevels(Player player, LifeSkillType type, boolean forceUpdate) {
    return getSkillLevels(plugin.getStrifeMobManager().getStatMob(player), type, forceUpdate);
  }

  public static SkillLevelData getSkillLevels(StrifeMob mob, LifeSkillType type, boolean forceUpdate) {
    if (forceUpdate) {
      mob.getChampion().recombineCache(plugin);
    }
    SkillLevelData data = new SkillLevelData();
    data.setLevel(mob.getChampion().getLifeSkillLevel(type));
    switch (type) {
      case SNEAK -> data.setLevelWithBonus(
          (int) (data.getLevel() + StatUtil.getStat(mob, StrifeStat.SNEAK_SKILL)));
      case CRAFTING -> data.setLevelWithBonus(
          (int) (data.getLevel() + StatUtil.getStat(mob, StrifeStat.CRAFT_SKILL)));
      case ENCHANTING -> data.setLevelWithBonus(
          (int) (data.getLevel() + StatUtil.getStat(mob, StrifeStat.ENCHANT_SKILL)));
      default -> data.setLevelWithBonus(data.getLevel());
    }
    return data;
  }

  public static int getLifeSkillLevel(Player player, LifeSkillType type) {
    return getLifeSkillLevel(plugin.getChampionManager()
        .getChampion(player), type);
  }

  public static int getLifeSkillLevel(Champion champion, LifeSkillType type) {
    return champion.getLifeSkillLevel(type);
  }

  public static int getTotalSkillLevel(Player player) {
    int amount = 0;
    Champion champion = plugin.getChampionManager().getChampion(player);
    for (LifeSkillType type : LifeSkillType.types) {
      amount += champion.getLifeSkillLevel(type);
    }
    return amount;
  }

  public static float getLifeSkillExp(Player player, LifeSkillType type) {
    return plugin.getChampionManager().getChampion(player)
        .getLifeSkillExp(type);
  }

  public static float getLifeSkillExp(Champion champion, LifeSkillType type) {
    return champion.getLifeSkillExp(type);
  }

  public static float getLifeSkillMaxExp(Player player, LifeSkillType type) {
    int level = plugin.getChampionManager().getChampion(player)
        .getLifeSkillLevel(type);
    return plugin.getSkillExperienceManager().getMaxExp(type, level);
  }

  public static float getLifeSkillMaxExp(Champion champion, LifeSkillType type) {
    int level = champion.getLifeSkillLevel(type);
    return plugin.getSkillExperienceManager().getMaxExp(type, level);
  }

  public static int getLifeSkillExpToLevel(Champion champion, LifeSkillType type) {
    int level = champion.getLifeSkillLevel(type);
    return (int) (plugin.getSkillExperienceManager().getMaxExp(type, level)
        - champion.getLifeSkillExp(type));
  }

  public static float getSkillProgress(Champion champion, LifeSkillType type) {
    float progress = champion.getSaveData().getSkillExp(type) / plugin
        .getSkillExperienceManager().getMaxExp(type, champion.getSaveData().getSkillLevel(type));
    return Math.max(0.0f, Math.min(1.0f, progress));
  }

  public static <T> T getRandomFromCollection(Collection<T> coll) {
    int num = (int) (StrifePlugin.RNG.nextFloat() * coll.size());
    for (T t : coll) {
      if (--num < 0) {
        return t;
      }
    }
    throw new AssertionError();
  }
}
