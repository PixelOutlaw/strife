package info.faceland.strife.managers;

import static org.bukkit.Bukkit.getLogger;

import com.tealcube.minecraft.bukkit.TextUtils;
import info.faceland.strife.data.Ability;
import info.faceland.strife.data.Ability.TargetType;
import info.faceland.strife.effects.Effect;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;

public class AbilityManager {

  private final EffectManager effectManager;
  private final Map<String, Ability> loadedAbilities;

  public AbilityManager(EffectManager effectManager) {
    this.effectManager = effectManager;
    this.loadedAbilities = new HashMap<>();
  }

  public Ability getAbility(String name) {
    if (loadedAbilities.containsKey(name)) {
      return loadedAbilities.get(name);
    }
    getLogger().warning("Attempted to get unknown ability '" + name + "'.");
    return null;
  }

  public void loadAbility(String key, ConfigurationSection cs) {
    String name = TextUtils.color(cs.getString("name", "ABILITY NOT NAMED"));
    TargetType targetType;
    try {
      targetType = TargetType.valueOf(cs.getString("target-type"));
    } catch (Exception e) {
      getLogger().warning("Skipping load of ability " + key + " - Invalid target type.");
      return;
    }
    int cooldown = cs.getInt("cooldown", 10);
    int range = cs.getInt("range", 4);
    List<String> effectStrings = cs.getStringList("effects");
    if (effectStrings.isEmpty()) {
      getLogger().warning("Skipping ability " + key + " - No effects.");
      return;
    }
    List<Effect> effects = new ArrayList<>();
    for (String s : effectStrings) {
      Effect effect = effectManager.getEffect(s);
      if (effect == null) {
        getLogger().warning("Ability " + key + " tried to add unknown effect" + s + ".");
        continue;
      }
      effects.add(effect);
    }
    loadedAbilities.put(key, new Ability(name, effects, targetType, range, cooldown));
    getLogger().info("Loaded ability " + key + " successfully.");
  }
}
