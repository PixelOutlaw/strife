package land.face.strife.tasks;

import com.tealcube.minecraft.bukkit.facecore.utilities.ChunkUtil;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;

public class ThrownItemTask extends BukkitRunnable {

  private final Projectile projectile;
  private final ArmorStand stand;
  private final boolean spin;
  private static final EulerAngle spin_angle = new EulerAngle(0, 0, 0);
  private static final EulerAngle point_angle = new EulerAngle(0, 5, 272);

  public ThrownItemTask(Projectile projectile, ItemStack stack, Location location, boolean spin) {
    this.projectile = projectile;
    this.spin = spin;
    stand = location.getWorld().spawn(location, ArmorStand.class);
    stand.setRightArmPose(spin ? spin_angle : point_angle);
    stand.setVisible(false);
    stand.setGravity(false);
    stand.setMarker(true);
    stand.setInvulnerable(true);
    stand.setItem(EquipmentSlot.HAND, stack.clone());
    ChunkUtil.setDespawnOnUnload(stand);
  }

  @Override
  public void run() {
    if (projectile == null) {
      stand.remove();
      cancel();
      return;
    }
    if (!projectile.isValid() || (projectile instanceof Arrow && ((Arrow) projectile).isInBlock())) {
      stand.remove();
      cancel();
      return;
    }
    Location loc = projectile.getLocation().clone();
    loc.add(0, -0.9, 0);
    loc.setDirection(projectile.getVelocity());
    if (spin) {
      stand.setRightArmPose(stand.getRightArmPose().add(0.4, 0.0, 0.0));
    }
    stand.teleport(loc);
    stand.setVelocity(projectile.getVelocity());
  }
}

