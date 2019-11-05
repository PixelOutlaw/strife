package land.face.strife.util;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import java.util.Set;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.conditions.Condition;
import land.face.strife.data.conditions.Condition.CompareTarget;
import land.face.strife.data.conditions.Condition.Comparison;
import land.face.strife.data.conditions.Condition.ConditionUser;
import land.face.strife.util.DamageUtil.DamageType;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class PlayerDataUtil {

  public static void sendActionbarDamage(LivingEntity entity, double damage, double overBonus,
      double critBonus, Set<DamageType> triggeredElements, boolean isBleedApplied,
      boolean isSneakAttack) {
    if (!(entity instanceof Player)) {
      return;
    }
    StringBuilder damageString = new StringBuilder("&f&l" + (int) Math.ceil(damage) + " Damage! ");
    if (overBonus > 0) {
      damageString.append("&e✦");
    }
    if (isSneakAttack) {
      damageString.append("&7&l!");
    }
    if (critBonus > 0) {
      damageString.append("&c✸");
    }
    if (triggeredElements.contains(DamageType.FIRE)) {
      damageString.append("&6✷");
    }
    if (triggeredElements.contains(DamageType.ICE)) {
      damageString.append("&b❊");
    }
    if (triggeredElements.contains(DamageType.LIGHTNING)) {
      damageString.append("&e&l⚡");
    }
    if (triggeredElements.contains(DamageType.EARTH)) {
      damageString.append("&2⚍");
    }
    if (triggeredElements.contains(DamageType.LIGHT)) {
      damageString.append("&f❂");
    }
    if (triggeredElements.contains(DamageType.DARK)) {
      damageString.append("&8❂");
    }
    if (isBleedApplied) {
      damageString.append("&4✘");
    }
    //❖✜
    MessageUtils.sendActionBar((Player) entity, damageString.toString());
  }

  public static boolean areConditionsMet(StrifeMob caster, StrifeMob target,
      Set<Condition> conditions) {
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
        return false;
      }
      if (!condition.isMet(caster, target)) {
        LogUtil.printDebug("-- Skipping, condition " + condition + " not met!");
        return false;
      }
    }
    return true;
  }

  public static void updatePlayerEquipment(Player player) {
    StrifePlugin.getInstance().getChampionManager().updateEquipmentStats(
        StrifePlugin.getInstance().getChampionManager().getChampion(player));
  }

  public static void playExpSound(Player player) {
    player.playSound(player.getEyeLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.2f,
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
      case NONE:
        throw new IllegalArgumentException("Compare condition is NONE! Invalid usage!");
    }
    return false;
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
    return StrifePlugin.getInstance().getChampionManager().getChampion(player)
        .getEffectiveLifeSkillLevel(type, updateEquipment);
  }

  public static int getLifeSkillLevel(Player player, LifeSkillType type) {
    return StrifePlugin.getInstance().getChampionManager().getChampion(player)
        .getLifeSkillLevel(type);
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

  public static float getLifeSkillMaxExp(Player player, LifeSkillType type) {
    int level = StrifePlugin.getInstance().getChampionManager().getChampion(player)
        .getLifeSkillLevel(type);
    return StrifePlugin.getInstance().getSkillExperienceManager().getMaxExp(type, level);
  }

  public static float getSkillProgress(Champion champion, LifeSkillType type) {
    return champion.getSaveData().getSkillExp(type) / StrifePlugin.getInstance()
        .getSkillExperienceManager().getMaxExp(type, champion.getSaveData().getSkillLevel(type));
  }
}
