package land.face.strife.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.conditions.Condition;
import land.face.strife.data.conditions.Condition.CompareTarget;
import land.face.strife.data.conditions.Condition.Comparison;
import land.face.strife.data.conditions.Condition.ConditionUser;
import land.face.strife.listeners.SwingListener;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MiscDisguise;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.RabbitType;
import me.libraryaddict.disguise.disguisetypes.watchers.AgeableWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.DroppedItemWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.FoxWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.MushroomCowWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ParrotWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.RabbitWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.SheepWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.SlimeWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.SnowmanWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ZombieWatcher;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fox;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MushroomCow.Variant;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class PlayerDataUtil {

  private static final Map<UUID, Set<Player>> NEARBY_PLAYER_CACHE = new HashMap<>();

  public static void restoreHealth(LivingEntity le, double amount) {
    DamageUtil.restoreHealth(le, amount);
  }

  public static void restoreEnergy(LivingEntity le, float amount) {
    if (le instanceof Player) {
      StrifePlugin.getInstance().getEnergyManager().changeEnergy((Player) le, amount);
    }
  }

  public static void restoreHealthOverTime(LivingEntity le, float amount, int ticks) {
    if (le instanceof Player) {
      StrifePlugin.getInstance().getRegenTask().addHealing(le.getUniqueId(), amount, ticks);
    }
  }

  public static void restoreEnergyOverTime(LivingEntity le, float amount, int ticks) {
    if (le instanceof Player) {
      StrifePlugin.getInstance().getEnergyRegenTask().addEnergy(le.getUniqueId(), amount, ticks);
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

    PacketContainer swingPacket = new PacketContainer(PacketType.Play.Server.ANIMATION);
    swingPacket.getEntityModifier(entity.getWorld()).write(0, entity);
    swingPacket.getIntegers().write(0, entity.getEntityId());
    swingPacket.getIntegers().write(1, slot == EquipmentSlot.HAND ? 0 : 3);

    ProtocolLibrary.getProtocolManager().broadcastServerPacket(swingPacket, entity, false);

    if (!(entity instanceof Player)) {
      return;
    }

    SwingListener.addFakeSwing(entity.getUniqueId());

    try {
      ProtocolLibrary.getProtocolManager().sendServerPacket((Player) entity, swingPacket, true);
    } catch (InvocationTargetException exception) {
      Bukkit.getLogger().warning("Unable to send self swing packet");
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

  public static Disguise parseDisguise(ConfigurationSection section, String name, boolean dynamic) {
    if (section == null) {
      return null;
    }
    String disguiseType = section.getString("type", null);
    if (StringUtils.isBlank(disguiseType)) {
      return null;
    }
    DisguiseType type;
    try {
      type = DisguiseType.valueOf(disguiseType);
    } catch (Exception e) {
      Bukkit.getLogger().warning("Invalid disguise type " + disguiseType + " for " + name);
      return null;
    }
    if (type == DisguiseType.PLAYER) {
      String disguisePlayer = section.getString("disguise-player");
      if (StringUtils.isBlank(disguisePlayer)) {
        disguisePlayer = "Pur3p0w3r";
      }
      PlayerDisguise playerDisguise = new PlayerDisguise(name, disguisePlayer);
      playerDisguise.setReplaceSounds(true);
      if (dynamic) {
        playerDisguise.setName("<Inherit>");
        playerDisguise.setDynamicName(true);
      } else {
        playerDisguise.setName(name);
        playerDisguise.setDynamicName(false);
      }
      return playerDisguise;
    }
    if (type.isMob()) {
      String typeData = section.getString("disguise-type-data", "");
      boolean babyData = section.getBoolean("baby", false);
      MobDisguise mobDisguise = new MobDisguise(type);
      FlagWatcher watcher = mobDisguise.getWatcher();
      if (babyData && type == DisguiseType.ZOMBIE) {
        ((ZombieWatcher) watcher).setBaby();
      }
      if (watcher instanceof AgeableWatcher) {
        ((AgeableWatcher) watcher).setBaby(babyData);
      }
      if (StringUtils.isNotBlank(typeData)) {
        try {
          switch (type) {
            case MUSHROOM_COW:
              if (typeData.toUpperCase().equals("BROWN")) {
                ((MushroomCowWatcher) watcher).setVariant(Variant.BROWN);
              } else {
                ((MushroomCowWatcher) watcher).setVariant(Variant.RED);
              }
              break;
            case FOX:
              Fox.Type foxType = Fox.Type.valueOf(typeData);
              ((FoxWatcher) watcher).setType(foxType);
              break;
            case PARROT:
              Parrot.Variant parrotType = Parrot.Variant.valueOf(typeData);
              ((ParrotWatcher) watcher).setVariant(parrotType);
              break;
            case SHEEP:
              DyeColor color = DyeColor.valueOf(typeData.toUpperCase());
              ((SheepWatcher) watcher).setColor(color);
              break;
            case RABBIT:
              RabbitType rabbitType = RabbitType.valueOf(typeData);
              ((RabbitWatcher) watcher).setType(rabbitType);
              break;
            case SLIME:
              ((SlimeWatcher) watcher).setSize(Integer.parseInt(typeData));
              break;
            case SNOWMAN:
              ((SnowmanWatcher) watcher).setDerp(Boolean.parseBoolean(typeData));
              break;
          }
        } catch (Exception e) {
          LogUtil.printWarning("Cannot load type " + typeData + " for " + name);
        }
      }
      mobDisguise.setReplaceSounds(true);
      return mobDisguise;
    }
    if (type.isMisc()) {
      MiscDisguise miscDisguise = new MiscDisguise(type);
      miscDisguise.setReplaceSounds(true);
      FlagWatcher watcher = miscDisguise.getWatcher();
      try {
        if (type == DisguiseType.DROPPED_ITEM) {
          Material material = Material.valueOf(section.getString("material", "STONE"));
          ItemStack stack = new ItemStack(material);
          if (material == Material.PLAYER_HEAD) {
            String base64 = section.getString("base64",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTIyODRlMTMyYmZkNjU5YmM2YWRhNDk3YzRmYTMwOTRjZDkzMjMxYTZiNTA1YTEyY2U3Y2Q1MTM1YmE4ZmY5MyJ9fX0=");
            stack = ItemUtil.withBase64(stack, base64);
          }
          ((DroppedItemWatcher) watcher).setItemStack(stack);
        }
      } catch (Exception e) {
        LogUtil.printWarning("Cannot load data for " + name);
      }
      return miscDisguise;
    }
    return null;
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
}
