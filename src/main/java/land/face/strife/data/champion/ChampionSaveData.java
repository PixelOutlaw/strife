package land.face.strife.data.champion;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import land.face.strife.data.LevelPath.Choice;
import land.face.strife.data.LevelPath.Path;
import land.face.strife.stats.AbilitySlot;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ChampionSaveData {

  @Getter
  private final UUID uniqueId;

  private final Map<StrifeAttribute, Integer> levelMap = new HashMap<>();
  @Getter
  private final Map<LifeSkillType, Integer> skillLevelMap = new HashMap<>();
  @Getter
  private final Map<LifeSkillType, Float> skillExpMap = new HashMap<>();
  private final Map<AbilitySlot, String> abilities = new HashMap<>();
  @Getter
  private final Map<AbilitySlot, List<String>> castMessages = new HashMap<>();
  @Getter
  private final Map<Path, Choice> pathMap = new HashMap<>();
  @Getter
  private final Set<String> boundAbilities = new HashSet<>();

  @Getter
  private final Map<StrifeAttribute, Integer> pendingStats = new HashMap<>();

  @Getter @Setter
  private SelectedGod selectedGod;
  @Getter
  private final Map<SelectedGod, Integer> godXp = new HashMap<>();
  @Getter
  private final Map<SelectedGod, Integer> godLevel = new HashMap<>();

  @Getter @Setter
  private int unusedStatPoints, pendingUnusedStatPoints, highestReachedLevel;

  @Getter @Setter
  private float pvpScore = 700;
  @Getter @Setter
  private float prayerPoints = 0;
  @Getter
  private double catchupExpUsed;

  @Getter @Setter
  private boolean onMount, glowEnabled, displayExp;

  public ChampionSaveData(UUID uniqueId) {
    this.uniqueId = uniqueId;
  }

  public void setAbility(AbilitySlot abilitySlot, String ability) {
    abilities.put(abilitySlot, ability);
  }

  public String getAbility(AbilitySlot abilitySlot) {
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

  public void setCatchupExpUsed(double catchupExpUsed) {
    this.catchupExpUsed = Math.max(catchupExpUsed, -750000000);
  }

  public Player getPlayer() {
    return Bukkit.getPlayer(uniqueId);
  }

  public void setSkillLevel(LifeSkillType type, int level) {
    skillLevelMap.put(type, level);
  }

  public void setGodXp(SelectedGod selectedGod, int amount) {
    godXp.put(selectedGod, amount);
  }

  public void setGodLevel(SelectedGod selectedGod, int amount) {
    godLevel.put(selectedGod, amount);
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
