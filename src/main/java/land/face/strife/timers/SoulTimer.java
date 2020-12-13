/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package land.face.strife.timers;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.util.LogUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class SoulTimer extends BukkitRunnable {

  private final Location location;
  private final UUID owner;
  private final Set<Player> viewers = new HashSet<>();
  private final Hologram soulHead;
  private final long creationTime = System.currentTimeMillis();

  private static int MAX_SOUL_LIFESPAN = 300000;

  private double lostExp = 0;

  public SoulTimer(UUID owner, Hologram soulHead, Location location) {
    this.owner = owner;
    this.location = location;
    this.soulHead = soulHead;
    LogUtil.printDebug("New SoulTimer created for " + owner);
    runTaskTimer(StrifePlugin.getInstance(), 0L, 20L);
  }

  @Override
  public void run() {
    if (System.currentTimeMillis() - creationTime > MAX_SOUL_LIFESPAN) {
      StrifePlugin.getInstance().getSoulManager().removeSoul(this);
      return;
    }
    Player p = Bukkit.getPlayer(owner);
    if (p != null && !p.isDead()) {
      if (!StrifePlugin.getInstance().getSoulManager().isDeathWorld(p.getWorld().getName())) {
        StrifePlugin.getInstance().getSoulManager().removeSoul(this);
        return;
      }
    }

    Set<Player> oldViewers = new HashSet<>(viewers);
    Set<Player> currentViewers = new HashSet<>();

    for (Entity e : location.getWorld().getNearbyEntities(location, 24, 24, 24, this::canSeeSouls)) {
      ((Player) e).spawnParticle(Particle.SOUL, location, 4, 0.3, 0.3, 0.3, 0);
      ((Player) e).spawnParticle(Particle.END_ROD, location, 4, 0.6, 0.6, 0.6, 0);
      currentViewers.add((Player) e);
    }

    for (Player player : oldViewers) {
      if (currentViewers.contains(player)) {
        continue;
      }
      soulHead.getVisibilityManager().hideTo(player);
    }

    for (Player player : currentViewers) {
      if (oldViewers.contains(player)) {
        continue;
      }
      soulHead.getVisibilityManager().showTo(player);
    }

    viewers.clear();
    viewers.addAll(currentViewers);
  }

  public UUID getOwner() {
    return owner;
  }

  public Location getLocation() {
    return location;
  }

  public Set<Player> getViewers() {
    return viewers;
  }

  public Hologram getSoulHead() {
    return soulHead;
  }

  public double getLostExp() {
    return lostExp;
  }

  public void setLostExp(double lostExp) {
    this.lostExp = lostExp;
  }

  private boolean canSeeSouls(Entity entity) {
    if (entity instanceof Player) {
      return StrifePlugin.getInstance().getSoulManager().canSeeSouls((Player) entity);
    }
    return false;
  }
}
