package land.face.strife.util;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import land.face.strife.StrifePlugin;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.disguisetypes.MiscDisguise;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.RabbitType;
import me.libraryaddict.disguise.disguisetypes.watchers.AgeableWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.DroppedItemWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.FoxWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.MushroomCowWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.PandaWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ParrotWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.RabbitWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.SheepWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.SlimeWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.SnowmanWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.WolfWatcher;
import me.libraryaddict.disguise.disguisetypes.watchers.ZombieWatcher;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Fox;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MushroomCow.Variant;
import org.bukkit.entity.Panda.Gene;
import org.bukkit.entity.Parrot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

public class DisguiseUtil {

  private static final Map<LivingEntity, Long> tempDisguiseMap = new ConcurrentHashMap<>();
  private static BukkitTask task;

  public static void refresh() {
    if (task != null) {
      task.cancel();
    }
    for (LivingEntity le : tempDisguiseMap.keySet()) {
      if (le != null && le.isValid() && DisguiseAPI.isDisguised(le)) {
        DisguiseAPI.undisguiseToAll(le);
      }
    }
    task = Bukkit.getScheduler().runTaskTimer(StrifePlugin.getInstance(),
        DisguiseUtil::checkTempDisguises, 5L, 10L);
  }

  private static void checkTempDisguises() {
    for (Entry<LivingEntity, Long> entry : tempDisguiseMap.entrySet()) {
      if (!entry.getKey().isValid() || entry.getValue() > System.currentTimeMillis()) {
        tempDisguiseMap.remove(entry.getKey());
        DisguiseAPI.undisguiseToAll(entry.getKey());
      }
    }
  }

  public static void applyTempDisguise(LivingEntity le, Disguise disguise, int duration) {
    if (!tempDisguiseMap.containsKey(le)) {
      DisguiseAPI.disguiseToPlayers(le, disguise, Bukkit.getOnlinePlayers());
    }
    tempDisguiseMap.put(le, System.currentTimeMillis() + duration);
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
    Disguise disguise = null;
    if (type == DisguiseType.PLAYER) {
      String disguisePlayer = section.getString("disguise-player");
      if (StringUtils.isBlank(disguisePlayer)) {
        disguisePlayer = "Faceguy";
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
      disguise = playerDisguise;
    }
    if (type.isMob()) {
      String typeData = section.getString("disguise-type-data", "");
      boolean babyData = section.getBoolean("baby", false);
      MobDisguise mobDisguise = new MobDisguise(type);
      FlagWatcher watcher = mobDisguise.getWatcher();
      if (babyData && (type == DisguiseType.ZOMBIE || type == DisguiseType.HUSK)) {
        ((ZombieWatcher) watcher).setBaby();
      }
      if (watcher instanceof AgeableWatcher) {
        ((AgeableWatcher) watcher).setBaby(babyData);
      }
      if (StringUtils.isNotBlank(typeData)) {
        try {
          switch (type) {
            case MUSHROOM_COW:
              if (typeData.equalsIgnoreCase("BROWN")) {
                ((MushroomCowWatcher) watcher).setVariant(Variant.BROWN);
              } else {
                ((MushroomCowWatcher) watcher).setVariant(Variant.RED);
              }
              break;
            case FOX:
              Fox.Type foxType = Fox.Type.valueOf(typeData);
              ((FoxWatcher) watcher).setType(foxType);
              break;
            case WOLF:
              ((WolfWatcher) watcher).setAngry(Boolean.parseBoolean(typeData));
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
            case PANDA:
              Gene gene = Gene.valueOf(typeData);
              ((PandaWatcher) watcher).setMainGene(gene);
              ((PandaWatcher) watcher).setHiddenGene(gene);
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
      disguise = mobDisguise;
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
      disguise = miscDisguise;
    }
    if (disguise != null) {
      disguise.setSoundGroup(section.getString("sound-group", null));
    }
    return disguise;
  }
}
