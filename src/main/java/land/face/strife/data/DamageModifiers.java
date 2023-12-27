package land.face.strife.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import land.face.strife.util.DamageUtil;
import land.face.strife.util.DamageUtil.AbilityMod;
import land.face.strife.util.DamageUtil.AttackType;
import land.face.strife.util.DamageUtil.DamageType;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DamageModifiers {

  private static final Map<DamageType, Float> baseDamageMults = buildBaseDamageMults();

  private AttackType attackType = AttackType.OTHER;
  private float attackMultiplier = 1f;
  private float healMultiplier = 1f;
  private float damageReductionRatio = 1f;
  private final Map<DamageType, Float> damageMultipliers = new HashMap<>(baseDamageMults);
  private final List<BonusDamage> bonusDamages = new ArrayList<>();
  private final Map<AbilityMod, Float> abilityMods = new HashMap<>();
  private final Set<ElementalStatus> elementalStatuses = new HashSet<>();
  private boolean isBasicAttack = true;
  private boolean isSneakAttack = false;
  private boolean isBlocking = false;
  private boolean canBeBlocked = true;
  private boolean canBeEvaded = true;
  private boolean applyOnHitEffects = true;
  private boolean scaleChancesWithAttack = false;
  private boolean showPopoffs = true;
  private boolean useBasicDamageMult = true;
  private boolean applyMinionDamageMult = false;
  private boolean bypassBarrier = false;
  private boolean guardBreak = false;

  private static Map<DamageType, Float> buildBaseDamageMults() {
    Map<DamageType, Float> base = new HashMap<>();
    for (DamageType type : DamageUtil.DMG_TYPES) {
      base.put(type, 1f);
    }
    return base;
  }

  public enum ElementalStatus {
    IGNITE,
    FREEZE,
    SHOCK,
    CORRUPT,
    CRUNCH
  }

}
