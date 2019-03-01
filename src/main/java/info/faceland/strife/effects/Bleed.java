package info.faceland.strife.effects;

import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.util.DamageUtil;

public class Bleed extends Effect {

  private double amount;
  private boolean ignoreArmor;

  @Override
  public void apply(AttributedEntity caster, AttributedEntity target) {
    double bleedAmount = amount;
    for (StrifeAttribute attr : statMults.keySet()) {
      bleedAmount += statMults.get(attr) * caster.getAttributes().getOrDefault(attr, 0D);
    }
    // TODO: Add logic for ignore armor false
    DamageUtil.applyBleed(target.getEntity(), bleedAmount);
  }

  public void setAmount(double amount) {
    this.amount = amount;
  }

  public void setIgnoreArmor(boolean ignoreArmor) {
    this.ignoreArmor = ignoreArmor;
  }
}