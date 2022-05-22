package land.face.strife.tasks;

import com.ticxo.modelengine.api.model.ActiveModel;
import java.lang.ref.WeakReference;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.managers.PlayerMountManager;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;

public class MountTask extends BukkitRunnable {

  private final PlayerMountManager manager;
  @Getter
  private final UUID playerUUID;
  @Getter
  private final WeakReference<StrifeMob> mount;
  @Getter
  private final ActiveModel model;

  public MountTask(PlayerMountManager manager, UUID playerUUID, StrifeMob mount,
      ActiveModel model) {
    this.playerUUID = playerUUID;
    this.manager = manager;
    this.mount = new WeakReference<>(mount);
    this.model = model;
    this.runTaskTimer(StrifePlugin.getInstance(), 1L, 4L);
  }

  @Override
  public void run() {
    if (mount.get() == null || mount.get().getEntity() == null || !mount.get().getEntity()
        .isValid()) {
      manager.despawn(playerUUID);
      return;
    }
    if (model == null) {
      if (mount.get().getEntity().getPassengers().isEmpty()) {
        manager.despawn(playerUUID);
        return;
      }
    } else {
      if (model.getModeledEntity().getMountHandler().getDriver() == null) {
        manager.despawn(playerUUID);
        return;
      }
    }
    Material material = mount.get().getEntity().getLocation().getBlock().getType();
    if (material == Material.WATER || material == Material.LAVA) {
      manager.despawn(playerUUID);
    }
  }
}