package info.faceland.strife.effects;

import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.util.DamageUtil;

public class Corrupt extends Effect {

  private double amount;

  @Override
  public void apply(AttributedEntity caster, AttributedEntity target) {
    double corruptionStacks = amount;
    for (StrifeAttribute attr : getStatMults().keySet()) {
      corruptionStacks += getStatMults().get(attr) * caster.getAttributes().getOrDefault(attr, 0D);
    }
    DamageUtil.applyCorrupt(target.getEntity(), corruptionStacks);
  }

  public void setAmount(double amount) {
    this.amount = amount;
  }
}