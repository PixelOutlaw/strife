package info.faceland.strife.managers;

import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import info.faceland.strife.data.Ability;
import info.faceland.strife.data.AttributedEntity;
import info.faceland.strife.data.LoreAbility;
import info.faceland.strife.effects.Effect;
import info.faceland.strife.util.LogUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class LoreAbilityManager {

  private final Map<String, LoreAbility> loreStringToAbilityMap;
  private final Map<String, LoreAbility> loreIdToAbilityMap;
  private final AbilityManager abilityManager;
  private final EffectManager effectManager;

  public LoreAbilityManager(AbilityManager abilityManager, EffectManager effectManager) {
    this.loreStringToAbilityMap = new HashMap<>();
    this.loreIdToAbilityMap = new HashMap<>();
    this.abilityManager = abilityManager;
    this.effectManager = effectManager;
  }

  public List<LoreAbility> getLoreAbilitiesFromItem(ItemStack stack) {
    if (stack == null || stack.getType() == Material.AIR) {
      return new ArrayList<>();
    }
    List<LoreAbility> abilities = new ArrayList<>();
    if (stack.getItemMeta() == null || stack.getItemMeta().getLore() == null) {
      return abilities;
    }
    List<String> lore = stack.getItemMeta().getLore();
    if (lore.isEmpty()) {
      return abilities;
    }
    for (String s : lore) {
      LoreAbility loreAbility = getLoreAbilityFromString(s);
      if (loreAbility != null) {
        LogUtil.printDebug("Added LA string: " + s);
        abilities.add(loreAbility);
      }
    }
    return abilities;
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

    String triggerText = TextUtils.color(cs.getString("trigger-text", ""));
    if (StringUtils.isBlank(triggerText)) {
      LogUtil.printError("Failed to load " + key + ". Must provide trigger-text!");
      return;
    }

    String abilityString = cs.getString("ability-name", null);
    Ability ability = null;
    if (!StringUtils.isBlank(abilityString)) {
      ability = abilityManager.getAbility(abilityString);
    }
    List<String> stringEffects = cs.getStringList("effects");
    List<Effect> effectList = new ArrayList<>();
    for (String s : stringEffects) {
      Effect effect = effectManager.getEffect(s);
      if (effect != null && !effectList.contains(effect)) {
        effectList.add(effect);
      }
    }
    if (ability == null && effectList.isEmpty()) {
      LogUtil.printError("Failed to load " + key + ". No valid ability and no effects defined!");
      return;
    }
    List<String> description = TextUtils.color(cs.getStringList("description"));
    boolean singleTarget = cs.getBoolean("single-target", true);
    LoreAbility loreAbility = new LoreAbility(key, triggerType, triggerText, ability, singleTarget,
        description);
    for (Effect e : effectList) {
      loreAbility.addEffect(e);
    }
    loreStringToAbilityMap.put(triggerText, loreAbility);
    loreIdToAbilityMap.put(key, loreAbility);
    LogUtil.printInfo("Loaded lore ability " + key + " successfully.");
  }

  public void applyLoreAbility(LoreAbility la, AttributedEntity caster, AttributedEntity target) {
    if (la.getAbility() != null) {
      abilityManager.execute(la.getAbility(), caster, target);
    }
    for (Effect effect : la.getEffects()) {
      effect.execute(caster, target);
    }
  }

  public LoreAbility getLoreAbilityFromString(String loreString) {
    return loreStringToAbilityMap.getOrDefault(loreString, null);
  }
  public LoreAbility getLoreAbilityFromId(String id) {
    return loreIdToAbilityMap.getOrDefault(id, null);
  }

  public Map<String, LoreAbility> getLoreStringToAbilityMap() {
    return loreStringToAbilityMap;
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
