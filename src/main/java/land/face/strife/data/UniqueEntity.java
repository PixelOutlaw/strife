package land.face.strife.data;

import java.util.*;

import land.face.strife.data.ability.EntityAbilitySet;
import land.face.strife.data.effects.StrifeParticle;
import land.face.strife.stats.StrifeStat;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.DyeColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.inventory.EquipmentSlot;

@Getter
@Setter
public class UniqueEntity {

  private String id;
  private EntityType type;
  private String name;
  private int bonusExperience;
  private float experienceMultiplier;
  private float minLevelClampMult;
  private Map<StrifeStat, Float> attributeMap;
  private final Set<String> factions = new HashSet<>();
  private final Set<String> enemyUniques = new HashSet<>();
  private EntityAbilitySet abilitySet;
  private boolean showName, baby, angry, armsRaised, hasAI, gravity, collidable, ignoreTargetLevel,guildMob, pushImmune,
      elite, burnImmune, fallImmune, knockbackImmune, ignoreSneak, saddled, canTarget, removeFollowMods, powered,
      attackDisabledOnGlobalCooldown, alwaysRunTimer, vagabondAllowed, aggressiveAi, customAi, invisible, invulnerable,
      silent;
  private int baseLevel;
  private Profession profession;
  private int size;
  private int followRange = -1;
  private DyeColor color;
  private int maxMods;
  private double displaceMultiplier;
  private double boundingBonus;
  private Map<EquipmentSlot, String> equipment = new HashMap<>();
  private StrifeParticle strifeParticle;
  private List<String> removeGoals;
  private List<String> addGoals;
  private final List<String> bonusKnowledge = new ArrayList<>();
  private String modelId;

}
