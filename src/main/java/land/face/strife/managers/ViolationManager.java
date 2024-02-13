package land.face.strife.managers;

import com.tealcube.minecraft.bukkit.facecore.utilities.MoveUtil;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import land.face.strife.StrifePlugin;
import land.face.strife.data.UniqueEntity;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ViolationManager {

  private StrifePlugin plugin;

  private static final Map<Player, Location> lastPlayerLocationOnKill = new WeakHashMap<>();
  private static final Map<Player, Integer> violationLevel = new WeakHashMap<>();
  private static final Map<Player, Float> cachedViolation = new WeakHashMap<>();

  private final List<String> penaltyFreeWorlds;

  public ViolationManager(StrifePlugin plugin) {
    this.plugin = plugin;
    penaltyFreeWorlds = plugin.getSettings().getStringList("config.penalty-free-worlds");
  }

  public float getSafespotViolationMult(Player player) {
    return cachedViolation.getOrDefault(player, 1f);
  }

  public float calculateSafespotViolationMult(Player player) {
    float mult = 1.0f;
    if (lastPlayerLocationOnKill.containsKey(player)) {
      if (player.getLocation().getWorld() != lastPlayerLocationOnKill.get(player).getWorld() ||
          penaltyFreeWorlds.contains(player.getLocation().getWorld().getName())) {
        violationLevel.put(player, 0);
        lastPlayerLocationOnKill.put(player, player.getLocation());
        cachedViolation.put(player, 1.0f);
        return mult;
      }
      int amount = violationLevel.getOrDefault(player, 0);
      if (!MoveUtil.hasMoved(player, 60000)) {
        amount = 200;
      } else if (!MoveUtil.hasMoved(player, 10000)) {
        amount += 10;
      } else {
        double distance = lastPlayerLocationOnKill.get(player).distanceSquared(player.getLocation());
        if (distance < 0.5) {
          amount += 3;
        } else if (distance < 2) {
          amount += 2;
        } else if (distance < 4) {
          amount += 1;
        } else if (distance < 9) {
          amount -= 12;
        } else if (distance < 16) {
          amount -= 70;
        } else {
          amount = 0;
        }
      }
      amount = Math.max(amount, 0);
      amount = Math.min(amount, 200);
      violationLevel.put(player, amount);
      if (amount > 70) {
        mult = (200f - amount) / 200f;
      }
    }
    lastPlayerLocationOnKill.put(player, player.getLocation());
    cachedViolation.put(player, mult);
    return mult;
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
        maximumDifference = 8;
      } else {
        maximumDifference = (int) Math.max(13, (float) lowestPlayerLevel / 4.5f);
      }
      // Bukkit.getLogger().info("[XPDEBUG] max level diff: " + maximumDifference);
    }

    if (levelDiff > maximumDifference) {
      baseXpMult *= (float) Math.pow(0.85f, levelDiff - maximumDifference);
      //Bukkit.getLogger().info("[XPDEBUG] PENALTY - FINAL RESULT: " + baseXpMult);
    }
    if (uniqueEntity == null) {
      return baseXpMult;
    }
    return Math.max(uniqueEntity.getMinLevelClampMult(), baseXpMult);
  }
}
