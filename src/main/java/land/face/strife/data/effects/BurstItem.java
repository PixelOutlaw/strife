package land.face.strife.data.effects;

import com.tealcube.minecraft.bukkit.facecore.utilities.ChunkUtil;
import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import java.util.List;
import land.face.strife.data.StrifeMob;
import land.face.strife.util.TargetingUtil;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class BurstItem extends LocationEffect {

  @Setter
  private String equipmentId;
  @Setter
  private String mobId;
  @Setter
  private int amount;
  @Setter
  private float minSpeed;
  @Setter
  private float maxSpeed;
  @Setter
  private int minLife;
  @Setter
  private int maxLife;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    applyAtLocation(caster, TargetingUtil.getOriginLocation(target.getEntity(), getOrigin()));
  }

  @Override
  public void applyAtLocation(StrifeMob caster, Location location) {
    if (equipmentId != null) {
      ItemStack s = getPlugin().getEquipmentManager().getItem(equipmentId);
      int count = amount;
      while (count > 0) {
        Item item = location.getWorld().spawn(location, Item.class, (i) -> {
          ItemStack loopStack = s.clone();
          TextUtils.setLore(loopStack, List.of(" " + Math.random()), false);
          i.setItemStack(loopStack);
          float power = (float) (minSpeed + ((maxSpeed - minSpeed) * Math.random()));
          Vector velocity = new Vector(Math.random(), 0.1 + Math.random() / 3, Math.random());
          velocity.multiply(power * (-1 + (2 * Math.random())));
          i.setVelocity(velocity);
          i.setInvulnerable(true);
          i.setWillAge(false);
          i.setCanMobPickup(false);
          i.setCanPlayerPickup(false);
        });
        Bukkit.getScheduler().runTaskLater(
            getPlugin(),
            item::remove,
            (long) (minLife + (float) (maxLife - minLife) * Math.random())
        );
        ChunkUtil.setDespawnOnUnload(item);
        count--;
      }
    } else if (mobId != null) {
      EntityType type;
      try {
        type = EntityType.valueOf(mobId);
      } catch (Exception e) {
        Bukkit.getLogger().warning("[Strife] Invalid mob type for effect " + getId());
        return;
      }
      int count = amount;
      while (count > 0) {
        Entity mob = location.getWorld().spawn(location, type.getEntityClass(), (i) -> {
          float power = (float) (minSpeed + ((maxSpeed - minSpeed) * Math.random()));
          Vector velocity = new Vector(Math.random(), 0.1 + Math.random() / 3, Math.random());
          velocity.multiply(power * (-1 + (2 * Math.random())));
          i.setVelocity(velocity);
          i.setInvulnerable(true);
        });
        Bukkit.getScheduler().runTaskLater(
            getPlugin(),
            mob::remove,
            (long) (minLife + (float) (maxLife - minLife) * Math.random())
        );
        ChunkUtil.setDespawnOnUnload(mob);
        count--;
      }
    }
  }
}