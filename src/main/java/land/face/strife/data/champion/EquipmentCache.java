package land.face.strife.data.champion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import land.face.dinvy.windows.equipment.EquipmentMenu.DeluxeSlot;
import land.face.strife.StrifePlugin;
import land.face.strife.data.LoreAbility;
import land.face.strife.data.StrifeMob;
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

  private final Map<String, Integer> slotHashCodeMap = new HashMap<>();

  private final Map<String, Map<StrifeStat, Float>> slotStatMap = new HashMap<>();
  private final Map<String, List<LoreAbility>> slotAbilityMap = new HashMap<>();
  private final Map<String, List<StrifeTrait>> slotTraitMap = new HashMap<>();

  private final Set<LoreAbility> loreAbilities = new HashSet<>();
  private final Map<StrifeStat, Float> combinedStats = new HashMap<>();
  private final Set<StrifeTrait> combinedTraits = new HashSet<>();

  public final static Set<String> ITEM_SLOTS = buildSlots();
  public final static EquipmentSlot[] EQUIPMENT_SLOTS = EquipmentSlot.values();
  public final static DeluxeSlot[] DELUXE_SLOTS = DeluxeSlot.values();

  private static Set<String> buildSlots() {
    Set<String> keys = new HashSet<>();
    for (DeluxeSlot slot : DeluxeSlot.values()) {
      keys.add(slot.toString());
    }
    keys.add("HAND");
    keys.add("OFF_HAND");
    return keys;
  }

  public EquipmentCache() {
    for (String key : ITEM_SLOTS) {
      slotHashCodeMap.put(key, -1);
      slotStatMap.put(key, new HashMap<>());
      slotAbilityMap.put(key, new ArrayList<>());
      slotTraitMap.put(key, new ArrayList<>());
    }
    lastUpdate = System.currentTimeMillis();
  }

  public void setSlotStats(String slot, Map<StrifeStat, Float> stats) {
    this.slotStatMap.get(slot).clear();
    this.slotStatMap.get(slot).putAll(stats);
  }

  public Map<StrifeStat, Float> getSlotStats(String slot) {
    return this.slotStatMap.get(slot);
  }

  public void setSlotTraits(String slot, Set<StrifeTrait> traits) {
    this.slotTraitMap.get(slot).clear();
    this.slotTraitMap.get(slot).addAll(traits);
  }

  public List<StrifeTrait> getSlotTraits(String slot) {
    return this.slotTraitMap.get(slot);
  }

  public void setSlotAbilities(String slot, Set<LoreAbility> abilities) {
    this.slotAbilityMap.get(slot).clear();
    this.slotAbilityMap.get(slot).addAll(abilities);
  }

  public List<LoreAbility> getSlotAbilities(String slot) {
    return this.slotAbilityMap.get(slot);
  }

  public int getSlotHash(String slot) {
    return slotHashCodeMap.get(slot);
  }

  public void setSlotHash(String slot, int hash) {
    this.slotHashCodeMap.put(slot, hash);
  }

  public void clearSlot(String slot) {
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
        slotStatMap.get("HAND"),
        slotStatMap.get("OFF_HAND"),
        slotStatMap.get("HELMET"),
        slotStatMap.get("BODY"),
        slotStatMap.get("LEGS"),
        slotStatMap.get("BOOTS"),
        slotStatMap.get("NECKLACE"),
        slotStatMap.get("RING_1"),
        slotStatMap.get("RING_2"),
        slotStatMap.get("EARRING_1"),
        slotStatMap.get("EARRING_2"),
        slotStatMap.get("COSMETIC_HAT"),
        slotStatMap.get("PET"),
        slotStatMap.get("SOUL_GEM_1"),
        slotStatMap.get("SOUL_GEM_2"),
        slotStatMap.get("SOUL_GEM_3"),
        slotStatMap.get("SOUL_GEM_4")
    ));
  }

  public void recombineAbilities(StrifePlugin plugin, StrifeMob mob) {
    loreAbilities.clear();
    if (mob.getChampion() != null) {
      for (String la : mob.getChampion().getSaveData().getBoundAbilities()) {
        LoreAbility loreAbility = plugin.getLoreAbilityManager().getLoreAbilityFromId(la);
        if (loreAbility != null) {
          loreAbilities.add(loreAbility);
        }
      }
    }
    for (String key : ITEM_SLOTS) {
      loreAbilities.addAll(slotAbilityMap.get(key));
    }
  }

  public void recombineTraits() {
    combinedTraits.clear();
    for (String key : slotTraitMap.keySet()) {
      combinedTraits.addAll(slotTraitMap.get(key));
    }
  }

  public void recombine(StrifePlugin plugin, StrifeMob mob) {
    recombineStats();
    recombineAbilities(plugin, mob);
    recombineTraits();
  }
}
