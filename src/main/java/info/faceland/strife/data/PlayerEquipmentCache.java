package info.faceland.strife.data;

import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.managers.AttributeUpdateManager;
import java.util.HashMap;
import java.util.Map;

public class PlayerEquipmentCache {

  private Map<StrifeAttribute, Double> mainhandStats = new HashMap<>();
  private Map<StrifeAttribute, Double> offhandStats = new HashMap<>();
  private Map<StrifeAttribute, Double> helmetStats = new HashMap<>();
  private Map<StrifeAttribute, Double> chestplateStats = new HashMap<>();
  private Map<StrifeAttribute, Double> leggingsStats = new HashMap<>();
  private Map<StrifeAttribute, Double> bootsStats = new HashMap<>();

  private Map<StrifeAttribute, Double> combinedStats = new HashMap<>();

  private int mainHandHash = -1;
  private int offHandHash = -1;
  private int helmetHash = -1;
  private int chestHash = -1;
  private int legsHash = -1;
  private int bootsHash = -1;

  public void recombine() {
    combinedStats.clear();
    combinedStats.putAll(AttributeUpdateManager.combineMaps(
        mainhandStats,
        offhandStats,
        helmetStats,
        chestplateStats,
        leggingsStats,
        bootsStats
    ));
  }

  public Map<StrifeAttribute, Double> getMainhandStats() {
    return mainhandStats;
  }

  public void setMainhandStats(Map<StrifeAttribute, Double> mainhandStats) {
    this.mainhandStats = mainhandStats;
  }

  public Map<StrifeAttribute, Double> getOffhandStats() {
    return offhandStats;
  }

  public void setOffhandStats(Map<StrifeAttribute, Double> offhandStats) {
    this.offhandStats = offhandStats;
  }

  public Map<StrifeAttribute, Double> getHelmetStats() {
    return helmetStats;
  }

  public void setHelmetStats(Map<StrifeAttribute, Double> helmetStats) {
    this.helmetStats = helmetStats;
  }

  public Map<StrifeAttribute, Double> getChestplateStats() {
    return chestplateStats;
  }

  public void setChestplateStats(Map<StrifeAttribute, Double> chestplateStats) {
    this.chestplateStats = chestplateStats;
  }

  public Map<StrifeAttribute, Double> getLeggingsStats() {
    return leggingsStats;
  }

  public void setLeggingsStats(Map<StrifeAttribute, Double> leggingsStats) {
    this.leggingsStats = leggingsStats;
  }

  public Map<StrifeAttribute, Double> getBootsStats() {
    return bootsStats;
  }

  public void setBootsStats(
      Map<StrifeAttribute, Double> bootsStats) {
    this.bootsStats = bootsStats;
  }

  public Map<StrifeAttribute, Double> getCombinedStats() {
    return combinedStats;
  }

  public int getMainHandHash() {
    return mainHandHash;
  }

  public void setMainHandHash(int mainHandHash) {
    this.mainHandHash = mainHandHash;
  }

  public int getOffHandHash() {
    return offHandHash;
  }

  public void setOffHandHash(int offHandHash) {
    this.offHandHash = offHandHash;
  }

  public int getHelmetHash() {
    return helmetHash;
  }

  public void setHelmetHash(int helmetHash) {
    this.helmetHash = helmetHash;
  }

  public int getChestHash() {
    return chestHash;
  }

  public void setChestHash(int chestHash) {
    this.chestHash = chestHash;
  }

  public int getLegsHash() {
    return legsHash;
  }

  public void setLegsHash(int legsHash) {
    this.legsHash = legsHash;
  }

  public int getBootsHash() {
    return bootsHash;
  }

  public void setBootsHash(int bootsHash) {
    this.bootsHash = bootsHash;
  }
}
