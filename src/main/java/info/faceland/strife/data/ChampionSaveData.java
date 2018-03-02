package info.faceland.strife.data;

import info.faceland.strife.stats.StrifeStat;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ChampionSaveData {

  private UUID uniqueId;
  private Map<StrifeStat, Integer> levelMap;
  private int unusedStatPoints;
  private int highestReachedLevel;
  private int bonusLevels;

  public ChampionSaveData(UUID uniqueId) {
    this.uniqueId = uniqueId;
    this.levelMap = new HashMap<>();
  }

  public int getLevel(StrifeStat stat) {
    if (levelMap.containsKey(stat)) {
      return levelMap.get(stat);
    }
    return 0;
  }

  public int getBonusLevels() {
    return bonusLevels;
  }

  public void setBonusLevels(int bonusLevels) {
    this.bonusLevels = bonusLevels;
  }

  public int getUnusedStatPoints() {
    return unusedStatPoints;
  }

  public void setUnusedStatPoints(int unusedStatPoints) {
    this.unusedStatPoints = unusedStatPoints;
  }

  public int getHighestReachedLevel() {
    return highestReachedLevel;
  }

  public void setHighestReachedLevel(int highestReachedLevel) {
    this.highestReachedLevel = highestReachedLevel;
  }

  public UUID getUniqueId() {
    return uniqueId;
  }

  public void setLevel(StrifeStat stat, int level) {
    levelMap.put(stat, level);
  }

  public Map<StrifeStat, Integer> getLevelMap() {
    return new HashMap<>(levelMap);
  }

  public Player getPlayer() {
    return Bukkit.getPlayer(getUniqueId());
  }

}
