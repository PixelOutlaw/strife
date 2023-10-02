package land.face.strife.data;

import java.util.*;

import land.face.strife.data.ability.EntityAbilitySet;
import land.face.strife.data.effects.StrifeParticle;
import land.face.strife.stats.StrifeStat;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.DyeColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager.Profession;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

@Getter
public class UniqueEntity {

  @Setter String id;
  @Setter EntityType type;
  @Setter String name;
  @Setter int bonusExperience;
  @Setter float experienceMultiplier;
  @Setter float minLevelClampMult;
  @Setter Map<StrifeStat, Float> attributeMap;
  final Set<String> factions = new HashSet<>();
  @Setter EntityAbilitySet abilitySet;
  @Setter int baseLevel;
  @Setter boolean showName;
  @Setter boolean baby;
  @Setter boolean angry;
  @Setter boolean zombificationImmune;
  @Setter boolean armsRaised;
  @Setter boolean hasAI;
  @Setter boolean gravity;
  @Setter boolean collidable;
  @Setter boolean guildMob;
  @Setter Profession profession;
  @Setter boolean invisible;
  @Setter boolean silent;
  @Setter int size;
  @Setter int followRange = -1;
  @Setter DyeColor color;
  @Setter boolean pushImmune;
  @Setter boolean charmImmune;
  @Setter boolean burnImmune;
  @Setter boolean fallImmune;
  @Setter boolean knockbackImmune;
  @Setter boolean ignoreSneak;
  @Setter boolean saddled;
  @Setter boolean canTarget;
  @Setter int maxMods;
  @Setter boolean removeFollowMods;
  @Setter boolean powered;
  @Setter boolean attackDisabledOnGlobalCooldown;
  @Setter boolean alwaysRunTimer;
  @Setter double displaceMultiplier;
  @Setter double boundingBonus;
  @Setter String mount;
  @Setter Map<EquipmentSlot, String> equipment = new HashMap<>();
  @Setter ItemStack itemPassenger = null;
  @Setter StrifeParticle strifeParticle;
  @Setter boolean customAi;
  @Setter boolean aggressiveAi;
  @Setter List<String> removeGoals;
  @Setter List<String> addGoals;
  final List<String> bonusKnowledge = new ArrayList<>();
  @Setter String modelId;
  @Setter boolean vagabondAllowed;

}
