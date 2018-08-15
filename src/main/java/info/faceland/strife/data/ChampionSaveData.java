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

  private int craftingLevel;
  private float craftingExp;

  private int fishingLevel;
  private float fishingExp;

  private int enchantLevel;
  private float enchantExp;

  private int miningLevel;
  private float miningExp;

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

  // CRAFTING STUFF //
  public int getCraftingLevel() {
    return craftingLevel;
  }

  public void setCraftingLevel(int craftingLevel) {
    this.craftingLevel = craftingLevel;
  }

  public float getCraftingExp() {
    return craftingExp;
  }

  public void setCraftingExp(float craftingExp) {
    this.craftingExp = craftingExp;
  }

  // ENCHANTING STUFF //
  public int getEnchantLevel() {
    return enchantLevel;
  }

  public void setEnchantLevel(int enchantLevel) {
    this.enchantLevel = enchantLevel;
  }

  public float getEnchantExp() {
    return enchantExp;
  }

  public void setEnchantExp(float enchantExp) {
    this.enchantExp = enchantExp;
  }

  // FISHING STUFF //
  public int getFishingLevel() {
    return fishingLevel;
  }

  public void setFishingLevel(int fishingLevel) {
    this.fishingLevel = fishingLevel;
  }

  public float getFishingExp() {
    return fishingExp;
  }

  public void setFishingExp(float fishingExp) {
    this.fishingExp = fishingExp;
  }


  // MINING STUFF //
  public int getMiningLevel() {
    return miningLevel;
  }

  public void setMiningLevel(int miningLevel) {
    this.miningLevel = miningLevel;
  }

  public float getMiningExp() {
    return miningExp;
  }

  public void setMiningExp(float miningExp) {
    this.miningExp = miningExp;
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
