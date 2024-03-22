package land.face.strife.data.effects;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.BoneBehaviorTypes;
import java.util.Map;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.pojo.ItemBoneData;
import lombok.Setter;

public class EquipModelBones extends Effect {

  @Setter
  private String modelId;
  @Setter
  private Map<String, ItemBoneData> boneMap;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    ModeledEntity modeledEntity = ModelEngineAPI.getModeledEntity(target.getEntity());
    if (modeledEntity == null) {
      return;
    }
    modeledEntity.getModels().values().stream().findFirst().ifPresent(m -> m.getBones().forEach((s, modelBone) ->
        modelBone.getBoneBehavior(BoneBehaviorTypes.ITEM).ifPresent(iBone -> {
          if (boneMap.containsKey(iBone.getBone().getBoneId())) {
            ItemBoneData boneData = boneMap.get(iBone.getBone().getBoneId());
            iBone.setDisplay(boneData.getTransform());
            iBone.setItemProvider(boneData.getProvider());
          }
        })
    ));
  }
}
