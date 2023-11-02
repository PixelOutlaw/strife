package land.face.strife.data;

import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.champion.StrifeAttribute;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.DamageUtil.DamageScale;
import land.face.strife.util.DamageUtil.DamageType;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BonusDamage {

  private DamageScale damageScale;
  private DamageType damageType;
  private StrifeStat damageStat = null;
  private StrifeAttribute attribute = null;
  private LifeSkillType lifeSkillType = null;
  private float amount;
  private boolean negateMinionDamage;

  public void setDamageScale(DamageScale damageScale) {
    this.damageScale = damageScale;
    this.negateMinionDamage = switch (damageScale) {
      case FLAT, CASTER_DAMAGE -> false;
      default -> true;
    };
  }
}
