package info.faceland.strife.managers;

import com.tealcube.minecraft.bukkit.TextUtils;
import info.faceland.strife.effects.Effect;
import info.faceland.strife.effects.Ignite;
import info.faceland.strife.effects.Knockback;
import info.faceland.strife.effects.PotionEffectAction;
import info.faceland.strife.effects.Speak;
import info.faceland.strife.effects.Summon;
import info.faceland.strife.effects.Wait;
import info.faceland.strife.util.LogUtil;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffectType;

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
      LogUtil.printError("Skipping effect " + key + " for invalid effect type");
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
        ((Speak) effect).setMessages(
            TextUtils.color(cs.getStringList("messages")));
        break;
      case KNOCKBACK:
        effect = new Knockback();
        ((Knockback) effect).setPower(cs.getDouble("power", 1));
        break;
      case SUMMON:
        effect = new Summon();
        ((Summon) effect).setAmount(cs.getInt("amount", 1));
        ((Summon) effect).setUniqueEntity(cs.getString("unique-entity"));
        break;
      case POTION:
        effect = new PotionEffectAction();
        PotionEffectType potionType;
        try {
          potionType = PotionEffectType.getByName(cs.getString("effect"));
        } catch (Exception e) {
          LogUtil.printWarning("Invalid potion effect type in effect " + key + ". Skipping.");
          return;
        }
        ((PotionEffectAction) effect).setPotionEffectType(potionType);
        ((PotionEffectAction) effect).setIntensity(cs.getInt("intensity", 0));
        ((PotionEffectAction) effect).setDuration(cs.getInt("duration", 0));
        break;
    }
    if (effect == null) {
      LogUtil.printError("Null effect for " + key + "! Skipping...");
      return;
    }
    if (effectType != EffectType.WAIT) {
      effect.setName(TextUtils.color(cs.getString("name", "&8Unnamed Effect")));
      effect.setRange(cs.getInt("range", 0));
      effect.setSelfAffect(cs.getBoolean("self-affect", false));
      effect.setFriendly(cs.getBoolean("friendly", false));
    } else {
      effect.setName("wait");
    }
    loadedEffects.put(key, effect);
    LogUtil.printInfo("Loaded effect " + key + " successfully.");
  }

  public Effect getEffect(String key) {
    if (loadedEffects.containsKey(key)) {
      LogUtil.printDebug("Attempting to load effect " + key);
      return loadedEffects.get(key);
    }
    LogUtil.printWarning("Attempted to get unknown effect '" + key + "'.");
    return null;
  }

  public Map<String, Effect> getLoadedEffects() {
    return loadedEffects;
  }

  public enum EffectType {
    PHYSICAL_DAMAGE,
    MAGIC_DAMAGE,
    IGNITE,
    WAIT,
    SPEAK,
    KNOCKBACK,
    POTION,
    SUMMON
  }
}
