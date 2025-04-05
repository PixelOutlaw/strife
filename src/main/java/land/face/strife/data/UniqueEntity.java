package land.face.strife.data;

import java.util.*;

import land.face.strife.data.ability.EntityAbilitySet;
import land.face.strife.data.effects.StrifeParticle;
import land.face.strife.stats.StrifeStat;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import org.bukkit.DyeColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.inventory.EquipmentSlot;

@Getter
@Setter
@EqualsAndHashCode
public class UniqueEntity {

  private String id;

  private EntityType type;
  private String name;
  private float experienceMultiplier;
  private float minLevelClampMult;
  private Map<StrifeStat, Float> attributeMap;
  @EqualsAndHashCode.Exclude
  private final Set<String> factions = new HashSet<>();
  @EqualsAndHashCode.Exclude
  private final Set<String> enemyUniques = new HashSet<>();
  @EqualsAndHashCode.Exclude
  private EntityAbilitySet abilitySet;
  private boolean showName, baby, angry, armsRaised, hasAI, gravity, collidable, ignoreTargetLevel,guildMob, pushImmune,
      elite, burnImmune, fallImmune, knockbackImmune, ignoreSneak, saddled, canTarget, removeFollowMods, powered,
      attackDisabledOnGlobalCooldown, alwaysRunTimer, vagabondAllowed, aggressiveAi, customAi, invisible, invulnerable,
      silent;
  private float customAttackRange;
  private int baseLevel, size, followRange = -1, maxMods, bonusExperience;
  private Profession profession;
  private DyeColor color;
  private double displaceMultiplier;
  private double boundingBonus;
  private Map<EquipmentSlot, String> equipment = new HashMap<>();
  @EqualsAndHashCode.Exclude
  private StrifeParticle strifeParticle;
  private List<String> removeGoals;
  private List<String> addGoals;
  private final List<String> bonusKnowledge = new ArrayList<>();
  private String modelId;
  private List<Double> modelSizes;

}
