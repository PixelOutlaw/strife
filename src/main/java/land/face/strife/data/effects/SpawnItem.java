package land.face.strife.data.effects;

import land.face.strife.data.StrifeMob;
import land.face.strife.data.buff.Buff;
import land.face.strife.data.buff.LoadedBuff;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.LogUtil;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

public class SpawnItem extends LocationEffect {

  @Setter
  private String itemId;
  @Setter
  private boolean naturalDrop;
  @Setter
  private boolean canPickup;
  @Setter
  private int protectTicks;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    Location loc = target.getEntity().getLocation().clone();
    applyAtLocation(caster, loc);
  }

  @Override
  public void applyAtLocation(StrifeMob caster, Location location) {
    ItemStack itemStack = getPlugin().getEquipmentManager().getItem(itemId);
    if (itemStack == null) {
      return;
    }
    Item item;
    if (naturalDrop) {
      item = location.getWorld().dropItemNaturally(location, itemStack);
    } else {
      item = location.getWorld().dropItem(location, itemStack);
    }
    item.setCanMobPickup(false);
    item.setCanPlayerPickup(canPickup);
    if (canPickup && protectTicks > 0) {
      item.setOwner(caster.getEntity().getUniqueId());
      Bukkit.getScheduler().runTaskLater(getPlugin(), () -> item.setOwner(null), protectTicks);
    }
  }
}