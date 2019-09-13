package info.faceland.strife.data;

import info.faceland.strife.data.ability.EntityAbilitySet;
import info.faceland.strife.data.effects.SpawnParticle;
import info.faceland.strife.stats.StrifeStat;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class UniqueEntity {

  private String id;
  private EntityType type;
  private String name;
  private int experience;
  private Map<StrifeStat, Float> attributeMap;
  private EntityAbilitySet abilitySet;

  private boolean baby;
  private int size;
  private int followRange = -1;
  private boolean knockbackImmune;
  private boolean charmImmune;
  private Map<EquipmentSlot, ItemStack> equipment = new HashMap<>();
  private SpawnParticle spawnParticle;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public EntityType getType() {
    return type;
  }

  public void setType(EntityType type) {
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getExperience() {
    return experience;
  }

  public void setExperience(int experience) {
    this.experience = experience;
  }

  public Map<StrifeStat, Float> getAttributeMap() {
    return attributeMap;
  }

  public void setAttributeMap(Map<StrifeStat, Float> attributeMap) {
    this.attributeMap = attributeMap;
  }

  public EntityAbilitySet getAbilitySet() {
    return abilitySet;
  }

  public void setAbilitySet(EntityAbilitySet abilitySet) {
    this.abilitySet = abilitySet;
  }

  public boolean isBaby() {
    return baby;
  }

  public void setBaby(boolean baby) {
    this.baby = baby;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public int getFollowRange() {
    return followRange;
  }

  public void setFollowRange(int followRange) {
    this.followRange = followRange;
  }

  public boolean isKnockbackImmune() {
    return knockbackImmune;
  }

  public void setKnockbackImmune(boolean knockbackImmune) {
    this.knockbackImmune = knockbackImmune;
  }

  public boolean isCharmImmune() {
    return charmImmune;
  }

  public void setCharmImmune(boolean charmImmune) {
    this.charmImmune = charmImmune;
  }

  public Map<EquipmentSlot, ItemStack> getEquipment() {
    return equipment;
  }

  public ItemStack getEquipmentItem(EquipmentSlot slot) {
    return equipment.get(slot);
  }

  public void setEquipment(Map<EquipmentSlot, ItemStack> equipment) {
    this.equipment = equipment;
  }

  public SpawnParticle getSpawnParticle() {
    return spawnParticle;
  }

  public void setSpawnParticle(SpawnParticle spawnParticle) {
    this.spawnParticle = spawnParticle;
  }
}
