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
package land.face.strife.managers;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import com.tealcube.minecraft.bukkit.shade.google.gson.Gson;
import com.tealcube.minecraft.bukkit.shade.google.gson.JsonArray;
import com.tealcube.minecraft.bukkit.shade.google.gson.JsonElement;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Role;
import github.scarsz.discordsrv.util.DiscordUtil;
import io.pixeloutlaw.minecraft.spigot.garbage.ListExtensionsKt;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import land.face.strife.StrifePlugin;
import land.face.strife.data.Boost;
import land.face.strife.data.LoadedStatBoost;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.LogUtil;
import land.face.strife.util.StatUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class BoostManager {

  private StrifePlugin plugin;

  private final Map<DayOfWeek, String> boostSchedule = new HashMap<>();
  private final Map<String, LoadedStatBoost> loadedBoosts = new HashMap<>();
  private final List<Boost> boosts = new CopyOnWriteArrayList<>();
  private LoadedStatBoost dailyBoost = null;

  private Gson gson = new Gson();

  private Set<UUID> discordBoosters = new HashSet<>();
  private Set<UUID> contributors = new HashSet<>();

  public BoostManager(StrifePlugin plugin) {
    this.plugin = plugin;
    for (Player p : Bukkit.getOnlinePlayers()) {
      updateGlobalBoostStatus(p);
    }
  }

  public int getContributorSize() {
    return contributors.size();
  }

  public int getDiscordBoostSize() {
    return discordBoosters.size();
  }

  public void removeBooster(UUID uuid) {
    discordBoosters.remove(uuid);
    contributors.remove(uuid);
  }

  public Set<String> getLoadedBoostIds() {
    return loadedBoosts.keySet();
  }

  public void updateGlobalBoostStatus(Player player) {
    if (player.hasPermission("has.donated")) {
      contributors.add(player.getUniqueId());
    }
    try {
      String id = DiscordSRV.getPlugin().getAccountLinkManager().getDiscordId(player.getUniqueId());
      if (StringUtils.isBlank(id)) {
        return;
      }
      Member member = DiscordUtil.getMemberById(id);
      if (member == null) {
        return;
      }
      for (Role role : member.getRoles()) {
        if (role.getName().equals("Boosty Bois")) {
          discordBoosters.add(player.getUniqueId());
          return;
        }
      }
    } catch (Exception e) {
      Bukkit.getLogger().warning("Error setting up boost data! " + e.getMessage());
    }
  }

  public Map<StrifeStat, Float> getStats() {
    Map<StrifeStat, Float> statMap = new HashMap<>();
    for (Boost boost : boosts) {
      statMap.putAll(StatUpdateManager.combineMaps(statMap, boost.getStats()));
    }
    if (dailyBoost != null) {
      statMap.putAll(StatUpdateManager.combineMaps(statMap, dailyBoost.getStats()));
    }
    if (discordBoosters.size() > 0) {
      statMap.put(StrifeStat.SKILL_XP_GAIN, discordBoosters.size() *
          2 + statMap.getOrDefault(StrifeStat.SKILL_XP_GAIN, 0f));
    }
    if (contributors.size() > 0) {
      statMap.put(StrifeStat.XP_GAIN, contributors.size() + statMap.getOrDefault(StrifeStat.XP_GAIN, 0f));
    }
    return statMap;
  }

  public boolean startBoost(String name, String boostId, int seconds) {
    if (!loadedBoosts.containsKey(boostId)) {
      LogUtil.printWarning("Invalid boostID Failed to start boost " + boostId + " for " + " name");
      return false;
    }
    LoadedStatBoost loadedStatBoost = loadedBoosts.get(boostId);
    Boost boost = new Boost();
    boost.setBoostId(boostId);
    boost.setBoosterName(name);
    boost.setSecondsRemaining(seconds);
    boost.setStats(new HashMap<>(loadedBoosts.get(boostId).getStats()));
    boosts.add(boost);
    announceBoost(loadedStatBoost.getAnnounceStart(), name, seconds);
    return true;
  }

  public void tickBoosts() {
    for (Boost boost : boosts) {
      LoadedStatBoost loadedStatBoost = loadedBoosts.get(boost.getBoostId());
      if (boost.getSecondsRemaining() <= 0) {
        announceBoost(loadedStatBoost.getAnnounceEnd(), boost.getBoosterName(), 0);
        boosts.remove(boost);
        for (Player p : Bukkit.getOnlinePlayers()) {
          plugin.getChampionManager().getChampion(p).recombineCache();
        }
        continue;
      }
      boost.setSecondsRemaining(boost.getSecondsRemaining() - 1);
      if (boost.getSecondsRemaining() % loadedStatBoost.getAnnounceInterval() == 0) {
        announceBoost(loadedStatBoost.getAnnounceRun(), boost.getBoosterName(),
            boost.getSecondsRemaining());
      }
    }
  }

  public void loadStatBoosts(ConfigurationSection cs) {
    for (String boostId : cs.getKeys(false)) {
      ConfigurationSection boost = cs.getConfigurationSection(boostId);

      String creator = boost.getString("creator", "SERVER");
      int announceInterval = boost.getInt("announcement-interval", 10);
      int duration = boost.getInt("duration", 60);
      List<String> announceStart = ListExtensionsKt.chatColorize(
          boost.getStringList("announcement-start"));
      List<String> announceRun = ListExtensionsKt.chatColorize(
          boost.getStringList("announcement-running"));
      List<String> announceEnd = ListExtensionsKt.chatColorize(
          boost.getStringList("announcement-end"));

      ConfigurationSection attrSection = boost.getConfigurationSection("stats");
      Map<StrifeStat, Float> attrMap = StatUtil.getStatMapFromSection(attrSection);

      LoadedStatBoost loadedStatBoost = new LoadedStatBoost(creator, announceInterval, duration);
      loadedStatBoost.getAnnounceStart().addAll(announceStart);
      loadedStatBoost.getAnnounceRun().addAll(announceRun);
      loadedStatBoost.getAnnounceEnd().addAll(announceEnd);
      loadedStatBoost.getStats().putAll(attrMap);

      loadedBoosts.put(boostId, loadedStatBoost);
    }
  }

  public void loadScheduledBoosts(ConfigurationSection cs) {
    for (String dayString : cs.getKeys(false)) {
      DayOfWeek dayOfWeek;
      try {
        dayOfWeek = DayOfWeek.valueOf(dayString);
      } catch (Exception e) {
        LogUtil.printWarning("Invalid day of week '" + dayString + "'. Skipping...");
        continue;
      }
      String boostId = cs.getString(dayString);
      boostSchedule.put(dayOfWeek, boostId);
    }
  }

  public void checkBoostSchedule() {
    LocalDate date = LocalDate.now();
    DayOfWeek dow = date.getDayOfWeek();
    String boostId = boostSchedule.get(dow);
    if (boostId == null) {
      dailyBoost = null;
      return;
    }
    LoadedStatBoost loadedStatBoost = loadedBoosts.get(boostSchedule.get(dow));
    if (loadedStatBoost == null) {
      dailyBoost = null;
      LogUtil.printWarning("OI! Invalid event for today?? What is... " + boostId + "??");
      return;
    }
    dailyBoost = loadedStatBoost;
    announceBoost(loadedStatBoost.getAnnounceRun(), "SERVER", 0);
  }

  private void announceBoost(List<String> announce, String creator, int secondsRemaining) {
    int minutes = secondsRemaining / 60;
    for (String line : announce) {
      Bukkit.broadcastMessage(line
          .replace("{name}", creator)
          .replace("{seconds}", String.valueOf(secondsRemaining))
          .replace("{minutes}", String.valueOf(minutes)));
    }
  }

  public void saveBoosts() {
    try (FileWriter writer = new FileWriter(plugin.getDataFolder() + "/boosts.json")) {
      gson.toJson(boosts.toArray(), writer);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void loadBoosts() {
    try (FileReader reader = new FileReader(plugin.getDataFolder() + "/boosts.json")) {
      JsonArray array = gson.fromJson(reader, JsonArray.class);
      for (JsonElement e : array) {
        Boost boost = gson.fromJson(e, Boost.class);
        if (boostSchedule.containsValue(boost.getBoostId())) {
          continue;
        }
        boosts.add(boost);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
