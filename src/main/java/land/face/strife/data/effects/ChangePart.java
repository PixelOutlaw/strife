package land.face.strife.data.effects;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.generator.blueprint.BlueprintBone;
import com.ticxo.modelengine.api.generator.blueprint.ModelBlueprint;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.bone.ModelBone;
import land.face.strife.data.StrifeMob;
import lombok.Setter;

public class ChangePart extends Effect {

  @Setter
  private String newModelId;
  @Setter
  private String oldModelId;
  @Setter
  private String oldPartId;
  @Setter
  private String newPartId;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (target.getModelEntity() == null) {
      return;
    }
    // Might only work on things with one active model? can you have two...?
    ActiveModel currentModel = target.getModelEntity().getModels().get(oldModelId);
    final ModelBone partEntity = currentModel.getBone(oldPartId).get();
    if (partEntity == null) {
      return;
    }
    final ModelBlueprint blueprint = ModelEngineAPI.getBlueprint(newModelId);
    if (blueprint == null) {
      return;
    }
    BlueprintBone bpBone = blueprint.getBones().get(newPartId);
    if (bpBone == null) {
      return;
    }
    if (partEntity.getParent() == null) {
      currentModel.removeBone(oldPartId);
      currentModel.forceGenerateBone(null, null, bpBone);
    } else {
      currentModel.removeBone(oldPartId);
      currentModel.forceGenerateBone(partEntity.getParent().getBoneId(), null, bpBone);
    }
  }
}