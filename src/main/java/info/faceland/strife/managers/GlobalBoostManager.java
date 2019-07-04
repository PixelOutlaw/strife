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
package info.faceland.strife.managers;

import com.tealcube.minecraft.bukkit.TextUtils;
import info.faceland.strife.data.GlobalStatBoost;
import info.faceland.strife.data.LoadedStatBoost;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.util.LogUtil;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

public class GlobalBoostManager {

  private final Map<DayOfWeek, String> scheduledBoosts = new HashMap<>();
  private final Map<String, LoadedStatBoost> loadedBoosts = new HashMap<>();
  private final List<GlobalStatBoost> runningBoosts = new ArrayList<>();

  public double getAttribute(StrifeStat attribute) {
    double amount = 0;
    for (GlobalStatBoost boost : runningBoosts) {
      amount += boost.getAttribute(attribute);
    }
    return amount;
  }

  public Map<StrifeStat, Double> getAttributes() {
    Map<StrifeStat, Double> attrMap = new HashMap<>();
    for (GlobalStatBoost boost : runningBoosts) {
      attrMap.putAll(AttributeUpdateManager.combineMaps(attrMap, boost.getAttributes()));
    }
    return attrMap;
  }

  public boolean createStatBoost(String boostId, String creator, int duration) {
    LoadedStatBoost loadedStatBoost = loadedBoosts.get(boostId);
    if (loadedStatBoost == null) {
      return false;
    }
    for (GlobalStatBoost boost : runningBoosts) {
      if (boost.getBoostId().equals(boostId)) {
        return false;
      }
    }
    GlobalStatBoost boost = new GlobalStatBoost(boostId, creator, loadedStatBoost.getStats(),
        duration);
    runningBoosts.add(boost);
    announceBoost(loadedStatBoost.getAnnounceStart(), creator, duration);
    return true;
  }

  public void tickBoosts() {
    for (GlobalStatBoost boost : runningBoosts) {
      LoadedStatBoost loadedStatBoost = loadedBoosts.get(boost.getBoostId());
      int minutesRemaining = boost.getMinutesRemaining();
      if (minutesRemaining == 0) {
        announceBoost(loadedStatBoost.getAnnounceEnd(), boost.getCreator(), 0);
        runningBoosts.remove(boost);
        continue;
      }
      if (minutesRemaining % loadedStatBoost.getAnnounceInterval() == 0) {
        announceBoost(loadedStatBoost.getAnnounceRun(), boost.getCreator(), minutesRemaining);
      }
      boost.setMinutesRemaining(minutesRemaining - 1);
    }
  }

  public void loadStatBoosts(ConfigurationSection cs) {
    for (String boostId : cs.getKeys(false)) {
      ConfigurationSection boost = cs.getConfigurationSection(boostId);

      String creator = boost.getString("creator", "SERVER");
      int announceInterval = boost.getInt("announcement-interval", 10);
      int duration = boost.getInt("duration", 60);
      List<String> announceStart = TextUtils.color(boost.getStringList("announcement-start"));
      List<String> announceRun = TextUtils.color(boost.getStringList("announcement-running"));
      List<String> announceEnd = TextUtils.color(boost.getStringList("announcement-end"));

      ConfigurationSection attrSection = boost.getConfigurationSection("stats");
      Map<StrifeStat, Double> attrMap = new HashMap<>();
      for (String attr : attrSection.getKeys(false)) {
        StrifeStat attribute;
        try {
          attribute = StrifeStat.valueOf(attr);
        } catch (Exception e) {
          LogUtil.printWarning("Invalid attribute " + attr + ". Skipping...");
          continue;
        }
        attrMap.put(attribute, attrSection.getDouble(attr));
      }

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
      scheduledBoosts.put(dayOfWeek, boostId);
    }
  }

  public void startScheduledEvents() {
    LocalDate date = LocalDate.now();
    DayOfWeek dow = date.getDayOfWeek();
    String boostId = scheduledBoosts.get(dow);
    if (boostId == null) {
      return;
    }
    LoadedStatBoost loadedStatBoost = loadedBoosts.get(scheduledBoosts.get(dow));
    if (loadedStatBoost == null) {
      LogUtil.printWarning("OI! Invalid event for today?? What is... " + boostId + "??");
      return;
    }
    createStatBoost(boostId, loadedStatBoost.getCreator(), loadedStatBoost.getDuration());
  }

  private void announceBoost(List<String> announce, String creator, int minutesRemaining) {
    for (String line : announce) {
      Bukkit.broadcastMessage(line
          .replace("{name}", creator)
          .replace("{duration}", String.valueOf(minutesRemaining)));
    }
  }
}
