package info.faceland.strife.conditions;

import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.util.PlayerDataUtil;

public class AttributeCondition implements Condition {

  private final StrifeAttribute strifeAttribute;
  private final CompareTarget compareTarget;
  private final Comparison comparison;
  private final double value;

  public AttributeCondition(StrifeAttribute attribute, CompareTarget compareTarget,
      Comparison comparison, double value) {
    this.strifeAttribute = attribute;
    this.compareTarget = compareTarget;
    this.comparison = comparison;
    this.value = value;
  }

  public boolean isMet(AttributedEntity attacker, AttributedEntity target) {
    long attributeValue = compareTarget == CompareTarget.SELF ?
        Math.round(attacker.getAttribute(strifeAttribute)) :
        Math.round(target.getAttribute(strifeAttribute));
    return PlayerDataUtil.conditionCompare(comparison, (int) attributeValue, value);
  }
}
