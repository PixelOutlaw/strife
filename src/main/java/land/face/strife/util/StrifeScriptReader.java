package land.face.strife.util;

import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
import com.ticxo.modelengine.api.animation.script.ScriptReader;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.utils.logger.TLogger;
import land.face.strife.StrifePlugin;
import land.face.strife.data.TargetResponse;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class StrifeScriptReader implements ScriptReader {

  private final StrifePlugin plugin;

  public StrifeScriptReader(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  public void read(IAnimationProperty property, String script) {
    /*
    ActiveModel model = property.getModel();
    ModelBlueprint blueprint = model.getBlueprint();
    Object original = model.getModeledEntity().getBase().getOriginal();
    if (original instanceof ArmorStand) {
      TargetResponse response;
      plugin.getEffectManager().executeEffectList();
    } else if (original instanceof LivingEntity) {

    } else if (original instanceof Entity) {

    }
      Entity entity = (Entity)original;
      float power = 1.0F;
      ActiveMob activeMob = MythicBukkit.inst().getAPIHelper().getMythicMobInstance(entity);
      if (activeMob != null) {
        power = activeMob.getPower();
      }

      String[] scriptSplit = script.split("\\{", 2);
      String skillName = scriptSplit[0];
      boolean succeed = MythicBukkit.inst().getAPIHelper().castSkill(entity, skillName, power, (meta) -> {
        if (scriptSplit.length == 2) {
          String[] parameters = scriptSplit[1].substring(0, scriptSplit[1].length() - 1).split(";");
          String[] var5 = parameters;
          int var6 = parameters.length;

          for(int var7 = 0; var7 < var6; ++var7) {
            String param = var5[var7];
            String[] entry = param.split("=", 2);
            meta.getParameters().put(entry[0], entry.length == 2 ? this.getAnimationPlaceholder(blueprint, entry[1].strip()) : "");
          }

        }
      });
      if (!succeed) {
        TLogger.warn("Unknown MythicMobs script: " + script);
      }

    }

     */
  }

  private String getAnimationPlaceholder(ModelBlueprint blueprint, String placeholder) {
    if (placeholder.startsWith("<") && placeholder.endsWith(">")) {
      String key = placeholder.substring(1, placeholder.length() - 1);
      return (String) blueprint.getAnimationsPlaceholders().getOrDefault(key, placeholder);
    } else {
      return placeholder;
    }
  }
}

