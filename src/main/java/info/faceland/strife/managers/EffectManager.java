package info.faceland.strife.managers;

import com.tealcube.minecraft.bukkit.TextUtils;
import info.faceland.strife.data.effects.Bleed;
import info.faceland.strife.data.effects.DealDamage;
import info.faceland.strife.data.effects.DealDamage.DamageScale;
import info.faceland.strife.data.effects.Effect;
import info.faceland.strife.data.effects.Heal;
import info.faceland.strife.data.effects.Ignite;
import info.faceland.strife.data.effects.Knockback;
import info.faceland.strife.data.effects.Leap;
import info.faceland.strife.data.effects.PotionEffectAction;
import info.faceland.strife.data.effects.ShootProjectile;
import info.faceland.strife.data.effects.SpawnParticle;
import info.faceland.strife.data.effects.Speak;
import info.faceland.strife.data.effects.Summon;
import info.faceland.strife.data.effects.Wait;
import info.faceland.strife.util.DamageUtil.DamageType;
import info.faceland.strife.util.LogUtil;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
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
      case HEAL:
        effect = new Heal();
        ((Heal) effect).setAmount(cs.getInt("amount", 1));
        ((Heal) effect).setDamageScale(DamageScale.valueOf(cs.getString("scale", "FLAT")));
        break;
      case DAMAGE:
        effect = new DealDamage();
        ((DealDamage) effect).setAmount(cs.getDouble("amount", 1));
        try {
          ((DealDamage) effect).setDamageScale(DamageScale.valueOf(cs.getString("scale", "FLAT")));
          ((DealDamage) effect)
              .setDamageType(DamageType.valueOf(cs.getString("damage-type", "TRUE")));
        } catch (Exception e) {
          LogUtil.printError("Skipping effect " + key + " for invalid damage scale/type");
          return;
        }
        break;
      case PROJECTILE:
        effect = new ShootProjectile();
        ((ShootProjectile) effect).setQuantity(cs.getInt("quantity", 1));
        EntityType projType;
        try {
          projType = EntityType.valueOf(cs.getString("projectile-type", "null"));
        } catch (Exception e) {
          LogUtil.printError("Skipping effect " + key + " for invalid projectile type");
          return;
        }
        if (!(projType == EntityType.ARROW || projType == EntityType.THROWN_EXP_BOTTLE
            || projType == EntityType.SPLASH_POTION || projType == EntityType.LINGERING_POTION
            || projType == EntityType.SHULKER_BULLET || projType == EntityType.PRIMED_TNT
            || projType == EntityType.EGG || projType == EntityType.SNOWBALL
            || projType == EntityType.FIREBALL || projType == EntityType.DRAGON_FIREBALL
            || projType == EntityType.SMALL_FIREBALL || projType == EntityType.WITHER_SKULL)) {
          LogUtil.printWarning("Skipping effect " + key + " for non projectile entity");
          return;
        }
        ((ShootProjectile) effect).setProjectileEntity(projType);
        ((ShootProjectile) effect).setVerticalBonus(cs.getDouble("vertical-bonus", 0));
        ((ShootProjectile) effect).setSpread(cs.getDouble("spread", 0));
        ((ShootProjectile) effect).setSpeed(cs.getDouble("speed", 1));
        ((ShootProjectile) effect).setYield((float) cs.getDouble("yield", 0.0D));
        ((ShootProjectile) effect).setIgnite(cs.getBoolean("ignite", false));
        ((ShootProjectile) effect).setIgnite(cs.getBoolean("bounce", false));
        ((ShootProjectile) effect).setHitEffects(cs.getStringList("hit-effects"));
        ((ShootProjectile) effect).setTargeted(cs.getBoolean("targeted", false));
        ((ShootProjectile) effect).setSeeking(cs.getBoolean("seeking", false));
        break;
      case IGNITE:
        effect = new Ignite();
        ((Ignite) effect).setDuration(cs.getInt("duration", 20));
        break;
      case BLEED:
        effect = new Bleed();
        ((Bleed) effect).setAmount(cs.getInt("amount", 10));
        ((Bleed) effect).setIgnoreArmor(cs.getBoolean("ignore-armor", true));
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
      case LEAP:
        effect = new Leap();
        ((Leap) effect).setForward(cs.getDouble("forward", 1));
        ((Leap) effect).setHeight(cs.getDouble("height", 1));
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
      case PARTICLE:
        effect = new SpawnParticle();
        Particle particle;
        try {
          particle = Particle.valueOf((cs.getString("particle-type")));
        } catch (Exception e) {
          LogUtil.printWarning("Invalid particle effect type in effect " + key + ". Skipping.");
          return;
        }
        ((SpawnParticle) effect).setParticle(particle);
        ((SpawnParticle) effect).setQuantity(cs.getInt("quantity", 10));
        ((SpawnParticle) effect).setSpeed((float) cs.getDouble("speed", 0));
        break;
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
    DAMAGE,
    HEAL,
    PROJECTILE,
    IGNITE,
    BLEED,
    WAIT,
    PARTICLE,
    SPEAK,
    KNOCKBACK,
    LEAP,
    POTION,
    SUMMON
  }
}
