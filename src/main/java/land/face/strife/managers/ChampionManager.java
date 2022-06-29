/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package land.face.strife.managers;

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.LoreAbility;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.ChampionSaveData;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.champion.StrifeAttribute;
import land.face.strife.managers.LoreAbilityManager.TriggerType;
import land.face.strife.util.PlayerDataUtil;
import land.face.strife.util.StatUtil;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class ChampionManager {

  private final StrifePlugin plugin;
  private final Map<UUID, Champion> championMap = new HashMap<>();
  private final static String RESET_MESSAGE =
      "&a&lYour Levelpoints have been automatically reset due to an update!";

  public ChampionManager(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  public Champion getChampion(Player player) {

    UUID uuid = player.getUniqueId();

    if (championExists(uuid)) {
      championMap.get(uuid).setPlayer(player);
      return championMap.get(uuid);
    }

    ChampionSaveData saveData = plugin.getStorage().load(player.getUniqueId());
    Champion champion = new Champion(player, saveData);
    championMap.put(uuid, champion);

    buildBaseStats(champion);
    rebuildAttributes(champion);
    plugin.getPathManager().buildPathBonus(champion);

    champion.recombineCache();

    return champion;
  }

  public boolean championExists(UUID uuid) {
    return championMap.containsKey(uuid);
  }

  public Collection<Champion> getChampions() {
    return new HashSet<>(championMap.values());
  }

  public void update(Champion champion) {
    champion.recombineCache();
    plugin.getStatUpdateManager().updateAllAttributes(champion.getPlayer());
  }

  public void update(Player player) {
    update(getChampion(player));
  }

  public void updateAll() {
    for (Player p : Bukkit.getOnlinePlayers()) {
      update(p);
    }
  }

  public void tickPassiveLoreAbilities() {
    for (Player p : Bukkit.getOnlinePlayers()) {
      if (!p.isValid()) {
        continue;
      }
      StrifeMob mob = plugin.getStrifeMobManager().getStatMob(p);
      Set<LoreAbility> abilities = mob.getLoreAbilities(TriggerType.TIMER);
      if (abilities == null || abilities.isEmpty()) {
        continue;
      }
      for (LoreAbility ability : abilities) {
        plugin.getLoreAbilityManager().applyLoreAbility(ability, mob, mob);
      }
    }
  }

  public boolean hasPendingChanges(Player player) {
    return hasPendingChanges(getChampion(player));
  }

  public boolean hasPendingChanges(Champion champion) {
    return !champion.getLevelMap().equals(champion.getPendingLevelMap());
  }

  public void verifyPendingStats(Champion champion) {
    for (StrifeAttribute attr : new HashSet<>(champion.getPendingLevelMap().keySet())) {
      int statCap = plugin.getAttributeManager().getPendingStatCap(attr, champion);
      int diff = champion.getPendingLevelMap().get(attr) - statCap;
      if (diff > 0) {
        champion.getPendingLevelMap().put(attr, statCap);
      }
    }
    int pendingTotal = 0;
    for (StrifeAttribute stat : champion.getPendingLevelMap().keySet()) {
      pendingTotal += champion.getPendingLevelMap().getOrDefault(stat, 0);
    }
    champion.setPendingUnusedStatPoints(champion.getPlayer().getLevel() - pendingTotal);
  }

  public void resetPendingStats(Champion champion) {
    champion.getSaveData().resetPendingStats();
  }

  public void savePendingStats(Champion champion) {
    for (StrifeAttribute stat : champion.getPendingLevelMap().keySet()) {
      if (champion.getPendingLevel(stat) > champion.getAttributeLevel(stat)) {
        sendMessage(champion.getPlayer(), stat.getName() + " increased to " + champion.getPendingLevel(stat) + "!");
        champion.getPlayer().playSound(champion.getPlayer().getLocation(), stat.getLevelSound(), 1f, stat.getLevelPitch());
      }
    }
    champion.getSaveData().savePendingStats();
    champion.buildAttributeHeatmap();
    rebuildAttributes(champion);
  }

  public void verifyStatValues(Champion champion) {
    Player player = champion.getPlayer();
    if (getTotalChampionStats(champion) != player.getLevel()) {
      notifyResetPoints(player);
      for (StrifeAttribute stat : plugin.getAttributeManager().getAttributes()) {
        champion.setLevel(stat, 0);
      }
      champion.setHighestReachedLevel(player.getLevel());
      champion.setUnusedStatPoints(player.getLevel());
    }
  }

  public void addListOfChampions(List<Champion> champions) {
    for (Champion c : champions) {
      championMap.put(c.getUniqueId(), c);
    }
  }

  public void clearAllChampions() {
    championMap.clear();
  }

  public void buildBaseStats(Champion champion) {
    champion.setBaseStats(plugin.getMonsterManager().getBaseStats(
        EntityType.PLAYER, champion.getPlayer().getLevel()));
  }

  public void rebuildAttributes(Champion champion) {
    champion.setLevelPointStats(StatUtil.buildStatsFromAttributes(champion.getLevelMap()));
  }

  public boolean addBoundLoreAbility(StrifeMob mob, LoreAbility loreAbility) {
    Champion champion = mob.getChampion();
    if (champion == null) {
      return false;
    }
    if (champion.getSaveData().getBoundAbilities().contains(loreAbility)) {
      return false;
    }
    champion.getSaveData().getBoundAbilities().add(loreAbility);
    mob.getEquipmentCache().recombineAbilities(mob);
    return true;
  }

  public boolean removeBoundLoreAbility(StrifeMob mob, LoreAbility loreAbility) {
    Champion champion = mob.getChampion();
    if (champion == null) {
      return false;
    }
    if (!champion.getSaveData().getBoundAbilities().contains(loreAbility)) {
      return false;
    }
    champion.getSaveData().getBoundAbilities().remove(loreAbility);
    mob.getEquipmentCache().recombineAbilities(mob);
    return true;
  }

  public void updateRecentSkills(Champion champion, LifeSkillType type) {
    if (!champion.getRecentSkills().contains(type)) {
      if (champion.getRecentSkills().size() < 3) {
        champion.getRecentSkills().add(type);
      } else {
        champion.getRecentSkills().set(2, champion.getRecentSkills().get(1));
        champion.getRecentSkills().set(1, champion.getRecentSkills().get(0));
        champion.getRecentSkills().set(0, type);
      }
    }
  }

  public void pushCloseSkills(Champion champion) {
    SortedMap<Float, LifeSkillType> map = new TreeMap<>();
    for (LifeSkillType lifeSkillType : LifeSkillType.types) {
      int level = champion.getLifeSkillLevel(lifeSkillType);
      if (level < 2 || level > 98) {
        continue;
      }
      float progress = PlayerDataUtil.getSkillProgress(champion, lifeSkillType);
      if (map.size() < 3) {
        map.put(progress, lifeSkillType);
      } else if (progress > map.firstKey()) {
        map.remove(map.firstKey());
        map.put(progress, lifeSkillType);
      }
    }
    for (LifeSkillType val : map.values()) {
      updateRecentSkills(champion, val);
    }
  }

  private int getTotalChampionStats(Champion champion) {
    ChampionSaveData championSaveData = champion.getSaveData();
    int total = championSaveData.getUnusedStatPoints();
    for (StrifeAttribute stat : championSaveData.getLevelMap().keySet()) {
      total += champion.getLevelMap().getOrDefault(stat, 0);
    }
    return total;
  }

  private void notifyResetPoints(final Player player) {
    Bukkit.getScheduler().runTaskLater(plugin,
        () -> MessageUtils.sendMessage(player, RESET_MESSAGE), 20L * 3);
  }
}
