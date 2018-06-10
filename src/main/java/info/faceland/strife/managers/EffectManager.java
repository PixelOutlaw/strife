package info.faceland.strife.managers;

import static org.bukkit.Bukkit.getLogger;

import com.tealcube.minecraft.bukkit.TextUtils;
import info.faceland.strife.effects.Effect;
import info.faceland.strife.effects.Ignite;
import info.faceland.strife.effects.Speak;
import info.faceland.strife.effects.Wait;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;

public class EffectManager {

  private final Map<String, Effect> loadedEffects;

  public EffectManager() {
    this.loadedEffects = new HashMap<>();
  }

  public void loadEffect(String key, ConfigurationSection cs) {
    String type = cs.getString("type", "NULL").toUpperCase();
    EffectType effectType;
    try {
      effectType = EffectType.valueOf(type);
    } catch (Exception e) {
      getLogger().severe("Skipping effect " + key + " for invalid effect type");
      return;
    }
    Effect effect = null;
    switch (effectType) {
      case IGNITE:
        effect = new Ignite();
        ((Ignite) effect).setDuration(cs.getInt("duration", 20));
        break;
      case WAIT:
        effect = new Wait();
        ((Wait) effect).setTickDelay(cs.getInt("duration", 20));
        break;
      case SPEAK:
        effect = new Speak();
        ((Speak) effect).setMessage(
            TextUtils.color(cs.getString("message", "set a message for this speech")));
        break;
    }
    if (effect == null) {
      getLogger().severe("Null effect for " + key + "! Skipping...");
      return;
    }
    if (effectType != EffectType.WAIT) {
      effect.setName(TextUtils.color(cs.getString("name", "&8Unnamed Effect")));
      effect.setRange(cs.getInt("range", 5));
      effect.setFriendly(cs.getBoolean("friendly", false));
      effect.setSelfHarm(cs.getBoolean("self-harm", false));
    } else {
      effect.setName("wait");
    }
    loadedEffects.put(key, effect);
    getLogger().info("Loaded effect " + key + " successfully.");
  }

  public Effect getEffect(String key) {
    if (loadedEffects.containsKey(key)) {
      return loadedEffects.get(key);
    }
    getLogger().warning("Attempted to get unknown effect '" + key + "'.");
    return null;
  }

  public enum EffectType {
    PHYSICAL_DAMAGE,
    MAGIC_DAMAGE,
    IGNITE,
    WAIT,
    SPEAK
  }
}
