package land.face.strife.data;

import java.util.*;

import land.face.strife.data.ability.EntityAbilitySet;
import land.face.strife.data.effects.StrifeParticle;
import land.face.strife.stats.StrifeStat;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.DyeColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class UniqueEntity {

  private String id;
  private EntityType type;
  private String name;
  private int bonusExperience;
  private float experienceMultiplier;
  private Map<StrifeStat, Float> attributeMap;
  private final Set<String> factions = new HashSet<>();
  private EntityAbilitySet abilitySet;
  private int baseLevel;
  private boolean showName;
  private boolean baby;
  private boolean angry;
  private boolean zombificationImmune;
  private boolean armsRaised;
  private boolean hasAI;
  private boolean gravity;
  @Getter @Setter
  private boolean collidable;
  @Getter @Setter
  private boolean guildMob;
  private Profession profession;
  private boolean invisible;
  @Getter @Setter
  private boolean silent;
  private int size;
  private int followRange = -1;
  @Getter @Setter
  private DyeColor color;
  private boolean pushImmune;
  private boolean charmImmune;
  private boolean burnImmune;
  private boolean fallImmune;
  private boolean ignoreSneak;
  private boolean saddled;
  @Getter @Setter
  private boolean canTarget;
  private int maxMods;
  private boolean removeFollowMods;
  private boolean powered;
  @Getter @Setter
  private boolean attackDisabledOnGlobalCooldown;
  private double displaceMultiplier;
  @Getter @Setter
  private double boundingBonus;
  private String mount;
  private Map<EquipmentSlot, String> equipment = new HashMap<>();
  private ItemStack itemPassenger = null;
  private StrifeParticle strifeParticle;
  @Getter @Setter
  private boolean customAi;
  @Getter @Setter
  private boolean aggressiveAi;
  @Getter @Setter
  private List<String> removeGoals;
  @Getter @Setter
  private List<String> addGoals;
  @Getter
  private final List<String> bonusKnowledge = new ArrayList<>();
  @Getter @Setter
  private String modelId;
  @Getter @Setter
  private boolean vagabondAllowed;

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

  public boolean isAngry() {
    return angry;
  }

  public void setAngry(boolean angry) {
    this.angry = angry;
  }

  public boolean isZombificationImmune() {
    return zombificationImmune;
  }

  public void setZombificationImmune(boolean zombificationImmune) {
    this.zombificationImmune = zombificationImmune;
  }

  public boolean isArmsRaised() {
    return armsRaised;
  }

  public void setArmsRaised(boolean armsRaised) {
    this.armsRaised = armsRaised;
  }

  public boolean isHasAI() {
    return hasAI;
  }

  public void setHasAI(boolean hasAI) {
    this.hasAI = hasAI;
  }


  public boolean isGravity() {
    return gravity;
  }

  public void setGravity(boolean gravity) {
    this.gravity = gravity;
  }

  public Profession getProfession() {
    return profession;
  }

  public void setProfession(Profession profession) {
    this.profession = profession;
  }

  public boolean isInvisible() {
    return invisible;
  }

  public void setInvisible(boolean invisible) {
    this.invisible = invisible;
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

  public boolean isIgnoreSneak() {
    return ignoreSneak;
  }

  public void setIgnoreSneak(boolean ignoreSneak) {
    this.ignoreSneak = ignoreSneak;
  }

  public boolean isSaddled() {
    return saddled;
  }

  public void setSaddled(boolean saddled) {
    this.saddled = saddled;
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

  public boolean isPushImmune() {
    return pushImmune;
  }

  public void setPushImmune(boolean pushImmune) {
    this.pushImmune = pushImmune;
  }

  public boolean isCharmImmune() {
    return charmImmune;
  }

  public void setCharmImmune(boolean charmImmune) {
    this.charmImmune = charmImmune;
  }

  public Map<EquipmentSlot, String> getEquipment() {
    return equipment;
  }

  public void setEquipment(Map<EquipmentSlot, String> equipment) {
    this.equipment = equipment;
  }

  public ItemStack getItemPassenger() {
    return itemPassenger;
  }

  public void setItemPassenger(ItemStack itemPassenger) {
    this.itemPassenger = itemPassenger;
  }

  public StrifeParticle getStrifeParticle() {
    return strifeParticle;
  }

  public void setStrifeParticle(StrifeParticle strifeParticle) {
    this.strifeParticle = strifeParticle;
  }
}
