package land.face.strife.data.champion;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import land.face.strife.data.LevelPath.Choice;
import land.face.strife.data.LevelPath.Path;
import land.face.strife.data.LoreAbility;
import land.face.strife.data.ability.Ability;
import land.face.strife.stats.AbilitySlot;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Data
public class ChampionSaveData {

  private final UUID uniqueId;
  private final Map<StrifeAttribute, Integer> levelMap = new HashMap<>();
  private final Map<LifeSkillType, Integer> skillLevelMap = new HashMap<>();
  private final Map<LifeSkillType, Float> skillExpMap = new HashMap<>();
  private final Map<AbilitySlot, Ability> abilities = new HashMap<>();
  private final Map<AbilitySlot, List<String>> castMessages = new HashMap<>();
  private final Map<Path, Choice> pathMap = new HashMap<>();
  private final Set<LoreAbility> boundAbilities = new HashSet<>();

  private final Map<StrifeAttribute, Integer> pendingStats = new HashMap<>();

  private SelectedGod selectedGod;
  private Map<SelectedGod, Integer> godXp = new HashMap<>();

  private int unusedStatPoints;
  private int pendingUnusedStatPoints;
  private int highestReachedLevel;
  private int bonusLevels;

  private float pvpScore = 700;

  private boolean onMount;
  private boolean glowEnabled;
  private boolean displayExp;

  public ChampionSaveData(UUID uniqueId) {
    this.uniqueId = uniqueId;
  }

  public void setAbility(AbilitySlot abilitySlot, Ability ability) {
    abilities.put(abilitySlot, ability);
  }

  public Ability getAbility(AbilitySlot abilitySlot) {
    return abilities.get(abilitySlot);
  }

  public void setLevel(StrifeAttribute stat, int level) {
    levelMap.put(stat, level);
  }

  public Map<StrifeAttribute, Integer> getLevelMap() {
    return new HashMap<>(levelMap);
  }

  public void resetPendingStats() {
    pendingStats.clear();
    pendingStats.putAll(levelMap);
    pendingUnusedStatPoints = unusedStatPoints;
  }

  public void savePendingStats() {
    unusedStatPoints = pendingUnusedStatPoints;
    levelMap.clear();
    levelMap.putAll(pendingStats);
  }

  public Player getPlayer() {
    return Bukkit.getPlayer(getUniqueId());
  }

  public void setSkillLevel(LifeSkillType type, int level) {
    skillLevelMap.put(type, level);
  }

  public void setGodXp(SelectedGod selectedGod, int amount) {
    godXp.put(selectedGod, amount);
  }

  public void setSkillExp(LifeSkillType type, float amount) {
    skillExpMap.put(type, amount);
  }

  public int getSkillLevel(LifeSkillType type) {
    return skillLevelMap.getOrDefault(type, 1);
  }

  public float getSkillExp(LifeSkillType type) {
    return skillExpMap.getOrDefault(type, 0f);
  }

  public enum SelectedGod {
    FACEGUY,
    AURORA,
    ZEXIR,
    ANYA,
    NONE
  }
}
