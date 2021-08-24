package land.face.strife.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import land.face.strife.data.ability.EntityAbilitySet;
import land.face.strife.stats.StrifeStat;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class MobMod {

  private String id;
  private String prefix;
  private int weight;
  private EntityAbilitySet abilitySet;
  private final Map<EquipmentSlot, ItemStack> equipment = new HashMap<>();
  private Map<StrifeStat, Float> baseStats = new HashMap<>();
  private Map<StrifeStat, Float> perLevelStats = new HashMap<>();

  private final Set<EntityType> validEntities = new HashSet<>();
  private final Set<String> validRegionIds = new HashSet<>();
  private final Set<Biome> validBiomes = new HashSet<>();

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public int getWeight() {
    return weight;
  }

  public void setWeight(int weight) {
    this.weight = weight;
  }

  public EntityAbilitySet getAbilitySet() {
    return abilitySet;
  }

  public void setAbilitySet(EntityAbilitySet abilitySet) {
    this.abilitySet = abilitySet;
  }

  public Map<StrifeStat, Float> getBaseStats() {
    return baseStats;
  }

  public void setBaseStats(Map<StrifeStat, Float> baseStats) {
    this.baseStats = baseStats;
  }

  public Map<StrifeStat, Float> getPerLevelStats() {
    return perLevelStats;
  }

  public void setPerLevelStats(Map<StrifeStat, Float> perLevelStats) {
    this.perLevelStats = perLevelStats;
  }

  public Map<EquipmentSlot, ItemStack> getEquipment() {
    return equipment;
  }

  public ItemStack getEquipmentItem(EquipmentSlot slot) {
    return equipment.get(slot);
  }

  public void addValidEntity(EntityType entityType) {
    this.validEntities.add(entityType);
  }
  public void addValidBiome(Biome biome) {
    this.validBiomes.add(biome);
  }

  public void addValidRegion(String id) {
    this.validRegionIds.add(id);
  }

  public Set<EntityType> getValidEntities() {
    return validEntities;
  }

  public Set<String> getValidRegionIds() {
    return validRegionIds;
  }

  public Set<Biome> getValidBiomes() {
    return validBiomes;
  }
}
