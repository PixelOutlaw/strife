package info.faceland.strife.data.champion;

import info.faceland.strife.data.LoreAbility;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ChampionSaveData {

  public static final HealthDisplayType[] DISPLAY_OPTIONS = HealthDisplayType.values();

  private final UUID uniqueId;
  private final Map<StrifeAttribute, Integer> levelMap;
  private final Map<StrifeAttribute, Integer> pendingStats;
  private final List<LoreAbility> boundAbilities;

  private HealthDisplayType healthDisplayType;
  private boolean displayExp;

  private int unusedStatPoints;
  private int pendingUnusedStatPoints;
  private int highestReachedLevel;
  private int bonusLevels;

  private final Map<LifeSkillType, Integer> skillLevelMap = new HashMap<>();
  private final Map<LifeSkillType, Float> skillExpMap = new HashMap<>();

  public ChampionSaveData(UUID uniqueId) {
    this.uniqueId = uniqueId;
    this.levelMap = new HashMap<>();
    this.pendingStats = new HashMap<>();
    this.boundAbilities = new ArrayList<>();
    this.healthDisplayType = HealthDisplayType.TEN_HEALTH_HEARTS;
  }

  public List<LoreAbility> getBoundAbilities() {
    return boundAbilities;
  }

  public HealthDisplayType getHealthDisplayType() {
    return healthDisplayType;
  }

  public void setHealthDisplayType(
      HealthDisplayType healthDisplayType) {
    this.healthDisplayType = healthDisplayType;
  }

  public boolean isDisplayExp() {
    return displayExp;
  }

  public void setDisplayExp(boolean displayExp) {
    this.displayExp = displayExp;
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

  public int getPendingUnusedStatPoints() {
    return pendingUnusedStatPoints;
  }

  public void setPendingUnusedStatPoints(int pendingUnusedStatPoints) {
    this.pendingUnusedStatPoints = pendingUnusedStatPoints;
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

  public void setLevel(StrifeAttribute stat, int level) {
    levelMap.put(stat, level);
  }

  public Map<StrifeAttribute, Integer> getLevelMap() {
    return new HashMap<>(levelMap);
  }

  public void resetPendingStats() {
    pendingStats.clear();
    pendingStats.putAll(levelMap);
    pendingUnusedStatPoints = unusedStatPoints;
  }

  public void savePendingStats() {
    unusedStatPoints = pendingUnusedStatPoints;
    levelMap.clear();
    levelMap.putAll(pendingStats);
  }

  public Player getPlayer() {
    return Bukkit.getPlayer(getUniqueId());
  }

  public Map<StrifeAttribute, Integer> getPendingLevelMap() {
    return pendingStats;
  }

  public void setSkillLevel(LifeSkillType type, int level) {
    skillLevelMap.put(type, level);
  }

  public void setSkillExp(LifeSkillType type, float amount) {
    skillExpMap.put(type, amount);
  }

  public int getSkillLevel(LifeSkillType type) {
    return skillLevelMap.getOrDefault(type, 0);
  }

  public float getSkillExp(LifeSkillType type) {
    return skillExpMap.getOrDefault(type, 0f);
  }

  public enum HealthDisplayType {
    TWO_HEALTH_HEARTS,
    FIVE_HEALTH_HEARTS,
    TEN_HEALTH_HEARTS,
    TEN_PERCENT_HEARTS,
    FIVE_PERCENT_HEARTS,
    THREE_PERCENT_HEARTS
  }
}
