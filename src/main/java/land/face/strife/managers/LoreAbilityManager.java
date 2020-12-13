package land.face.strife.managers;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import io.pixeloutlaw.minecraft.spigot.garbage.ListExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import land.face.strife.data.LoreAbility;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.TargetResponse;
import land.face.strife.data.ability.Ability;
import land.face.strife.data.effects.Effect;
import land.face.strife.util.ItemUtil;
import land.face.strife.util.LogUtil;
import land.face.strife.util.PlayerDataUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

public class LoreAbilityManager {

  private final Map<String, LoreAbility> loreStringToAbilityMap;
  private final Map<String, LoreAbility> loreIdToAbilityMap;
  private final AbilityManager abilityManager;
  private final EffectManager effectManager;

  public final static TriggerType[] triggerTypes = TriggerType.values();

  public LoreAbilityManager(AbilityManager abilityManager, EffectManager effectManager) {
    this.loreStringToAbilityMap = new HashMap<>();
    this.loreIdToAbilityMap = new HashMap<>();
    this.abilityManager = abilityManager;
    this.effectManager = effectManager;
  }

  Set<LoreAbility> getAbilities(ItemStack stack) {
    List<String> lore = ItemUtil.getLore(stack);
    Set<LoreAbility> abilities = new HashSet<>();
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

  public Set<String> getLoreAbilityIds() {
    return loreIdToAbilityMap.keySet();
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

    String triggerText = StringExtensionsKt.chatColorize(cs.getString("trigger-text", ""));
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
    LogUtil.printDebug("Getting effects for LoreAbility " + key);
    for (String s : stringEffects) {
      Effect effect = effectManager.getEffect(s);
      if (effect != null && !effectList.contains(effect)) {
        effectList.add(effect);
        LogUtil.printDebug("+-Added effect '" + s + "'");
      }
    }
    LogUtil.printDebug("Done!");
    if (triggerType != TriggerType.NONE && ability == null && effectList.isEmpty()) {
      LogUtil.printError("Failed to load lore-ability " + key + ". No valid ability/effects!");
      return;
    }
    List<String> description = ListExtensionsKt.chatColorize(cs.getStringList("description"));
    LoreAbility loreAbility = new LoreAbility(key, triggerType, triggerText, ability, description);
    for (Effect e : effectList) {
      loreAbility.addEffect(e);
    }
    loreStringToAbilityMap.put(triggerText, loreAbility);
    loreIdToAbilityMap.put(key, loreAbility);
    LogUtil.printDebug("Loaded lore ability " + key + " successfully.");
  }

  public void applyLoreAbility(LoreAbility la, StrifeMob caster, LivingEntity target) {
    LogUtil.printDebug(PlayerDataUtil.getName(caster.getEntity()) + " is casting: " + la.getId());
    if (la.getAbility() != null) {
      abilityManager.execute(la.getAbility(), caster, target);
    }
    Set<LivingEntity> entities = new HashSet<>();
    entities.add(target);

    TargetResponse response = new TargetResponse(entities);

    for (Effect effect : la.getEffects()) {
      effectManager.execute(effect, caster, response);
    }
  }

  private LoreAbility getLoreAbilityFromString(String loreString) {
    return loreStringToAbilityMap.getOrDefault(loreString, null);
  }
  public LoreAbility getLoreAbilityFromId(String id) {
    return loreIdToAbilityMap.getOrDefault(id, null);
  }

  public enum TriggerType {
    ON_HIT,
    ON_KILL,
    WHEN_HIT,
    ON_CRIT,
    ON_BLOCK,
    ON_EVADE,
    ON_SNEAK_ATTACK,
    ON_FALL,
    ON_CAST,
    ON_COMBAT_START,
    ON_COMBAT_END,
    ON_AIR_JUMP,
    TIMER,
    NONE
  }
}
