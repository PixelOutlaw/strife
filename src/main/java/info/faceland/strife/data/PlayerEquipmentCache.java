package info.faceland.strife.data;

import static info.faceland.strife.managers.LoreAbilityManager.TriggerType.*;

import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.managers.AttributeUpdateManager;
import info.faceland.strife.managers.LoreAbilityManager.TriggerType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PlayerEquipmentCache {

  private final Map<StrifeAttribute, Double> mainhandStats = new HashMap<>();
  private final Map<StrifeAttribute, Double> offhandStats = new HashMap<>();
  private final Map<StrifeAttribute, Double> helmetStats = new HashMap<>();
  private final Map<StrifeAttribute, Double> chestplateStats = new HashMap<>();
  private final Map<StrifeAttribute, Double> leggingsStats = new HashMap<>();
  private final Map<StrifeAttribute, Double> bootsStats = new HashMap<>();
  private final List<LoreAbility> mainAbilities = new ArrayList<>();
  private final List<LoreAbility> offhandAbilities = new ArrayList<>();
  private final List<LoreAbility> helmetAbilities = new ArrayList<>();
  private final List<LoreAbility> chestAbilities = new ArrayList<>();
  private final List<LoreAbility> legsAbilities = new ArrayList<>();
  private final List<LoreAbility> bootAbilities = new ArrayList<>();

  private final Map<StrifeAttribute, Double> combinedStats = new HashMap<>();
  private final Map<TriggerType, List<LoreAbility>> loreAbilities = new HashMap<>();

  private int mainHandHash = -1;
  private int offHandHash = -1;
  private int helmetHash = -1;
  private int chestHash = -1;
  private int legsHash = -1;
  private int bootsHash = -1;

  public PlayerEquipmentCache() {
    // Don't use TriggerType.values() here, MC has enough performance and memory problems already
    this.loreAbilities.put(ON_HIT, new ArrayList<>());
    this.loreAbilities.put(ON_KILL, new ArrayList<>());
    this.loreAbilities.put(ON_CRIT, new ArrayList<>());
    this.loreAbilities.put(ON_BLOCK, new ArrayList<>());
    this.loreAbilities.put(ON_EVADE, new ArrayList<>());
    this.loreAbilities.put(ON_SNEAK, new ArrayList<>());
    this.loreAbilities.put(WHEN_HIT, new ArrayList<>());
  }

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
    combineLoreAbilities();
  }

  public Map<StrifeAttribute, Double> getMainhandStats() {
    return mainhandStats;
  }

  public void setMainhandStats(Map<StrifeAttribute, Double> mainhandStats) {
    this.mainhandStats.clear();
    this.mainhandStats.putAll(mainhandStats);
  }

  public Map<StrifeAttribute, Double> getOffhandStats() {
    return offhandStats;
  }

  public void setOffhandStats(Map<StrifeAttribute, Double> offhandStats) {
    this.offhandStats.clear();
    this.offhandStats.putAll(offhandStats);
  }

  public Map<StrifeAttribute, Double> getHelmetStats() {
    return helmetStats;
  }

  public void setHelmetStats(Map<StrifeAttribute, Double> helmetStats) {
    this.helmetStats.clear();
    this.helmetStats.putAll(helmetStats);
  }

  public Map<StrifeAttribute, Double> getChestplateStats() {
    return chestplateStats;
  }

  public void setChestplateStats(Map<StrifeAttribute, Double> chestplateStats) {
    this.chestplateStats.clear();
    this.chestplateStats.putAll(chestplateStats);
  }

  public Map<StrifeAttribute, Double> getLeggingsStats() {
    return leggingsStats;
  }

  public void setLeggingsStats(Map<StrifeAttribute, Double> leggingsStats) {
    this.leggingsStats.clear();
    this.leggingsStats.putAll(leggingsStats);
  }

  public Map<StrifeAttribute, Double> getBootsStats() {
    return bootsStats;
  }

  public void setBootsStats(Map<StrifeAttribute, Double> bootsStats) {
    this.bootsStats.clear();
    this.bootsStats.putAll(bootsStats);
  }

  public List<LoreAbility> getMainAbilities() {
    return mainAbilities;
  }

  public List<LoreAbility> getOffhandAbilities() {
    return offhandAbilities;
  }

  public List<LoreAbility> getHelmetAbilities() {
    return helmetAbilities;
  }

  public List<LoreAbility> getChestAbilities() {
    return chestAbilities;
  }

  public List<LoreAbility> getLegsAbilities() {
    return legsAbilities;
  }

  public List<LoreAbility> getBootAbilities() {
    return bootAbilities;
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

  public Map<TriggerType, List<LoreAbility>> getLoreAbilities() {
    return loreAbilities;
  }

  private void combineLoreAbilities() {
    for (Entry<TriggerType, List<LoreAbility>> entry : loreAbilities.entrySet()) {
      entry.getValue().clear();
    }
    List<LoreAbility> newAbilities = new ArrayList<>();
    newAbilities.addAll(mainAbilities);
    newAbilities.addAll(offhandAbilities);
    newAbilities.addAll(helmetAbilities);
    newAbilities.addAll(chestAbilities);
    newAbilities.addAll(legsAbilities);
    newAbilities.addAll(bootAbilities);
    for (LoreAbility loreAbility : newAbilities) {
      loreAbilities.get(loreAbility.getTriggerType()).add(loreAbility);
    }
  }
}
