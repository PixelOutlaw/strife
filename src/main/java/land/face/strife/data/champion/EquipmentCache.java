package land.face.strife.data.champion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import land.face.strife.data.LoreAbility;
import land.face.strife.data.StrifeMob;
import land.face.strife.managers.LoreAbilityManager;
import land.face.strife.managers.LoreAbilityManager.TriggerType;
import land.face.strife.managers.StatUpdateManager;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.EquipmentSlot;

public class EquipmentCache {

  @Getter
  @Setter
  private long lastUpdate;

  private final Map<EquipmentSlot, Integer> slotHashCodeMap = new HashMap<>();

  private final Map<EquipmentSlot, Map<StrifeStat, Float>> slotStatMap = new HashMap<>();
  private final Map<EquipmentSlot, List<LoreAbility>> slotAbilityMap = new HashMap<>();
  private final Map<EquipmentSlot, List<StrifeTrait>> slotTraitMap = new HashMap<>();

  private final Set<LoreAbility> loreAbilities = new HashSet<>();
  private final Map<StrifeStat, Float> combinedStats = new HashMap<>();
  private final Set<StrifeTrait> combinedTraits = new HashSet<>();

  public final static EquipmentSlot[] ITEM_SLOTS = EquipmentSlot.values();

  public EquipmentCache() {
    for (EquipmentSlot slot : ITEM_SLOTS) {
      slotHashCodeMap.put(slot, -1);
      slotStatMap.put(slot, new HashMap<>());
      slotAbilityMap.put(slot, new ArrayList<>());
      slotTraitMap.put(slot, new ArrayList<>());
    }
    lastUpdate = System.currentTimeMillis();
  }

  public void setSlotStats(EquipmentSlot slot, Map<StrifeStat, Float> stats) {
    this.slotStatMap.get(slot).clear();
    this.slotStatMap.get(slot).putAll(stats);
  }

  public Map<StrifeStat, Float> getSlotStats(EquipmentSlot slot) {
    return this.slotStatMap.get(slot);
  }

  public void setSlotTraits(EquipmentSlot slot, Set<StrifeTrait> traits) {
    this.slotTraitMap.get(slot).clear();
    this.slotTraitMap.get(slot).addAll(traits);
  }

  public List<StrifeTrait> getSlotTraits(EquipmentSlot slot) {
    return this.slotTraitMap.get(slot);
  }

  public void setSlotAbilities(EquipmentSlot slot, Set<LoreAbility> abilities) {
    this.slotAbilityMap.get(slot).clear();
    this.slotAbilityMap.get(slot).addAll(abilities);
  }

  public List<LoreAbility> getSlotAbilities(EquipmentSlot slot) {
    return this.slotAbilityMap.get(slot);
  }

  public int getSlotHash(EquipmentSlot slot) {
    return slotHashCodeMap.get(slot);
  }

  public void setSlotHash(EquipmentSlot slot, int hash) {
    this.slotHashCodeMap.put(slot, hash);
  }

  public void clearSlot(EquipmentSlot slot) {
    this.slotAbilityMap.get(slot).clear();
    this.slotStatMap.get(slot).clear();
    this.slotTraitMap.get(slot).clear();
  }

  public Map<StrifeStat, Float> getCombinedStats() {
    return combinedStats;
  }

  public Set<LoreAbility> getCombinedAbilities() {
    return loreAbilities;
  }

  public Set<StrifeTrait> getCombinedTraits() {
    return combinedTraits;
  }

  public void recombineStats() {
    combinedStats.clear();
    combinedStats.putAll(StatUpdateManager.combineMaps(
        slotStatMap.get(EquipmentSlot.HAND),
        slotStatMap.get(EquipmentSlot.OFF_HAND),
        slotStatMap.get(EquipmentSlot.HEAD),
        slotStatMap.get(EquipmentSlot.CHEST),
        slotStatMap.get(EquipmentSlot.LEGS),
        slotStatMap.get(EquipmentSlot.FEET)
    ));
  }

  public void recombineAbilities(StrifeMob mob) {
    loreAbilities.clear();
    if (mob.getChampion() != null) {
      loreAbilities.addAll(mob.getChampion().getSaveData().getBoundAbilities());
    }
    for (EquipmentSlot slot : ITEM_SLOTS) {
      loreAbilities.addAll(slotAbilityMap.get(slot));
    }
  }

  public void recombineTraits() {
    combinedTraits.clear();
    for (EquipmentSlot slot : slotTraitMap.keySet()) {
      combinedTraits.addAll(slotTraitMap.get(slot));
    }
  }

  public void recombine(StrifeMob mob) {
    recombineStats();
    recombineAbilities(mob);
    recombineTraits();
  }
}
