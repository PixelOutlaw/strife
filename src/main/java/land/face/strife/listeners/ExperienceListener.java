/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package land.face.strife.listeners;

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.MoveUtil;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import land.face.learnin.LearninBooksPlugin;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.UniqueEntity;
import land.face.strife.data.champion.Champion;
import land.face.strife.events.StrifeCombatXpEvent;
import land.face.strife.events.UniqueKillEvent;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.ItemUtil;
import land.face.strife.util.SpecialStatusUtil;
import land.face.strife.util.StatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class ExperienceListener implements Listener {

  private final StrifePlugin plugin;

  private static final String LEVEL_UP = "&a&l( &f&lDANG &a&l/ &f&lSON! &a&l)";
  private static final String LEVEL_DOWN = "&c&l( &f&lDANG &c&l/ &f&lSON! &c&l)";

  private static final Map<Integer, String> xpString = new HashMap<>();
  private static final Map<Player, Location> lastPlayerLocationOnKill = new WeakHashMap<>();
  private static final Map<Player, Integer> violationLevel = new WeakHashMap<>();

  private final List<String> penaltyFreeWorlds;

  public ExperienceListener(StrifePlugin plugin) {
    this.plugin = plugin;
    penaltyFreeWorlds = plugin.getSettings().getStringList("config.penalty-free-worlds");
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onXpShare(EntityDeathEvent event) {
    if (event.getDroppedExp() == 0 || SpecialStatusUtil.isSpawnerMob(event.getEntity())) {
      return;
    }
    if (!plugin.getStrifeMobManager().isTrackedEntity(event.getEntity())) {
      return;
    }
    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(event.getEntity());
    Set<Player> killers = StrifeMob.getKillers(mob);
    if (killers.isEmpty()) {
      event.setDroppedExp(0);
      return;
    }

    UniqueKillEvent ev = new UniqueKillEvent(mob, killers);
    Bukkit.getPluginManager().callEvent(ev);

    UniqueEntity ue = plugin.getUniqueEntityManager().getUnique(mob.getUniqueEntityId());
    if (ue != null) {
      if (LearninBooksPlugin.instance.getKnowledgeManager().getLoadedKnowledge().containsKey(mob.getUniqueEntityId())) {
        for (Player p : ev.getKillers()) {
          LearninBooksPlugin.instance.getKnowledgeManager().incrementKnowledge(p, mob.getUniqueEntityId(), 1);
        }
      }
      for (String bonusKnowledge : ue.getBonusKnowledge()) {
        for (Player p : ev.getKillers()) {
          LearninBooksPlugin.instance.getKnowledgeManager().incrementKnowledge(p, bonusKnowledge, 1);
        }
      }
    }

    for (String key : mob.getMods()) {
      key = "mod-" + key;
      if (LearninBooksPlugin.instance.getKnowledgeManager()
          .getLoadedKnowledge().containsKey(key)) {
        for (Player p : ev.getKillers()) {
          LearninBooksPlugin.instance.getKnowledgeManager()
              .incrementKnowledge(p, key, 1);
        }
      }
    }

    float droppedXp = event.getDroppedExp();
    event.setDroppedExp(0);

    float expMultiplier = 1f / killers.size();
    expMultiplier += (killers.size() - 1) * 0.2f;

    expMultiplier = calculateLevelPenalty(expMultiplier, StatUtil.getMobLevel(event.getEntity()), killers, ue);

    for (Player player : killers) {
      float xpPenalty = calculateSafespotViolationMult(player);
      //Bukkit.getLogger().info("[xxxxx] safespotpenalty " + xpPenalty);
      float finalXp = (droppedXp * expMultiplier * xpPenalty);
      if (finalXp < 1) {
        continue;
      }
      StrifeCombatXpEvent xpEvent = new StrifeCombatXpEvent(player, event.getEntity(), finalXp);
      Bukkit.getPluginManager().callEvent(xpEvent);
      if (xpEvent.isCancelled()) {
        // Only do skillXP
        StrifeMob playerMob = plugin.getStrifeMobManager().getStatMob(player);
        playerMob.getChampion().getDetailsContainer().addExp(finalXp);
      } else {
        plugin.getExperienceManager().addExperience(player, finalXp, false);
      }
    }
  }

  private float calculateLevelPenalty(float baseXpMult, int mobLevel, Set<Player> partyMembers,
      UniqueEntity uniqueEntity) {
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
      baseXpMult *= Math.pow(0.85, levelDiff - maximumDifference);
      //Bukkit.getLogger().info("[XPDEBUG] PENALTY - FINAL RESULT: " + baseXpMult);
    }
    if (uniqueEntity == null) {
      return baseXpMult;
    }
    return Math.max(uniqueEntity.getMinLevelClampMult(), baseXpMult);
  }

  private float calculateSafespotViolationMult(Player player) {
    float mult = 1.0f;
    if (lastPlayerLocationOnKill.containsKey(player)) {
      if (player.getLocation().getWorld() != lastPlayerLocationOnKill.get(player).getWorld()) {
        violationLevel.put(player, 0);
        lastPlayerLocationOnKill.put(player, player.getLocation());
        return mult;
      }
      int amount = violationLevel.getOrDefault(player, 0);
      if (!MoveUtil.hasMoved(player, 60000)) {
        amount = 200;
      } else if (!MoveUtil.hasMoved(player, 10000)) {
        amount += 5;
      } else {
        double distance = lastPlayerLocationOnKill.get(player).distanceSquared(player.getLocation());
        if (distance < 0.5) {
          amount += 4;
        } else if (distance < 1.5) {
          amount += 2;
        } else if (distance < 3) {
          amount += 1;
        } else if (distance > 2500) {
          amount = 0;
        } else if (distance > 25) {
          amount -= 45;
        } else {
          amount -= 8;
        }
      }
      amount = Math.min(200, Math.max(amount, 0));
      violationLevel.put(player, amount);
      if (amount > 50) {
        mult = Math.max(0.2f, (200f - amount) / 200f);
      }
    }
    lastPlayerLocationOnKill.put(player, player.getLocation());
    return mult;
  }

  // TODO: xp popoff
  public static String convertToXpString(int i) {
    if (xpString.containsKey(i)) {
      return xpString.get(i);
    }
    String s = Integer.toString(i);
    s = s
        .replaceAll("0", "⑳")
        .replaceAll("1", "⑪")
        .replaceAll("2", "⑫")
        .replaceAll("3", "⑬")
        .replaceAll("4", "⑭")
        .replaceAll("5", "⑮")
        .replaceAll("6", "⑯")
        .replaceAll("7", "⑰")
        .replaceAll("8", "⑱")
        .replaceAll("9", "⑲");
    xpString.put(i, s + "学");
    return s;
  }


  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerDeath(PlayerDeathEvent event) {
    event.setKeepLevel(true);
    event.setDroppedExp(0);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerRespawn(PlayerRespawnEvent event) {
    Player p = event.getPlayer();
    if (plugin.getDamageManager().getSourceOfDeath().containsKey(p.getUniqueId())) {
      PaletteUtil.sendMessage(p, "|none|\uD83D\uDC80 |gray|You have been slain by " +
          plugin.getDamageManager().getSourceOfDeath().get(p.getUniqueId()) + "|gray|!");
      plugin.getDamageManager().getSourceOfDeath().remove(p.getUniqueId());
    }
    if (penaltyFreeWorlds.contains(p.getWorld().getName())) {
      return;
    }
    StrifeMob playerMob = plugin.getStrifeMobManager().getStatMob(p);
    if (plugin.getStrifeMobManager().getStatMob(p).diedFromPvp()) {
      return;
    }
    if (p.getLevel() >= 100) {
      return;
    }
    if (p.getLevel() < 10) {
      sendMessage(p, "&cYou lost &f0 XP &cfrom dying &a(No loss below level 10!)");
      return;
    }
    PlayerInventory inv = p.getInventory();
    double lostXP = 0;
    float lossReduction = playerMob.getStat(StrifeStat.XP_LOST_ON_DEATH);
    if (lossReduction >= 99.5) {
      sendMessage(p, "&a&oSomehow, your stats reduce XP loss so low that you didn't lose any! Good job!");
    } else if (hadSoulShard(inv)) {
      sendMessage(p, "&a&oYou consumed a &f&oSoul Shard&a&o! You lost &f&o0 XP&a&o!");
    } else {
      double currentLevelMaxXP = plugin.getExperienceManager().getMaxFaceExp(p.getLevel());
      float xpLossPercent = 0.03f;
      xpLossPercent *= 1 - lossReduction / 100;
      lostXP = Math.min(currentLevelMaxXP * xpLossPercent, p.getExp() * currentLevelMaxXP);
      sendMessage(p, "&cAlas! You lost &f" + StrifePlugin.INT_FORMAT.format(lostXP) + " XP &cfrom dying!");
      p.setExp(Math.max(p.getExp() - xpLossPercent, 0.00001f));
    }
    if (lostXP > 0) {
      playerMob.getChampion().getSaveData().setCatchupExpUsed(
          playerMob.getChampion().getSaveData().getCatchupExpUsed() - lostXP * 0.5);
    }
    plugin.getGuiManager().updateLevelDisplay(playerMob);
    plugin.getSoulManager().setLostExp(p, lostXP);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerLevelChange(PlayerLevelChangeEvent event) {
    Player player = event.getPlayer();
    Champion champion = plugin.getChampionManager().getChampion(player);
    plugin.getChampionManager().buildBaseStats(champion);
    if (event.getNewLevel() <= champion.getHighestReachedLevel()) {
      return;
    }
    int points = event.getNewLevel() - event.getOldLevel();
    champion.setHighestReachedLevel(event.getNewLevel());
    champion.setUnusedStatPoints(champion.getUnusedStatPoints() + points);
    champion.recombineCache(plugin);

    player.setHealth(player.getMaxHealth());
    StatUtil.changeEnergy(plugin.getStrifeMobManager().getStatMob(player), 200000);
    plugin.getStatUpdateManager().updateAllAttributes(event.getPlayer());
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerExpChange(PlayerExpChangeEvent event) {
    plugin.getExperienceManager().addExperience(event.getPlayer(), event.getAmount(), false);
    event.setAmount(0);
  }

  private boolean hadSoulShard(PlayerInventory inv) {
    for (int i = 0; i < inv.getContents().length; i++) {
      ItemStack is = inv.getItem(i);
      if (is == null) {
        continue;
      }
      if (isSoulShard(is)) {
        if (is.getAmount() > 1) {
          is.setAmount(is.getAmount() - 1);
          inv.setItem(i, is);
        } else {
          inv.setItem(i, null);
        }
        return true;
      }
    }
    return false;
  }

  private boolean isSoulShard(ItemStack itemStack) {
    return itemStack.getType() == Material.QUARTZ && ItemUtil.getCustomData(itemStack) == 100;
  }
}
