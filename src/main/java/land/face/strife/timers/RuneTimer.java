/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package land.face.strife.timers;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.managers.RuneManager;
import land.face.strife.tasks.ParticleTask;
import land.face.strife.util.PlayerDataUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class RuneTimer extends BukkitRunnable {

  private final WeakReference<StrifeMob> target;
  private final UUID savedUUID;
  private final RuneManager runeManager;
  private final Set<Hologram> runeHolograms = new HashSet<>();

  private static final Map<Integer, Map<Integer, List<Vector>>> gigaMap = cacheRuneLocations();

  private static final ItemStack RUNE_STACK = new ItemStack(Material.COARSE_DIRT);

  private int lifeTicks = 3000;

  public RuneTimer(RuneManager runeManager, StrifeMob mob) {
    this.target = new WeakReference<>(mob);
    this.runeManager = runeManager;
    savedUUID = mob.getEntity().getUniqueId();
    runTaskTimer(StrifePlugin.getInstance(), 0L, 2L);
  }

  @Override
  public void run() {
    StrifeMob mob = target.get();
    if (mob == null || mob.getEntity() == null || !mob.getEntity().isValid()) {
      runeManager.clearRunes(savedUUID);
      return;
    }
    if ((mob.getEarthRunes() < 1 || lifeTicks < 1 ||
        mob.getEarthRunes() == 0 && runeHolograms.size() == 0)) {
      mob.setEarthRunes(0);
      runeManager.clearRunes(savedUUID);
      return;
    }
    orbitRunes(mob.getEntity().getLocation().clone().add(0, 1, 0), runeHolograms);
    lifeTicks--;
  }

  public void bumpTime() {
    lifeTicks = 3000;
  }

  public void updateHolos() {
    StrifeMob mob = target.get();
    int currentRunes = mob.getEarthRunes();
    int visualRunes = runeHolograms.size();
    if (currentRunes < runeHolograms.size()) {
      while (currentRunes < runeHolograms.size()) {
        Hologram hologram = PlayerDataUtil.getRandomFromCollection(runeHolograms);
        mob.getEntity().getWorld()
            .playSound(hologram.getLocation(), Sound.BLOCK_GRASS_BREAK, 1f, 0.8f);
        mob.getEntity().getWorld().spawnParticle(
            Particle.ITEM_CRACK,
            hologram.getLocation(),
            20, 0, 0, 0, 0.07f,
            RUNE_STACK
        );
        hologram.delete();
        runeHolograms.remove(hologram);
      }
    } else if (currentRunes > visualRunes) {
      while (currentRunes > runeHolograms.size() && runeHolograms.size() < 6) {
        Hologram holo = DHAPI.createHologram(
            UUID.randomUUID().toString(),
            mob.getEntity().getEyeLocation().clone(),
            List.of("#ICON: IRON_NUGGET {CustomModelData:" + (100 + StrifePlugin.RNG.nextInt(5) + "}"))
        );
        runeHolograms.add(holo);
      }
    }
  }

  public void clearHolos() {
    for (Hologram hologram : runeHolograms) {
      hologram.delete();
    }
    runeHolograms.clear();
  }

  private static Map<Integer, Map<Integer, List<Vector>>> cacheRuneLocations() {
    Map<Integer, Map<Integer, List<Vector>>> runeTickMap = new HashMap<>();
    for (int tickIndex = 1; tickIndex <= 360; tickIndex++) {
      Map<Integer, List<Vector>> runeMap = new HashMap<>();
      for (int numRunes = 1; numRunes <= 10; numRunes++) {
        float step = 360f / numRunes;
        float start = (float) tickIndex;
        int currentRuneIndex = 0;
        List<Vector> displacements = new ArrayList<>();
        while (currentRuneIndex < numRunes) {
          float radian1 = (float) Math.toRadians(start + step * currentRuneIndex);
          displacements.add(new Vector(0.7 * Math.cos(radian1), 0,  0.7 * Math.sin(radian1)));
          currentRuneIndex++;
        }
        runeMap.put(numRunes, displacements);
      }
      runeTickMap.put(tickIndex, runeMap);
    }
    return runeTickMap;
  }

  public void orbitRunes(Location center, Set<Hologram> runeHolograms) {
    if (runeHolograms.size() == 0) {
      return;
    }
    int total = Math.min(runeHolograms.size(), 10);
    int index = 0;
    for (Hologram holo : runeHolograms) {
      Location loc = center.clone();
      Vector offset = gigaMap.get(ParticleTask.getCurrentTick()).get(total).get(index);
      loc.add(offset);
      DHAPI.moveHologram(holo, loc);
      index++;
    }
  }
}
