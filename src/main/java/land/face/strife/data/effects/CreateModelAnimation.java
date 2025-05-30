package land.face.strife.data.effects;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.Dummy;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.BoneBehaviorTypes;
import com.ticxo.modelengine.api.model.bone.type.HeldItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import land.face.strife.StrifePlugin;
import land.face.strife.data.ContinuousModelData;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.TargetingUtil;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

@Setter
@Getter
public class CreateModelAnimation extends LocationEffect {

  private String modelId;
  private List<String> animations = new ArrayList<>();
  private String startAnimation;
  private String endAnimation;
  private float lerpIn, lerpOut, speed = 1, scale = 1, directionOffset = 0;
  private int lifespan, forceGroundBlocks = 0;
  private AnimationType animationType;
  private boolean entityLock, rotationLock, strictDuration, areaScaling, reachScaling,
      hasCheckedEquipBones = false, hasEquipBones = false, refreshModel = false;
  private Color color = null;

  public static Location TEMP_LOC;
  public static List<Dummy<?>> TRACKED_MODELS = new ArrayList<>();

  public enum AnimationType {
    LOOK_DIRECTION,
    LOOK_ROTATION,
    RANDOM_ROTATION,
  }

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {

    var modelthing = ContinuousModelData.fetch(target.getEntity(), this);
    if (modelthing != null) {
      float effectDuration = strictDuration ? 1f : 1f + caster.getStat(StrifeStat.EFFECT_DURATION) / 100;
      float realSpeed = speed / effectDuration;
      int realLifespan = getTrueLifespan(modelthing.getModel(), effectDuration, realSpeed);
      modelthing.refresh(realLifespan);
      return;
    }

    ActiveModel model = ModelEngineAPI.createActiveModel(modelId);
    if (model == null) {
      Bukkit.getLogger().warning("[Strife] Failed to create model animation! No model!" + getId());
      return;
    }
    Location loc = TargetingUtil.getOriginLocation(target.getEntity(), getOrigin());
    loc.setPitch(0);

    switch (animationType) {
      case LOOK_DIRECTION -> loc.setDirection(caster.getEntity().getEyeLocation().getDirection());
      case LOOK_ROTATION -> loc.setYaw(caster.getEntity().getEyeLocation().getYaw());
      case RANDOM_ROTATION -> loc.setYaw(StrifePlugin.RNG.nextFloat() * 360f);
    }

    if (forceGroundBlocks > 0) {
      int tries = forceGroundBlocks;
      loc = loc.clone();
      while (tries > 0) {
        var block = loc.getBlock();
        if (block.isLiquid()) {
          return;
        }
        if (block.getType().isSolid()) {
          loc.setY(loc.getBlockY() + block.getBoundingBox().getHeight() + 0.05);
          break;
        }
        loc.setY(loc.getY() - 0.5);
        tries--;
      }
      if (tries == 0) {
        return;
      }
    }
    applyModelStuff(caster, target, loc, model);
  }

  @Override
  public void applyAtLocation(StrifeMob caster, Location loc) {

    var modelthing = ContinuousModelData.fetch(caster.getEntity(), this);
    if (modelthing != null) {
      float effectDuration = strictDuration ? 1f : 1f + caster.getStat(StrifeStat.EFFECT_DURATION) / 100;
      float realSpeed = speed / effectDuration;
      int realLifespan = getTrueLifespan(modelthing.getModel(), effectDuration, realSpeed);
      modelthing.refresh(realLifespan);
      return;
    }

    ActiveModel model = ModelEngineAPI.createActiveModel(modelId);
    if (model == null) {
      Bukkit.getLogger().warning("[Strife] Failed to create model animation! No model!" + getId());
      return;
    }

    switch (animationType) {
      case LOOK_DIRECTION -> {
        if (TEMP_LOC == null) {
          loc.setPitch(0);
          loc.setDirection(caster.getEntity().getEyeLocation().getDirection());
        } else {
          loc.setPitch(TEMP_LOC.getPitch());
          loc.setYaw(TEMP_LOC.getYaw());
        }
      }
      case LOOK_ROTATION -> {
        if (TEMP_LOC == null) {
          loc.setPitch(0);
          loc.setYaw(caster.getEntity().getEyeLocation().getYaw());
        } else {
          loc.setPitch(TEMP_LOC.getPitch());
          loc.setYaw(TEMP_LOC.getYaw());
        }
      }
      case RANDOM_ROTATION -> {
        loc.setPitch(0);
        loc.setYaw(StrifePlugin.RNG.nextFloat() * 360f);
      }
    }

    if (forceGroundBlocks > 0) {
      int tries = forceGroundBlocks;
      loc = loc.clone();
      while (tries > 0) {
        var block = loc.getBlock();
        if (block.isLiquid()) {
          return;
        }
        if (block.getType().isSolid()) {
          loc.setY(loc.getBlockY() + block.getBoundingBox().getHeight() + 0.05);
          break;
        }
        loc.setY(loc.getY() - 0.5);
        tries--;
      }
      if (tries == 0) {
        return;
      }
    }
    loc.setY(loc.getY() + 0.001 * Math.random());
    applyModelStuff(caster, caster, loc, model);
  }

  private void applyModelStuff(StrifeMob caster, StrifeMob target, Location loc, ActiveModel model) {

    Dummy<?> dummy = new Dummy<>();
    dummy.setLocation(loc.clone().add(loc.getDirection().multiply(directionOffset)));
    ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(dummy);
    if (modeledEntity == null) {
      dummy.setRemoved(true);
      Bukkit.getLogger().warning("Failed to create modelled entity");
      return;
    } else {
      modeledEntity.setSaved(false);
      if (color != null) {
        model.setDefaultTint(color);
      }
      modeledEntity.addModel(model, true);
      //modeledEntity.detectPlayers();
      dummy.getBodyRotationController().setRotationDuration(0);
      dummy.getBodyRotationController().setRotationDelay(0);
      dummy.setYBodyRot(loc.getYaw());
      dummy.setXHeadRot(loc.getPitch());
      dummy.setYHeadRot(loc.getYaw());
      dummy.setWalking(true);
    }

    float finalScale = scale;
    float extension = 0;
    if (areaScaling) {
      extension += caster.getStat(StrifeStat.AREA_OF_EFFECT);
    }
    if (reachScaling) {
      extension += caster.getStat(StrifeStat.ATTACK_RANGE);
    }
    if (Math.round(extension) != 0) {
      //extension /= (float) Math.PI;
      finalScale *= (1 + extension / 100);
    }
    model.setScale(finalScale);
    if (color != null) {
      model.setDefaultTint(color);
    }

    modeledEntity.addModel(model, false);
    modeledEntity.setModelRotationLocked(rotationLock);
    modeledEntity.setBaseEntityVisible(false);
    modeledEntity.getBase().setRenderRadius(36);

    float effectDuration = strictDuration ? 1f : 1f + caster.getStat(StrifeStat.EFFECT_DURATION) / 100;
    float realSpeed = speed / effectDuration;
    int realLifespan = getTrueLifespan(model, effectDuration, realSpeed);

    int endTicks = 0;
    if (endAnimation != null) {
      endTicks = (int) (20f * model.getBlueprint().getAnimations().get(endAnimation).getLength());
    }

    if (hasEquipBones || !hasCheckedEquipBones) {
      equipModel(caster, model);
      hasCheckedEquipBones = true;
    }

    ContinuousModelData.create(this, model, dummy, target, realSpeed, realLifespan, endTicks);
  }

  private int getTrueLifespan(ActiveModel model, float effectDuration, float realSpeed) {
    int realLifespan = lifespan;
    if (!animations.isEmpty()) {
      if (lifespan == -1) {
        Collections.shuffle(animations);
        double animationDuration = model.getBlueprint().getAnimations().get(animations.getFirst()).getLength() / realSpeed;
        realLifespan = (int) Math.floor(20 * animationDuration) + 1;
      } else {
        realLifespan = (int) ((float) lifespan * effectDuration);
      }
    }
    if (realLifespan == -1) {
      Bukkit.getLogger().warning(
          "[Strife] You screwed up effect " + getId() + " only supply -1 lifespan if an animation-id is defined!");
      realLifespan = 20;
    }
    return realLifespan;
  }

  public static void equipModel(StrifeMob caster, ActiveModel model) {
    ItemStack stack = caster.getEntity().getEquipment().getItemInMainHand().clone();
    if (stack.getType() == Material.AIR) {
      stack = caster.getEntity().getEquipment().getItemInOffHand().clone();
      if (stack.getType() == Material.AIR) {
        stack = new ItemStack(Material.IRON_SWORD);
      }
    }
    var provider = new HeldItem.StaticItemStackSupplier(stack);
    model.getBones().forEach((s, modelBone) ->
        modelBone.getBoneBehavior(BoneBehaviorTypes.ITEM).ifPresent(heldItem ->
            heldItem.setItemProvider(provider)
        )
    );
  }
}