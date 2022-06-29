package land.face.strife.util;

import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
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
import land.face.strife.listeners.SwingListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerDataUtil {

  private static final Map<UUID, Set<Player>> NEARBY_PLAYER_CACHE = new HashMap<>();

  public static LoadedKnowledge loadMobKnowledge(String key, int weight,
      ConfigurationSection knowledgeSection) {
    String name = TextUtils.color(knowledgeSection.getString("name",
        ChatColor.GRAY + key + "(WORK IN PROGRESS)"));
    String lore1 = TextUtils.color("&4&l&nMob Info (Offense)\n\n" +
        knowledgeSection.getString("lore1", "&0Sadly, there's nothing here!"));
    String lore2 = TextUtils.color("&4&l&nMob Info (Defense)\n\n" +
        knowledgeSection.getString("lore1", "&0Sadly, there's nothing here!"));
    String lore3 = TextUtils.color("&4&l&nMob Info (Stats)\n\n" +
        knowledgeSection.getString("lore1", "&0Sadly, there's nothing here!"));
    List<String> desc = new ArrayList<>();
    desc.add("");
    desc.add("&fKnowledge Type: &cCombat");
    desc.add("&fEnemy Level: &e" + weight);
    desc.add("");
    desc.add("&7Click to view what you've");
    desc.add("&7learned from this enemy!");
    desc.add("");
    int threshold1 = knowledgeSection.getInt("rank-1", 10);
    int threshold2 = knowledgeSection.getInt("rank-2", 100);
    int threshold3 = knowledgeSection.getInt("rank-3", 1000);
    LoadedKnowledge mobKnowledge = new LoadedKnowledge(
        key, name, weight, threshold1, threshold2, threshold3,
        lore1, lore2, lore3, TextUtils.color(desc));
    mobKnowledge.setSource("strife");
    return mobKnowledge;
  }

  public static LoadedKnowledge loadModKnowledge(String key, int weight,
      ConfigurationSection knowledgeSection) {
    String name = TextUtils.color(knowledgeSection.getString("name", ChatColor.GRAY + key + "(WORK IN PROGRESS)"));
    String lore1 = TextUtils.color(knowledgeSection.getString("lore1", "&0Sadly, there's nothing here!"));
    String lore2 = TextUtils.color(knowledgeSection.getString("lore1", "&0Sadly, there's nothing here!"));
    String lore3 = TextUtils.color(knowledgeSection.getString("lore1", "&0Sadly, there's nothing here!"));
    List<String> desc = TextUtils.color(knowledgeSection.getStringList("desc"));
    int threshold1 = knowledgeSection.getInt("rank-1", 10);
    int threshold2 = knowledgeSection.getInt("rank-2", 100);
    int threshold3 = knowledgeSection.getInt("rank-3", 1000);
    LoadedKnowledge mobKnowledge = new LoadedKnowledge(
        key, name, weight, threshold1, threshold2, threshold3,
        lore1, lore2, lore3, TextUtils.color(desc));
    mobKnowledge.setSource("strife");
    return mobKnowledge;
  }

  public static boolean isGlowEnabled(Player player) {
    return StrifePlugin.getInstance().getChampionManager().getChampion(player).getSaveData().isGlowEnabled();
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
    StrifeMob mob = StrifePlugin.getInstance().getStrifeMobManager().getStatMob(le);
    if (mob != null) {
      StatUtil.changeEnergy(mob, amount);
    }
  }

  public static void restoreHealthOverTime(LivingEntity le, float amount, int ticks) {
    StrifeMob mob = StrifePlugin.getInstance().getStrifeMobManager().getStatMob(le);
    if (mob != null) {
      mob.addHealingOverTime(amount, ticks);
    }
  }

  public static void restoreEnergyOverTime(LivingEntity le, float amount, int ticks) {
    StrifeMob mob = StrifePlugin.getInstance().getStrifeMobManager().getStatMob(le);
    if (mob != null) {
      mob.addEnergyOverTime(amount, ticks);
    }
  }

  public static void swingHand(LivingEntity entity, EquipmentSlot slot, long delay) {
    if (delay == 0) {
      swing(entity, slot);
      return;
    }
    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> swing(entity, slot), delay);
  }

  private static void swing(LivingEntity entity, EquipmentSlot slot) {
    if (slot == EquipmentSlot.HAND) {
      SwingListener.spoofSwing(entity.getUniqueId());
      entity.swingMainHand();
      Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () ->
          SwingListener.removeSwing(entity.getUniqueId()), 0L);
    } else if (slot == EquipmentSlot.OFF_HAND) {
      SwingListener.spoofSwing(entity.getUniqueId());
      entity.swingOffHand();
      Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () ->
          SwingListener.removeSwing(entity.getUniqueId()), 0L);
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
    StrifePlugin.getInstance().getStrifeMobManager().updateEquipmentStats(StrifePlugin
        .getInstance().getStrifeMobManager().getStatMob(player));
  }

  public static void playExpSound(Player player) {
    player.playSound(player.getEyeLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.2f,
        0.8f + (float) Math.random() * 0.4f);
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
        StrifePlugin.getInstance().getChampionManager().getChampion(player)
            .getLifeSkillLevel(LifeSkillType.CRAFTING));
  }

  private static int getMaxItemDestroyLevel(int craftLvl) {
    return 10 + (int) Math.floor((double) craftLvl / 3) * 5;
  }

  public static int getMaxCraftItemLevel(Player player) {
    return getMaxCraftItemLevel(
        StrifePlugin.getInstance().getChampionManager().getChampion(player)
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

  public static double getEffectiveLifeSkill(Player player, LifeSkillType type,
      Boolean updateEquipment) {
    return getEffectiveLifeSkill(
        StrifePlugin.getInstance().getChampionManager().getChampion(player), type, updateEquipment);
  }

  public static double getEffectiveLifeSkill(Champion champion, LifeSkillType type,
      Boolean updateEquipment) {
    return champion.getEffectiveLifeSkillLevel(type, updateEquipment);
  }

  public static int getLifeSkillLevel(Player player, LifeSkillType type) {
    return getLifeSkillLevel(StrifePlugin.getInstance().getChampionManager()
        .getChampion(player), type);
  }

  public static int getLifeSkillLevel(Champion champion, LifeSkillType type) {
    return champion.getLifeSkillLevel(type);
  }

  public static int getTotalSkillLevel(Player player) {
    int amount = 0;
    Champion champion = StrifePlugin.getInstance().getChampionManager().getChampion(player);
    for (LifeSkillType type : LifeSkillType.types) {
      amount += champion.getLifeSkillLevel(type);
    }
    return amount;
  }

  public static float getLifeSkillExp(Player player, LifeSkillType type) {
    return StrifePlugin.getInstance().getChampionManager().getChampion(player)
        .getLifeSkillExp(type);
  }

  public static float getLifeSkillExp(Champion champion, LifeSkillType type) {
    return champion.getLifeSkillExp(type);
  }

  public static float getLifeSkillMaxExp(Player player, LifeSkillType type) {
    int level = StrifePlugin.getInstance().getChampionManager().getChampion(player)
        .getLifeSkillLevel(type);
    return StrifePlugin.getInstance().getSkillExperienceManager().getMaxExp(type, level);
  }

  public static float getLifeSkillMaxExp(Champion champion, LifeSkillType type) {
    int level = champion.getLifeSkillLevel(type);
    return StrifePlugin.getInstance().getSkillExperienceManager().getMaxExp(type, level);
  }

  public static int getLifeSkillExpToLevel(Champion champion, LifeSkillType type) {
    int level = champion.getLifeSkillLevel(type);
    return (int) (StrifePlugin.getInstance().getSkillExperienceManager().getMaxExp(type, level)
        - champion.getLifeSkillExp(type));
  }

  public static float getSkillProgress(Champion champion, LifeSkillType type) {
    float progress = champion.getSaveData().getSkillExp(type) / StrifePlugin.getInstance()
        .getSkillExperienceManager().getMaxExp(type, champion.getSaveData().getSkillLevel(type));
    return Math.max(0.0f, Math.min(1.0f, progress));
  }

  private static final Map<String, String> cachedSuperscript = new HashMap<>();

  public static String convertToSuperscript(String original) {
    if (cachedSuperscript.containsKey(original)) {
      return cachedSuperscript.get(original);
    }
    String s = original.toLowerCase();
    s = s
        .replaceAll("(?<!§)a", "ᵃ\uF801")
        .replaceAll("(?<!§)b", "ᵇ\uF801")
        .replaceAll("(?<!§)c", "ᶜ\uF801")
        .replaceAll("(?<!§)d", "ᵈ\uF801")
        .replaceAll("(?<!§)e", "ᵉ\uF801")
        .replaceAll("(?<!§)f", "ᶠ\uF801")
        .replaceAll("g", "ᵍ\uF801")
        .replaceAll("h", "ʰ\uF801")
        .replaceAll("i", "ᶦ\uF801")
        .replaceAll("j", "ʲ\uF801")
        .replaceAll("k", "ᵏ\uF801")
        .replaceAll("l", "ˡ\uF801")
        .replaceAll("m", "ᵐ\uF801")
        .replaceAll("n", "ⁿ\uF801")
        .replaceAll("o", "ᵒ\uF801")
        .replaceAll("p", "ᵖ\uF801")
        .replaceAll("q", "ᵠ\uF801")
        .replaceAll("r", "ʳ\uF801")
        .replaceAll("s", "ˢ\uF801")
        .replaceAll("t", "ᵗ\uF801")
        .replaceAll("u", "ᵘ\uF801")
        .replaceAll("v", "ᵛ\uF801")
        .replaceAll("w", "ʷ\uF801")
        .replaceAll("(?<!§)x", "ˣ\uF801")
        .replaceAll("y", "ʸ\uF801")
        .replaceAll("z", "ᶻ\uF801")
        .replaceAll("(?<!§)1", "¹\uF801")
        .replaceAll("(?<!§)2", "²\uF801")
        .replaceAll("(?<!§)3", "³\uF801")
        .replaceAll("(?<!§)4", "⁴\uF801")
        .replaceAll("(?<!§)5", "⁵\uF801")
        .replaceAll("(?<!§)6", "⁶\uF801")
        .replaceAll("(?<!§)7", "⁷\uF801")
        .replaceAll("(?<!§)8", "⁸\uF801")
        .replaceAll("(?<!§)9", "⁹\uF801")
        .replaceAll("(?<!§)0", "⁰\uF801")
        .replaceAll("-", "⁻\uF801")
        .replaceAll("/", "⃫\uF801")
        .replaceAll("\\(", "⁽\uF801")
        .replaceAll("\\)", "⁾\uF801")
        .replaceAll("'", "՚\uF801")
        .replaceAll("!", "ᵎ\uF801")
        .replaceAll("\\?", "ˀ\uF801");
    cachedSuperscript.put(original, s);
    return s;
  }

  public static <T> T getRandomFromCollection(Collection<T> coll) {
    int num = (int) (Math.random() * coll.size());
    for (T t : coll) {
      if (--num < 0) {
        return t;
      }
    }
    throw new AssertionError();
  }
}
