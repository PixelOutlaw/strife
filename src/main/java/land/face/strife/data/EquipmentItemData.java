package land.face.strife.data;

import java.util.Set;
import land.face.strife.util.ItemUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class EquipmentItemData {

  private Material material;
  private int minData;
  private int maxData;

  public Material getMaterial() {
    return material;
  }

  public void setMaterial(Material material) {
    this.material = material;
  }

  public int getMinData() {
    return minData;
  }

  public void setMinData(int minData) {
    this.minData = minData;
  }

  public int getMaxData() {
    return maxData;
  }

  public void setMaxData(int maxData) {
    this.maxData = maxData;
  }

  public static boolean contains(Set<EquipmentItemData> dataSet, ItemStack stack) {
    for (EquipmentItemData data : dataSet) {
      if (data.getMaterial() != stack.getType()) {
        continue;
      }
      if (data.getMinData() != -1 && ItemUtil.getCustomData(stack) < data.getMinData()) {
        continue;
      }
      if (data.getMaxData() != -1 && ItemUtil.getCustomData(stack) > data.getMaxData()) {
        continue;
      }
      return true;
    }
    return false;
  }
}
