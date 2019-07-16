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

  private int craftingLevel;
  private float craftingExp;
  private int fishingLevel;
  private float fishingExp;
  private int enchantLevel;
  private float enchantExp;
  private int miningLevel;
  private float miningExp;
  private int sneakLevel;
  private float sneakExp;

  private int swordLevel;
  private float swordExp;
  private int axeLevel;
  private float axeExp;
  private int dualLevel;
  private float dualExp;
  private int shieldLevel;
  private float shieldExp;
  private int archeryLevel;
  private float archeryExp;
  private int magicLevel;
  private float magicExp;

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
    switch (type) {
      case CRAFTING:
        craftingLevel = level;
        return;
      case ENCHANTING:
        enchantLevel = level;
        return;
      case FISHING:
        fishingLevel = level;
        return;
      case MINING:
        miningLevel = level;
        return;
      case SNEAK:
        sneakLevel = level;
        return;
      case SWORDSMANSHIP:
        swordLevel = level;
        return;
      case AXE_MASTERY:
        axeLevel = level;
        return;
      case DUAL_WIELDING:
        dualLevel = level;
        return;
      case SHIELD_MASTERY:
        shieldLevel = level;
        return;
      case ARCHERY:
        archeryLevel = level;
        return;
      case MAGECRAFT:
        magicLevel = level;
        return;
      default:
        throw new IllegalArgumentException("Invalid life skill type!");
    }
  }

  public void setSkillExp(LifeSkillType type, float amount) {
    switch (type) {
      case CRAFTING:
        craftingExp = amount;
        return;
      case ENCHANTING:
        enchantExp = amount;
        return;
      case FISHING:
        fishingExp = amount;
        return;
      case MINING:
        miningExp = amount;
        return;
      case SNEAK:
        sneakExp = amount;
        return;
      case SWORDSMANSHIP:
        swordExp = amount;
        return;
      case AXE_MASTERY:
        axeExp = amount;
        return;
      case DUAL_WIELDING:
        dualExp = amount;
        return;
      case SHIELD_MASTERY:
        shieldExp = amount;
        return;
      case ARCHERY:
        archeryExp = amount;
        return;
      case MAGECRAFT:
        magicExp = amount;
        return;
      default:
        throw new IllegalArgumentException("Invalid life skill type!");
    }
  }

  public int getSkillLevel(LifeSkillType type) {
    switch (type) {
      case CRAFTING:
        return craftingLevel;
      case ENCHANTING:
        return enchantLevel;
      case FISHING:
        return fishingLevel;
      case MINING:
        return miningLevel;
      case SNEAK:
        return sneakLevel;
      case SWORDSMANSHIP:
        return swordLevel;
      case AXE_MASTERY:
        return axeLevel;
      case DUAL_WIELDING:
        return dualLevel;
      case SHIELD_MASTERY:
        return shieldLevel;
      case ARCHERY:
        return archeryLevel;
      case MAGECRAFT:
        return magicLevel;
      default:
        throw new IllegalArgumentException("Invalid life skill type!");
    }
  }

  public float getSkillExp(LifeSkillType type) {
    switch (type) {
      case CRAFTING:
        return craftingExp;
      case ENCHANTING:
        return enchantExp;
      case FISHING:
        return fishingExp;
      case MINING:
        return miningExp;
      case SNEAK:
        return sneakExp;
      case SWORDSMANSHIP:
        return swordExp;
      case AXE_MASTERY:
        return axeExp;
      case DUAL_WIELDING:
        return dualExp;
      case SHIELD_MASTERY:
        return shieldExp;
      case ARCHERY:
        return archeryExp;
      case MAGECRAFT:
        return magicExp;
      default:
        throw new IllegalArgumentException("Invalid life skill type!");
    }
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
