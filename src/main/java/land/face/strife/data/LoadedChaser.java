package land.face.strife.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import land.face.strife.StrifePlugin;
import land.face.strife.data.effects.Effect;
import land.face.strife.data.effects.StrifeParticle;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.configuration.ConfigurationSection;

@Getter
@Setter
public class LoadedChaser {

  private float speed, startSpeed, maxSpeed, startHorizontalMin, startHorizontalMax, startVerticalMin, startVerticalMax, gravity;
  private int lifespan, guideStopTicks;
  private boolean physical, strictDuration, destroyOnBlockContact, blockContactEffects, expiryEffects;
  private String modelId;
  private List<Color> modelColors = new ArrayList<>();
  private List<Double> modelSizes = new ArrayList<>();
  private final List<Effect> effectList = new ArrayList<>();
  private final List<StrifeParticle> particles = new ArrayList<>();

  public final static Map<String, LoadedChaser> LOADED_TEMPLATES = new HashMap<>();

  public static LoadedChaser loadChaser(String id, LoadedChaser data) {
    LOADED_TEMPLATES.put(id, data);
    return data;
  }

  public static LoadedChaser loadChaser(StrifePlugin plugin, String id, ConfigurationSection cs) {
    LoadedChaser data = new LoadedChaser();

    data.setLifespan(cs.getInt("lifespan", 200));
    data.setGuideStopTicks(cs.getInt("guide-span", -1));
    data.setStrictDuration(cs.getBoolean("strict-duration", false));

    data.setSpeed((float) cs.getDouble("speed", 1));
    data.setMaxSpeed((float) cs.getDouble("max-speed", 1));
    data.setStartSpeed((float) cs.getDouble("start-speed", 1));

    data.setStartHorizontalMin((float) cs.getDouble("start-horizontal-min", 0));
    data.setStartHorizontalMax((float) cs.getDouble("start-horizontal-max", 0.1));
    data.setStartVerticalMin((float) cs.getDouble("start-vertical-min", 0));
    data.setStartVerticalMax((float) cs.getDouble("start-vertical-max", 0.1));

    data.setGravity((float) cs.getDouble("gravity", 0));
    data.setModelId(cs.getString("model-id", null));
    data.setModelColors(cs.getStringList("model-colors").stream().map(s -> {
      try {
        var realColor = java.awt.Color.decode(s);
        return Color.fromRGB(realColor.getRed(), realColor.getGreen(), realColor.getBlue());
      } catch (Exception e) {
        return Color.WHITE;
      }
    }).toList());
    data.setModelSizes(cs.getDoubleList("model-sizes"));

    data.setPhysical(cs.getBoolean("physical-projectile", true));
    data.setDestroyOnBlockContact(cs.getBoolean("destroy-on-block-contact", false));
    data.setBlockContactEffects(cs.getBoolean("execute-on-block-contact", false));
    data.setExpiryEffects(cs.getBoolean("execute-on-expiry", false));
    //data.setUseProjectile(cs.getBoolean("remove-on-block-contact", true));

    List<String> effects = cs.getStringList("effects");
    List<String> particles = cs.getStringList("particles");

    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> {
      for (String s : effects) {
        data.getEffectList().add(plugin.getEffectManager().getEffect(s));
      }
      for (String s : particles) {
        data.getParticles().add((StrifeParticle) plugin.getEffectManager().getEffect(s));
      }
    }, 5L);

    LOADED_TEMPLATES.put(id, data);
    return data;
  }
}
