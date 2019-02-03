package info.faceland.strife.managers;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.strife.data.Ability;
import info.faceland.strife.data.LoreAbility;
import info.faceland.strife.util.LogUtil;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;

public class LoreAbilityManager {

  private final Map<String, LoreAbility> loreAbilityMap;
  private final AbilityManager abilityManager;

  public LoreAbilityManager(AbilityManager abilityManager) {
    this.loreAbilityMap = new HashMap<>();
    this.abilityManager = abilityManager;
  }

  public void loadLoreAbility(String key, ConfigurationSection cs) {
    String type = cs.getString("trigger-type", "NULL").toUpperCase();

    TriggerType triggerType;
    try {
      triggerType = TriggerType.valueOf(type);
    } catch (Exception e) {
      LogUtil.printError("Failed to load " + key + ". Invalid trigger type (" + type + ")");
      return;
    }

    String triggerText = cs.getString("trigger-text", null);
    if (StringUtils.isBlank(triggerText)) {
      LogUtil.printError("Failed to load " + key + ". Must provide trigger-text!");
      return;
    }

    Ability ability = abilityManager.getAbility(cs.getString("ability-name", null));
    if (ability == null) {
      LogUtil.printError("Failed to load " + key + ". No valid ability defined!");
      return;
    }
    loreAbilityMap.put(triggerText, new LoreAbility(triggerType, ability));
    LogUtil.printInfo("Loaded ability " + key + " successfully.");
  }

  public LoreAbility getLoreAbilityFromString(String loreString) {
    return loreAbilityMap.get(loreString);
  }

  public Map<String, LoreAbility> getLoreAbilityMap() {
    return loreAbilityMap;
  }

  public enum TriggerType {
    ON_HIT,
    ON_KILL,
    WHEN_HIT,
    ON_CRIT,
    ON_BLOCK,
    ON_EVADE,
    ON_SNEAK
  }
}
