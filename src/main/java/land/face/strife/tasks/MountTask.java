package land.face.strife.tasks;

import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.bone.manager.MountManager;
import java.lang.ref.WeakReference;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.managers.PlayerMountManager;
import land.face.strife.util.JumpUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class MountTask extends BukkitRunnable {

  private final PlayerMountManager manager;
  @Getter
  private final WeakReference<Player> player;
  @Getter
  private final WeakReference<StrifeMob> mount;
  @Getter
  private final ActiveModel model;

  public MountTask(PlayerMountManager manager, Player player, StrifeMob mount, ActiveModel model) {
    this.player = new WeakReference<>(player);
    this.manager = manager;
    this.mount = new WeakReference<>(mount);
    this.model = model;
    this.runTaskTimer(StrifePlugin.getInstance(), 10L, 4L);
  }

  @Override
  public void run() {
    Player player = getPlayer().get();
    if (player == null) {
      Bukkit.getLogger().warning("[Strife] Mount task cancelled due to null player?!");
      cancel();
      return;
    }
    if (mount.get() == null || mount.get().getEntity() == null || !mount.get().getEntity()
        .isValid() || player.isSneaking() || player.isSwimming() || JumpUtil.isRooted(player)) {
      player.leaveVehicle();
      manager.despawn(player);
      return;
    }
    if (model == null) {
      if (mount.get().getEntity().getPassengers().isEmpty()) {
        player.leaveVehicle();
        manager.despawn(player);
        return;
      }
    } else {
      model.getMountManager().ifPresent(m -> {
        if (m.getDriver() == null) {
          Bukkit.getLogger().info("[testoSTRIFE] despawned due to no driver");
          manager.despawn(player);
        }
      });
    }
    Material material = mount.get().getEntity().getLocation().getBlock().getType();
    if (material == Material.WATER || material == Material.LAVA) {
      player.leaveVehicle();
      manager.despawn(player);
      return;
    }
    Material material2 = player.getEyeLocation().getBlock().getType();
    if (material2.isSolid()) {
      player.leaveVehicle();
      manager.despawn(player);
    }
  }
}
