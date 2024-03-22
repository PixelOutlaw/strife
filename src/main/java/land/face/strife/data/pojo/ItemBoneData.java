package land.face.strife.data.pojo;

import com.ticxo.modelengine.api.model.bone.type.HeldItem;
import com.ticxo.modelengine.api.model.bone.type.HeldItem.StaticItemStackSupplier;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
public class ItemBoneData {

  private final String id;
  private final StaticItemStackSupplier provider;
  private final ItemDisplayTransform transform;

  public ItemBoneData(String id, ItemDisplayTransform transform, ItemStack itemStack) {
    this.id = id;
    this.provider = new HeldItem.StaticItemStackSupplier(itemStack);
    this.transform = transform;
  }
}
