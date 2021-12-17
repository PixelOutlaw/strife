package land.face.strife.data.effects;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.generator.ModelBlueprint;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.PartEntity;
import land.face.strife.data.StrifeMob;
import lombok.Setter;
import org.bukkit.Bukkit;

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
    ActiveModel currentModel = target.getModelEntity().getActiveModel(oldModelId);
    final PartEntity partEntity = currentModel.getPartEntity(oldPartId);
    if (partEntity == null) {
      return;
    }
    final ModelBlueprint blueprint = ModelEngineAPI.api.getModelBlueprint(newModelId);
    if (blueprint == null) {
      return;
    }
    final int id = blueprint.getItemModelId(newPartId);
    if (id <= 0) {
      return;
    }
    partEntity.setDataId(id);
  }
}