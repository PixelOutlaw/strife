package info.faceland.strife.data.condition;

import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.AttributedEntity;

public class AttributeCondition implements Condition {

  private StrifeAttribute strifeAttribute;
  private Comparison comparison;
  private double value;

  public AttributeCondition(StrifeAttribute attribute, Comparison comparison, double value) {
    this.strifeAttribute = attribute;
    this.comparison = comparison;
    this.value = value;
  }

  public boolean isMet(AttributedEntity attacker, AttributedEntity target) {
    switch (comparison) {
      case GREATER_THAN:
        return attacker.getAttribute(strifeAttribute) > value;
      case LESS_THAN:
        return attacker.getAttribute(strifeAttribute) < value;
      case EQUAL:
        return attacker.getAttribute(strifeAttribute) == value;
    }
    return false;
  }
}
