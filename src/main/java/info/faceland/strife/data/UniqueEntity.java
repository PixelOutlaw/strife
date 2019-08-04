package info.faceland.strife.data;

import info.faceland.strife.data.ability.EntityAbilitySet;
import info.faceland.strife.effects.SpawnParticle;
import info.faceland.strife.stats.StrifeStat;
import java.util.Map;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class UniqueEntity {

  private String id;
  private EntityType type;
  private String name;
  private int experience;
  private Map<StrifeStat, Double> attributeMap;
  private EntityAbilitySet abilitySet;

  private boolean baby;
  private int size;
  private int followRange = -1;
  private boolean knockbackImmune;

  private ItemStack mainHandItem = null;
  private ItemStack offHandItem = null;
  private ItemStack helmetItem = null;
  private ItemStack chestItem = null;
  private ItemStack legsItem = null;
  private ItemStack bootsItem = null;

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

  public Map<StrifeStat, Double> getAttributeMap() {
    return attributeMap;
  }

  public void setAttributeMap(Map<StrifeStat, Double> attributeMap) {
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

  public ItemStack getMainHandItem() {
    return mainHandItem;
  }

  public void setMainHandItem(ItemStack mainHandItem) {
    this.mainHandItem = mainHandItem;
  }

  public ItemStack getOffHandItem() {
    return offHandItem;
  }

  public void setOffHandItem(ItemStack offHandItem) {
    this.offHandItem = offHandItem;
  }

  public ItemStack getHelmetItem() {
    return helmetItem;
  }

  public void setHelmetItem(ItemStack helmetItem) {
    this.helmetItem = helmetItem;
  }

  public ItemStack getChestItem() {
    return chestItem;
  }

  public void setChestItem(ItemStack chestItem) {
    this.chestItem = chestItem;
  }

  public ItemStack getLegsItem() {
    return legsItem;
  }

  public void setLegsItem(ItemStack legsItem) {
    this.legsItem = legsItem;
  }

  public ItemStack getBootsItem() {
    return bootsItem;
  }

  public void setBootsItem(ItemStack bootsItem) {
    this.bootsItem = bootsItem;
  }

  public SpawnParticle getSpawnParticle() {
    return spawnParticle;
  }

  public void setSpawnParticle(SpawnParticle spawnParticle) {
    this.spawnParticle = spawnParticle;
  }
}
