package land.face.strife.managers;

import com.tealcube.minecraft.bukkit.facecore.utilities.MoveUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.UniqueEntity;
import land.face.strife.data.pojo.ViolationData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ViolationManager {

  private final StrifePlugin plugin;
  private static final Map<UUID, ViolationData> VIOLATIONS = new HashMap<>();
  private final List<String> penaltyFreeWorlds;

  public ViolationManager(StrifePlugin plugin) {
    this.plugin = plugin;
    penaltyFreeWorlds = plugin.getSettings().getStringList("config.penalty-free-worlds");
    Bukkit.getScheduler().runTaskTimer(plugin, () -> {
      for (var vData : VIOLATIONS.values()) {
        if (vData.getLevel() > 0) {
          vData.setLevel(vData.getLevel() - 1);
        }
      }
    }, 99L, 5 * 20L);
  }

  public float fetchViolationMult(Player player) {
    if (VIOLATIONS.containsKey(player.getUniqueId())) {
      return VIOLATIONS.get(player.getUniqueId()).getMultiplier();
    }
    VIOLATIONS.put(player.getUniqueId(), new ViolationData(player));
    return 1f;
  }

  public float updateViolationLevel(Player player) {
    var vData = VIOLATIONS.getOrDefault(player.getUniqueId(), new ViolationData(player));
    if (vData.getViolationSleepTime() > System.currentTimeMillis()) {
      VIOLATIONS.put(player.getUniqueId(), vData);
      return vData.getMultiplier();
    }
    float mult = 1.0f;
    int newLevel = vData.getLevel();
    newLevel += getViolationChange(player, vData);
    newLevel = Math.clamp(newLevel, 0, 195);
    vData.setLevel(newLevel);
    if (newLevel >= 100) {
      mult = (200f - newLevel) / 100f;
    }
    vData.setMultiplier(mult);
    vData.setLastLocation(player.getLocation());
    vData.setViolationSleepTime(System.currentTimeMillis() + 200L);
    return mult;
  }

  private int getViolationChange(Player player, ViolationData vData) {
    if (!vData.getLastLocation().getWorld().getName().equals(player.getWorld().getName())) {
      return -200;
    }
    if (penaltyFreeWorlds.contains(player.getLocation().getWorld().getName())) {
      return -200;
    }
    if (!MoveUtil.hasMoved(player, 60000)) {
      return 40;
    }
    if (!MoveUtil.hasMoved(player, 10000)) {
      return 10;
    }
    double distance = vData.getLastLocation().distanceSquared(player.getLocation());
    if (distance < 2) {
      return 10;
    }
    if (distance < 4) {
      return 6;
    }
    if (distance < 9) {
      return 4;
    }
    if (distance < 16) {
      return 2;
    }
    if (distance < 25) {
      return 0;
    }
    if (distance < 36) {
      return -16;
    }
    if (distance < 49) {
      return -32;
    }
    if (distance < 100) {
      return -50;
    }
    return -200;
  }

  public float calculateLevelPenalty(float baseXpMult, int mobLevel, Set<Player> partyMembers, UniqueEntity uniqueEntity) {
    int levelDiff;
    int maximumDifference;
    if (partyMembers.size() == 1) {
      // Bukkit.getLogger().info("[XPDEBUG] No party");
      // Bukkit.getLogger().info("[XPDEBUG] mob level: " + mobLevel);
      Player player = partyMembers.stream().findFirst().get();
      levelDiff = Math.abs(player.getLevel() - mobLevel);
      // Bukkit.getLogger().info("[XPDEBUG] level diff: " + levelDiff);
      if (mobLevel < player.getLevel()) {
        maximumDifference = 8;
      } else {
        maximumDifference = (int) Math.max(13, ((float) player.getLevel()) / 4.5f);
      }
      // Bukkit.getLogger().info("[XPDEBUG] max level diff: " + maximumDifference);
    } else {
      // Bukkit.getLogger().info("[XPDEBUG] Yes party");
      // Bukkit.getLogger().info("[XPDEBUG] mob level: " + mobLevel);
      int highestPlayerLevel = 0;
      int lowestPlayerLevel = 1000;
      for (Player player : partyMembers) {
        if (player.getLevel() > highestPlayerLevel) {
          highestPlayerLevel = player.getLevel();
        }
        if (player.getLevel() < lowestPlayerLevel) {
          lowestPlayerLevel = player.getLevel();
        }
      }
      // Bukkit.getLogger().info("[XPDEBUG] Highest level: " + highestPlayerLevel);
      // Bukkit.getLogger().info("[XPDEBUG] Lowest level: " + highestPlayerLevel);
      int highestDiff = Math.abs(mobLevel - highestPlayerLevel);
      int lowestDiff = Math.abs(mobLevel - lowestPlayerLevel);
      levelDiff = Math.max(lowestDiff, highestDiff);
      // Bukkit.getLogger().info("[XPDEBUG] level diff: " + levelDiff);
      if (mobLevel < highestPlayerLevel) {
        maximumDifference = 10;
      } else {
        maximumDifference = (int) Math.max(13, (float) lowestPlayerLevel / 4.5f);
      }
      // Bukkit.getLogger().info("[XPDEBUG] max level diff: " + maximumDifference);
    }

    if (levelDiff > maximumDifference) {
      baseXpMult *= (float) Math.pow(0.90f, levelDiff - maximumDifference);
      //Bukkit.getLogger().info("[XPDEBUG] PENALTY - FINAL RESULT: " + baseXpMult);
    }
    if (uniqueEntity == null) {
      return baseXpMult;
    }
    return Math.max(uniqueEntity.getMinLevelClampMult(), baseXpMult);
  }
}
