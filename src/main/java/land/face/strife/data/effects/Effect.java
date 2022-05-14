package land.face.strife.data.effects;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.LifeSkillType;
import land.face.strife.data.champion.StrifeAttribute;
import land.face.strife.data.conditions.Condition;
import land.face.strife.stats.StrifeStat;

public abstract class Effect {

  private static StrifePlugin plugin;

  private String id;
  private boolean forceTargetCaster;
  private boolean friendly;

  private final Map<StrifeStat, Float> statMults = new HashMap<>();
  private final Map<LifeSkillType, Float> skillMults = new HashMap<>();
  private final Map<StrifeAttribute, Float> attributeMults = new HashMap<>();
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

  public void setStatMults(Map<StrifeStat, Float> statMults) {
    this.statMults.clear();
    this.statMults.putAll(statMults);
  }

  public void setSkillMults(Map<LifeSkillType, Float> skillMults) {
    this.skillMults.clear();
    this.skillMults.putAll(skillMults);
  }

  public void setAttributeMults(Map<StrifeAttribute, Float> attributeMults) {
    this.attributeMults.clear();
    this.attributeMults.putAll(attributeMults);
  }

  public float applyMultipliers(StrifeMob caster, float amount) {
    float multiplier = 1f;
    for (StrifeStat attr : statMults.keySet()) {
      multiplier += statMults.get(attr) * caster.getStat(attr);
    }
    if (caster.getChampion() != null) {
      Champion champion = caster.getChampion();
      for (LifeSkillType attr : skillMults.keySet()) {
        multiplier += skillMults.get(attr) * champion.getLifeSkillLevel(attr);
      }
      for (StrifeAttribute attr : attributeMults.keySet()) {
        multiplier += attributeMults.get(attr) * (float) champion.getAttributeLevel(attr);
      }
    }
    return amount * multiplier;
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
    EFFECT_LIB_PARTICLE,
    REVIVE,
    HEAL,
    COOLDOWN_REDUCTION,
    UNTOGGLE,
    RESTORE_BARRIER,
    RESTORE_ENERGY,
    INCREASE_RAGE,
    PROJECTILE,
    RIPTIDE,
    EVENT,
    EQUIPMENT_SWAP,
    EVOKER_FANGS,
    FALLING_BLOCK,
    IGNITE,
    FROST,
    INVINCIBLE,
    SILENCE,
    BLEED,
    TELEPORT,
    TELEPORT_BEHIND,
    THRALL,
    TITLE,
    CORRUPT,
    ADD_EARTH_RUNES,
    CONSUME_BLEED,
    CONSUME_CORRUPT,
    BUFF_EFFECT,
    SET_FALL,
    WAIT,
    SOUND,
    FIREWORK,
    PARTICLE,
    SPAWN_ITEM,
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
    DISGUISE,
    UNDISGUISE,
    MODEL_ANIMATION,
    CREATE_MODEL,
    CHANGE_PART,
    STEALTH,
    CHANGE_SIZE,
    ZERO_VELOCITY,
    STINGER,
    REMOVE_ENTITY,
    MINION_CAST,
    REMOVE_BUFF
  }
}
