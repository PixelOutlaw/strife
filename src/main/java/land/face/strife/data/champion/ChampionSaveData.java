package land.face.strife.data.champion;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import land.face.strife.data.LevelPath.Choice;
import land.face.strife.data.LevelPath.Path;
import land.face.strife.data.LoreAbility;
import land.face.strife.data.ability.Ability;
import land.face.strife.stats.AbilitySlot;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ChampionSaveData {

  public static final HealthDisplayType[] DISPLAY_OPTIONS = HealthDisplayType.values();

  private final UUID uniqueId;
  private final Map<StrifeAttribute, Integer> levelMap = new HashMap<>();
  private final Map<LifeSkillType, Integer> skillLevelMap = new HashMap<>();
  private final Map<LifeSkillType, Float> skillExpMap = new HashMap<>();
  private final Map<AbilitySlot, Ability> abilities = new HashMap<>();
  private final Map<AbilitySlot, List<String>> castMessages = new HashMap<>();
  private final Map<Path, Choice> pathMap = new HashMap<>();
  private final Set<LoreAbility> boundAbilities = new HashSet<>();

  private final Map<StrifeAttribute, Integer> pendingStats = new HashMap<>();

  private int unusedStatPoints;
  private int pendingUnusedStatPoints;
  private int highestReachedLevel;
  private int bonusLevels;

  private float pvpScore = 700;
  @Getter @Setter
  private float energy = 10000000;

  // Player preferences
  private HealthDisplayType healthDisplayType = HealthDisplayType.TEN_LIFE_PER_HEART;

  private boolean glowEnabled;
  private boolean displayExp;

  public ChampionSaveData(UUID uniqueId) {
    this.uniqueId = uniqueId;
  }

  public Map<AbilitySlot, List<String>> getCastMessages() {
    return castMessages;
  }

  public Map<Path, Choice> getPathMap() {
    return pathMap;
  }

  public void setAbility(AbilitySlot abilitySlot, Ability ability) {
    abilities.put(abilitySlot, ability);
  }

  public Ability getAbility(AbilitySlot abilitySlot) {
    return abilities.get(abilitySlot);
  }

  public Map<AbilitySlot, Ability> getAbilities() {
    return new HashMap<>(abilities);
  }

  public Set<LoreAbility> getBoundAbilities() {
    return boundAbilities;
  }

  public HealthDisplayType getHealthDisplayType() {
    return healthDisplayType;
  }

  public void setHealthDisplayType(HealthDisplayType healthDisplayType) {
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
    return skillLevelMap.getOrDefault(type, 1);
  }

  public float getSkillExp(LifeSkillType type) {
    return skillExpMap.getOrDefault(type, 0f);
  }

  public float getPvpScore() {
    return pvpScore;
  }

  public void setPvpScore(float pvpScore) {
    this.pvpScore = pvpScore;
  }

  public boolean isGlowEnabled() {
    // TODO: allow Enable
    // return glowEnabled;
    return false;
  }

  public void setGlowEnabled(boolean glowEnabled) {
    this.glowEnabled = glowEnabled;
  }

  public enum HealthDisplayType {
    TWENTY_LIFE_PER_HEART,
    TEN_LIFE_PER_HEART,
    FIVE_LIFE_PER_HEART,
    VANILLA_TWO_LIFE_PER_HEART,
    ONE_ROW_OF_LIFE,
    TWO_ROWS_OF_LIFE,
    THREE_ROWS_OF_LIFE
  }
}
