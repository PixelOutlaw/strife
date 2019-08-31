package info.faceland.strife.data.champion;

import info.faceland.strife.data.LoreAbility;
import info.faceland.strife.managers.LoreAbilityManager;
import info.faceland.strife.managers.LoreAbilityManager.TriggerType;
import info.faceland.strife.managers.StatUpdateManager;
import info.faceland.strife.stats.StrifeStat;
import info.faceland.strife.stats.StrifeTrait;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.inventory.EquipmentSlot;

public class PlayerEquipmentCache {

  private final Map<EquipmentSlot, Integer> slotHashCodeMap = new HashMap<>();

  private final Map<EquipmentSlot, Map<StrifeStat, Float>> slotStatMap = new HashMap<>();
  private final Map<EquipmentSlot, List<LoreAbility>> slotAbilityMap = new HashMap<>();
  private final Map<EquipmentSlot, List<StrifeTrait>> slotTraitMap = new HashMap<>();

  private final Map<TriggerType, Set<LoreAbility>> loreAbilities = new HashMap<>();
  private final Map<StrifeStat, Float> combinedStats = new HashMap<>();
  private final Set<StrifeTrait> combinedTraits = new HashSet<>();

  public final static EquipmentSlot[] itemSlots = EquipmentSlot.values();

  PlayerEquipmentCache() {
    for (EquipmentSlot slot : itemSlots) {
      slotHashCodeMap.put(slot, -1);
      slotStatMap.put(slot, new HashMap<>());
      slotAbilityMap.put(slot, new ArrayList<>());
      slotTraitMap.put(slot, new ArrayList<>());
    }
    for (TriggerType triggerType : LoreAbilityManager.triggerTypes) {
      loreAbilities.put(triggerType, new HashSet<>());
    }
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

  public Map<TriggerType, Set<LoreAbility>> getCombinedAbilities() {
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

  public void recombineAbilities(Champion champion) {
    for (TriggerType triggerType : LoreAbilityManager.triggerTypes) {
      loreAbilities.get(triggerType).clear();
    }
    Set<LoreAbility> newAbilities = new HashSet<>(champion.getSaveData().getBoundAbilities());
    for (EquipmentSlot slot : itemSlots) {
      newAbilities.addAll(slotAbilityMap.get(slot));
    }
    for (LoreAbility la : newAbilities) {
      loreAbilities.get(la.getTriggerType()).add(la);
    }
  }

  public void recombineTraits() {
    combinedTraits.clear();
    for (EquipmentSlot slot : slotTraitMap.keySet()) {
      combinedTraits.addAll(slotTraitMap.get(slot));
    }
  }

  public void recombine(Champion champion) {
    recombineStats();
    recombineAbilities(champion);
    recombineTraits();
  }
}
