package info.faceland.strife.util;

import com.tealcube.minecraft.bukkit.TextUtils;
import gyurix.spigotlib.ChatAPI;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.conditions.Condition;
import info.faceland.strife.conditions.Condition.Comparison;
import info.faceland.strife.data.AttributedEntity;
import java.util.List;
import java.util.Set;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class PlayerDataUtil {

  public static void sendActionbarDamage(LivingEntity entity, double damage, double overBonus,
      double critBonus, double fireBonus, double iceBonus, double lightningBonus, double earthBonus,
      double lightBonus, boolean corrupt, boolean isBleedApplied) {
    if (!(entity instanceof Player)) {
      return;
    }
    StringBuilder damageString = new StringBuilder("&f&l" + (int) Math.ceil(damage) + " Damage! ");
    if (overBonus > 0) {
      damageString.append("&e✦");
    }
    if (critBonus > 0) {
      damageString.append("&c✶");
    }
    if (fireBonus > 0) {
      damageString.append("&6☀");
    }
    if (iceBonus > 0) {
      damageString.append("&b❊");
    }
    if (lightningBonus > 0) {
      damageString.append("&7⚡");
    }
    if (earthBonus > 0) {
      damageString.append("&2▼");
    }
    if (lightBonus > 0) {
      damageString.append("&f❂");
    }
    if (corrupt) {
      damageString.append("&8❂");
    }
    if (isBleedApplied) {
      damageString.append("&4♦");
    }
    ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.ACTION_BAR,
        TextUtils.color(damageString.toString()), (Player) entity);
  }

  public static boolean areConditionsMet(AttributedEntity caster, AttributedEntity target,
      Set<Condition> conditions) {
    if (target == null && conditions.size() > 0) {
      return false;
    }
    for (Condition condition : conditions) {
      if (!condition.isMet(caster, target)) {
        return false;
      }
    }
    return true;
  }

  public static int getCraftSkill(Player player, Boolean updateEquipment) {
    return StrifePlugin.getInstance().getChampionManager().getChampion(player)
        .getCraftSkill(updateEquipment);
  }

  public static int getCraftLevel(Player player) {
    return StrifePlugin.getInstance().getChampionManager().getChampion(player)
        .getCraftingLevel();
  }

  public static float getCraftExp(Player player) {
    return StrifePlugin.getInstance().getChampionManager().getChampion(player)
        .getCraftingExp();
  }

  public static float getCraftMaxExp(Player player) {
    int level = getCraftLevel(player);
    return StrifePlugin.getInstance().getCraftExperienceManager().getMaxExp(level);
  }

  public static int getEnchantSkill(Player player, Boolean updateEquipment) {
    return StrifePlugin.getInstance().getChampionManager().getChampion(player)
        .getEnchantSkill(updateEquipment);
  }

  public static int getEnchantLevel(Player player) {
    return StrifePlugin.getInstance().getChampionManager().getChampion(player)
        .getEnchantLevel();
  }

  public static float getEnchantExp(Player player) {
    return StrifePlugin.getInstance().getChampionManager().getChampion(player)
        .getEnchantExp();
  }

  public static float getEnchantMaxExp(Player player) {
    int level = getEnchantLevel(player);
    return StrifePlugin.getInstance().getEnchantExperienceManager().getMaxExp(level);
  }

  public static int getFishSkill(Player player, Boolean updateEquipment) {
    return StrifePlugin.getInstance().getChampionManager().getChampion(player)
        .getFishSkill(updateEquipment);
  }

  public static int getFishLevel(Player player) {
    return StrifePlugin.getInstance().getChampionManager().getChampion(player)
        .getFishingLevel();
  }

  public static float getFishExp(Player player) {
    return StrifePlugin.getInstance().getChampionManager().getChampion(player)
        .getFishingExp();
  }

  public static float getFishMaxExp(Player player) {
    int level = getFishLevel(player);
    return StrifePlugin.getInstance().getFishExperienceManager().getMaxExp(level);
  }

  public static int getMineSkill(Player player, Boolean updateEquipment) {
    return StrifePlugin.getInstance().getChampionManager().getChampion(player)
        .getMineSkill(updateEquipment);
  }

  public static int getMiningLevel(Player player) {
    return StrifePlugin.getInstance().getChampionManager().getChampion(player)
        .getMiningLevel();
  }

  public static float getMiningExp(Player player) {
    return StrifePlugin.getInstance().getChampionManager().getChampion(player)
        .getMiningExp();
  }

  public static float getMiningMaxExp(Player player) {
    int level = getMiningLevel(player);
    return StrifePlugin.getInstance().getMiningExperienceManager().getMaxExp(level);
  }

  public static void updatePlayerEquipment(Player player) {
    StrifePlugin.getInstance().getChampionManager().updateEquipmentAttributes(
        StrifePlugin.getInstance().getChampionManager().getChampion(player));
  }

  public static void playExpSound(Player player) {
    player.playSound(player.getEyeLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.4f,
        0.8f + (float) Math.random() * 0.4f);
  }

  // TODO: Something less stupid, this shouldn't be in this Util
  public static boolean conditionCompare(Comparison comparison, double val1, double val2) {
    switch (comparison) {
      case GREATER_THAN:
        return val1 > val2;
      case LESS_THAN:
        return val1 < val2;
      case EQUAL:
        return val1 == val2;
    }
    return false;
  }

  // TODO: Something better with the crap below here...
  public static int getMaxItemDestroyLevel(Player player) {
    return getMaxItemDestroyLevel(
        StrifePlugin.getInstance().getChampionManager().getChampion(player).getCraftingLevel());
  }

  private static int getMaxItemDestroyLevel(int craftLvl) {
    return 10 + (int) Math.floor((double) craftLvl / 3) * 5;
  }

  public static int getMaxCraftItemLevel(Player player) {
    return getMaxCraftItemLevel(
        StrifePlugin.getInstance().getChampionManager().getChampion(player).getCraftingLevel());
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
}
