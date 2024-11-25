package land.face.strife.data.ability;

import java.util.HashMap;
import java.util.Map;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.champion.StrifeAttribute;
import land.face.strife.stats.AbilitySlot;
import land.face.strife.util.PlayerDataUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
public class AbilityIconData {

  private final ItemStack fullStack;
  private final ItemStack simpleStack;

  private AbilitySlot abilitySlot;
  private int totalSkillRequirement = 0;
  private int levelRequirement = 0;
  private final Map<LifeSkillType, Integer> lifeSkillRequirements = new HashMap<>();
  private final Map<StrifeAttribute, Integer> attributeRequirement = new HashMap<>();
  private final Map<LifeSkillType, Float> expWeights = new HashMap<>();

  public AbilityIconData(ItemStack fullStack, ItemStack simpleStack) {
    this.fullStack = fullStack;
    this.simpleStack = simpleStack;
  }

  public boolean isRequirementMet(Champion champion) {
    if (PlayerDataUtil.getTotalSkillLevel(champion.getPlayer()) < totalSkillRequirement) {
      return false;
    }
    if (champion.getPlayer().getLevel() < levelRequirement) {
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
