package land.face.strife.data.effects;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.conditions.Condition;
import land.face.strife.stats.StrifeStat;

public abstract class Effect {

  private static StrifePlugin plugin;

  private String id;
  private boolean forceTargetCaster;
  private boolean friendly;

  private final Map<StrifeStat, Float> statMults = new HashMap<>();
  private final Set<Condition> conditions = new HashSet<>();

  public void apply(StrifeMob caster, StrifeMob target) {

  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public boolean isForceTargetCaster() {
    return forceTargetCaster;
  }

  public void setForceTargetCaster(boolean forceTargetCaster) {
    this.forceTargetCaster = forceTargetCaster;
  }

  public boolean isFriendly() {
    return friendly;
  }

  public void setFriendly(boolean friendly) {
    this.friendly = friendly;
  }

  public Map<StrifeStat, Float> getStatMults() {
    return statMults;
  }

  public void setStatMults(Map<StrifeStat, Float> statMults) {
    this.statMults.clear();
    this.statMults.putAll(statMults);
  }

  public void addCondition(Condition condition) {
    conditions.add(condition);
  }

  public Set<Condition> getConditions() {
    return conditions;
  }

  public static StrifePlugin getPlugin() {
    return plugin;
  }

  public static void setPlugin(StrifePlugin plugin) {
    Effect.plugin = plugin;
  }

  public enum EffectType {
    DAMAGE,
    WORLD_SPACE_ENTITY,
    CHASER,
    CONSOLE_COMMAND,
    COUNTER,
    AREA_EFFECT,
    ENDLESS_EFFECT,
    CANCEL_ENDLESS_EFFECT,
    REVIVE,
    HEAL,
    FOOD,
    COOLDOWN_REDUCTION,
    UNTOGGLE,
    RESTORE_BARRIER,
    RESTORE_ENERGY,
    INCREASE_RAGE,
    PROJECTILE,
    EQUIPMENT_SWAP,
    EVOKER_FANGS,
    FALLING_BLOCK,
    IGNITE,
    SILENCE,
    BLEED,
    TELEPORT,
    TELEPORT_BEHIND,
    TITLE,
    CORRUPT,
    ADD_EARTH_RUNES,
    CONSUME_BLEED,
    CONSUME_CORRUPT,
    BUFF_EFFECT,
    SET_FALL,
    WAIT,
    SOUND,
    PARTICLE,
    SPEAK,
    PUSH,
    LIGHTNING,
    MODIFY_PROJECTILE,
    POTION,
    TARGET,
    FORCE_STAT,
    SUMMON,
    CHARM,
    SWING,
    UNDISGUISE,
    STEALTH,
    REMOVE_ENTITY,
    MINION_CAST
  }
}
