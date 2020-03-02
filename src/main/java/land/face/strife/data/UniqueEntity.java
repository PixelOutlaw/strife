package land.face.strife.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import land.face.strife.data.ability.EntityAbilitySet;
import land.face.strife.data.effects.StrifeParticle;
import land.face.strife.stats.StrifeStat;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class UniqueEntity {

  private String id;
  private EntityType type;
  private String name;
  private int bonusExperience;
  private float experienceMultiplier;
  private Map<StrifeStat, Float> attributeMap;
  private Set<String> factions = new HashSet<>();
  private EntityAbilitySet abilitySet;
  private int baseLevel;
  private boolean showName;
  private boolean baby;
  private int size;
  private int followRange = -1;
  private boolean knockbackImmune;
  private boolean charmImmune;
  private boolean burnImmune;
  private boolean fallImmune;
  private boolean ignoreSneak;
  private int maxMods;
  private boolean removeFollowMods;
  private boolean powered;
  private double displaceMultiplier;
  private String mount;
  private Map<EquipmentSlot, ItemStack> equipment = new HashMap<>();
  private StrifeParticle strifeParticle;

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

  public float getExperienceMultiplier() {
    return experienceMultiplier;
  }

  public void setExperienceMultiplier(float experienceMultiplier) {
    this.experienceMultiplier = experienceMultiplier;
  }

  public int getBonusExperience() {
    return bonusExperience;
  }

  public void setBonusExperience(int bonusExperience) {
    this.bonusExperience = bonusExperience;
  }

  public Map<StrifeStat, Float> getAttributeMap() {
    return attributeMap;
  }

  public void setAttributeMap(Map<StrifeStat, Float> attributeMap) {
    this.attributeMap = attributeMap;
  }

  public Set<String> getFactions() {
    return factions;
  }

  public int getBaseLevel() {
    return baseLevel;
  }

  public void setBaseLevel(int baseLevel) {
    this.baseLevel = baseLevel;
  }

  public EntityAbilitySet getAbilitySet() {
    return abilitySet;
  }

  public void setAbilitySet(EntityAbilitySet abilitySet) {
    this.abilitySet = abilitySet;
  }

  public boolean isShowName() {
    return showName;
  }

  public void setShowName(boolean showName) {
    this.showName = showName;
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

  public boolean isIgnoreSneak() {
    return ignoreSneak;
  }

  public void setIgnoreSneak(boolean ignoreSneak) {
    this.ignoreSneak = ignoreSneak;
  }

  public int getMaxMods() {
    return maxMods;
  }

  public void setMaxMods(int maxMods) {
    this.maxMods = maxMods;
  }

  public boolean isRemoveFollowMods() {
    return removeFollowMods;
  }

  public void setRemoveFollowMods(boolean removeFollowMods) {
    this.removeFollowMods = removeFollowMods;
  }

  public boolean isPowered() {
    return powered;
  }

  public void setPowered(boolean powered) {
    this.powered = powered;
  }

  public double getDisplaceMultiplier() {
    return displaceMultiplier;
  }

  public void setDisplaceMultiplier(double displaceMultiplier) {
    this.displaceMultiplier = displaceMultiplier;
  }

  public String getMount() {
    return mount;
  }

  public void setMount(String mount) {
    this.mount = mount;
  }

  public boolean isBurnImmune() {
    return burnImmune;
  }

  public void setBurnImmune(boolean burnImmune) {
    this.burnImmune = burnImmune;
  }

  public boolean isFallImmune() {
    return fallImmune;
  }

  public void setFallImmune(boolean fallImmune) {
    this.fallImmune = fallImmune;
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

  public StrifeParticle getStrifeParticle() {
    return strifeParticle;
  }

  public void setStrifeParticle(StrifeParticle strifeParticle) {
    this.strifeParticle = strifeParticle;
  }
}
