package info.faceland.strife.data;

import info.faceland.strife.data.champion.Champion;
import info.faceland.strife.data.champion.ChampionSaveData.LifeSkillType;
import info.faceland.strife.data.champion.StrifeAttribute;
import info.faceland.strife.stats.AbilitySlot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.inventory.ItemStack;

public class AbilityIconData {

  private ItemStack stack;
  private AbilitySlot abilitySlot;
  private int levelRequirement = 0;
  private int bonusLevelRequirement = 0;
  private Map<LifeSkillType, Integer> lifeSkillRequirements = new HashMap<>();
  private Map<StrifeAttribute, Integer> attributeRequirement = new HashMap<>();

  public AbilityIconData(ItemStack stack) {
    this.stack = stack;
  }

  public ItemStack getStack() {
    return stack;
  }

  public void setStack(ItemStack stack) {
    this.stack = stack;
  }

  public AbilitySlot getAbilitySlot() {
    return abilitySlot;
  }

  public void setAbilitySlot(AbilitySlot abilitySlot) {
    this.abilitySlot = abilitySlot;
  }

  public int getLevelRequirement() {
    return levelRequirement;
  }

  public void setLevelRequirement(int levelRequirement) {
    this.levelRequirement = levelRequirement;
  }

  public int getBonusLevelRequirement() {
    return bonusLevelRequirement;
  }

  public void setBonusLevelRequirement(int bonusLevelRequirement) {
    this.bonusLevelRequirement = bonusLevelRequirement;
  }

  public Map<LifeSkillType, Integer> getLifeSkillRequirements() {
    return lifeSkillRequirements;
  }

  public void setLifeSkillRequirements(
      Map<LifeSkillType, Integer> lifeSkillRequirements) {
    this.lifeSkillRequirements = lifeSkillRequirements;
  }

  public Map<StrifeAttribute, Integer> getAttributeRequirement() {
    return attributeRequirement;
  }

  public void setAttributeRequirement(
      Map<StrifeAttribute, Integer> attributeRequirement) {
    this.attributeRequirement = attributeRequirement;
  }

  public boolean isRequirementMet(Champion champion) {
    if (champion.getPlayer().getLevel() < levelRequirement) {
      return false;
    }
    if (champion.getBonusLevels() < bonusLevelRequirement) {
      return false;
    }
    for (LifeSkillType type : lifeSkillRequirements.keySet()) {
      if (champion.getLifeSkillLevel(type) < lifeSkillRequirements.get(type)) {
        return false;
      }
    }
    for (StrifeAttribute attr : attributeRequirement.keySet()) {
      if (champion.getAttributeLevel(attr) < attributeRequirement.get(attr)) {
        return false;
      }
    }
    return true;
  }

  public static List<String> buildRequirementsLore(Champion champion, AbilityIconData data) {
    List<String> strings = new ArrayList<>();
    if (champion.getPlayer().getLevel() < data.levelRequirement) {
      strings.add("Requirement: Level " + data.levelRequirement);
    }
    if (champion.getBonusLevels() < data.bonusLevelRequirement) {
      strings.add("Requirement: Bonus Level " + data.bonusLevelRequirement);
    }
    for (LifeSkillType type : data.lifeSkillRequirements.keySet()) {
      if (champion.getLifeSkillLevel(type) < data.lifeSkillRequirements.get(type)) {
        strings.add("Requirement: " + type.name() + data.lifeSkillRequirements.get(type));
      }
    }
    for (StrifeAttribute attr : data.attributeRequirement.keySet()) {
      if (champion.getAttributeLevel(attr) < data.attributeRequirement.get(attr)) {
        strings.add("Requirement: " + attr.getName() + data.attributeRequirement.get(attr));
      }
    }
    return strings;
  }
}
