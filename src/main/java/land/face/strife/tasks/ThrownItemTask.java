package land.face.strife.tasks;

import land.face.strife.util.SpecialStatusUtil;
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
  private static final EulerAngle startAngle = new EulerAngle(0, 0, 0);

  public ThrownItemTask(Projectile projectile, ItemStack stack, Location location) {
    this.projectile = projectile;
    stand = location.getWorld().spawn(location, ArmorStand.class);
    stand.setRightArmPose(startAngle);
    stand.setVisible(false);
    stand.setGravity(false);
    stand.setMarker(true);
    stand.setInvulnerable(true);
    stand.setItem(EquipmentSlot.HAND, stack.clone());
    SpecialStatusUtil.setDespawnOnUnload(stand);
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
    loc.add(projectile.getVelocity());
    stand.teleport(loc);
    stand.setRightArmPose(stand.getRightArmPose().add(0.82, 0.0, 0.0));
  }
}

