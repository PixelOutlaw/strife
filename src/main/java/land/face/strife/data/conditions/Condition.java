package land.face.strife.data.conditions;

import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import java.util.HashMap;
import java.util.Map;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

@Getter @Setter
public abstract class Condition {

  private CompareTarget compareTarget;
  private Comparison comparison;
  private ConditionType type;
  private ConditionUser conditionUser;
  private float value;
  private boolean checkMaster = false;
  private boolean inverted;
  private String failMessage;
  private boolean playFailMessage;

  private final Map<StrifeStat, Float> statMults = new HashMap<>();

  public boolean isMet(StrifeMob attacker, StrifeMob target) {
    return true;
  }

  public void sendMessage(StrifeMob caster) {
    if (playFailMessage && caster.getEntity() instanceof Player) {
      PaletteUtil.sendMessage(caster.getEntity(), failMessage);
    }
  }

  public CompareTarget getCompareTarget() {
    return compareTarget;
  }

  public StrifeMob getEntity(StrifeMob caster, StrifeMob target) {
    if (compareTarget == CompareTarget.SELF) {
      return checkMaster ? caster.getMaster() : caster;
    } else if (compareTarget == CompareTarget.OTHER) {
      return checkMaster ? target.getMaster() : target;
    } else {
      return null;
    }
  }

  public Map<StrifeStat, Float> getStatMults() {
    return statMults;
  }

  public void setStatMults(Map<StrifeStat, Float> statMults) {
    this.statMults.clear();
    this.statMults.putAll(statMults);
  }

  public enum Comparison {
    GREATER_THAN,
    LESS_THAN,
    EQUAL,
    NONE
  }

  public enum CompareTarget {
    SELF,
    OTHER
  }

  public enum ConditionUser {
    PLAYER,
    MOB,
    ANY
  }

  public enum ConditionType {
    ATTRIBUTE,
    WEAPONS,
    BUFF,
    LORE,
    ENDLESS_EFFECT,
    BLOCKING,
    MOVING,
    MINION,
    HAS_MINIONS,
    NEARBY_ENTITIES,
    UNION,
    IN_COMBAT,
    CHANCE,
    COMPARE,
    STAT,
    LIGHT_LEVEL,
    HEALTH,
    BARRIER,
    FROST,
    RAGE,
    ENERGY,
    POTION_EFFECT,
    ORDER_ENTITY_READY,
    ORDER_ENTITY_EXISTS,
    TIME,
    LEVEL,
    ITS_OVER_ANAKIN,
    ENTITY_TYPE,
    UNIQUE_ID,
    FACTION_MEMBER,
    FLYING,
    VELOCITY,
    GROUNDED,
    WATER,
    BLEEDING,
    DARKNESS,
    RANGE,
    SIZE,
    SNEAK,
    STEALTHED,
    BURNING,
    EARTH_RUNES,
    ALIVE,
    AIR_JUMPS
  }
}
