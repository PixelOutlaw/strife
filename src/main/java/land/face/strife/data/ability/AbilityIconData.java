package land.face.strife.data.ability;

import java.util.HashMap;
import java.util.Map;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.champion.StrifeAttribute;
import land.face.strife.stats.AbilitySlot;
import land.face.strife.util.PlayerDataUtil;
import org.bukkit.inventory.ItemStack;

public class AbilityIconData {

  private final ItemStack stack;
  private AbilitySlot abilitySlot;
  private int totalSkillRequirement = 0;
  private int levelRequirement = 0;
  private int bonusLevelRequirement = 0;
  private final Map<LifeSkillType, Integer> lifeSkillRequirements = new HashMap<>();
  private final Map<StrifeAttribute, Integer> attributeRequirement = new HashMap<>();
  private final Map<LifeSkillType, Float> expWeights = new HashMap<>();

  public AbilityIconData(ItemStack stack) {
    this.stack = stack;
  }

  public ItemStack getStack() {
    return stack;
  }

  public AbilitySlot getAbilitySlot() {
    return abilitySlot;
  }

  public void setAbilitySlot(AbilitySlot abilitySlot) {
    this.abilitySlot = abilitySlot;
  }

  public int getTotalSkillRequirement() {
    return totalSkillRequirement;
  }

  public void setTotalSkillRequirement(int totalSkillRequirement) {
    this.totalSkillRequirement = totalSkillRequirement;
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

  public Map<StrifeAttribute, Integer> getAttributeRequirement() {
    return attributeRequirement;
  }

  public Map<LifeSkillType, Float> getExpWeights() {
    return expWeights;
  }

  public boolean isRequirementMet(Champion champion) {
    if (PlayerDataUtil.getTotalSkillLevel(champion.getPlayer()) < totalSkillRequirement) {
      return false;
    }
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
}
