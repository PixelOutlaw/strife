/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package land.face.strife.managers;

import com.tealcube.minecraft.bukkit.facecore.utilities.ChunkUtil;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import land.face.dinvy.DeluxeInvyPlugin;
import land.face.dinvy.windows.equipment.EquipmentMenu.DeluxeSlot;
import land.face.strife.StrifePlugin;
import land.face.strife.data.LoadedMount;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.Champion;
import land.face.strife.patch.FacelandMountController;
import land.face.strife.stats.StrifeStat;
import land.face.strife.tasks.MountTask;
import land.face.strife.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PlayerMountManager {

  private final StrifePlugin plugin;

  private final Map<UUID, MountTask> ownerMap = new HashMap<>();
  private final Map<UUID, String> selectedMount = new HashMap<>();
  private final Map<String, LoadedMount> loadedMounts = new HashMap<>();

  public PlayerMountManager(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  public boolean isMounted(Player player) {
    return ownerMap.containsKey(player.getUniqueId());
  }

  public LoadedMount getMount(Player player) {
    if (!selectedMount.containsKey(player.getUniqueId())) {
      return null;
    }
    return loadedMounts.get(selectedMount.get(player.getUniqueId()));
  }

  public boolean spawnMount(Player player) {
    UUID uuid = player.getUniqueId();
    if (ownerMap.containsKey(uuid)) {
      Bukkit.getLogger().info("[Strife] aaaa");
      return false;
    }
    String mountId = selectedMount.get(uuid);
    if (mountId == null) {
      Bukkit.getLogger().info("[Strife] bbb");
      return false;
    }
    LoadedMount loadedMount = loadedMounts.get(mountId);
    if (loadedMount == null) {
      Bukkit.getLogger().info("[Strife] cccc");
      return false;
    }
    createMount(player, loadedMount);
    return true;
  }

  public void updateSelectedMount(Player player) {
    if (plugin.getDeluxeInvyPlugin() == null) {
      return;
    }
    ItemStack stack = DeluxeInvyPlugin.getInstance().getPlayerManager()
        .getPlayerData(player).getEquipmentItem(DeluxeSlot.MOUNT);
    if (stack == null || stack.getType() != Material.SADDLE) {
      selectedMount.remove(player.getUniqueId());
      if (ownerMap.containsKey(player.getUniqueId())) {
        despawn(player);
      }
      return;
    }
    int mountData = ItemUtil.getCustomData(stack);
    LoadedMount mount = getLoadedMountFromData(mountData);
    if (mount == null) {
      selectedMount.remove(player.getUniqueId());
      if (ownerMap.containsKey(player.getUniqueId())) {
        despawn(player);
      }
      return;
    }
    selectedMount.put(player.getUniqueId(), mount.getId());
  }

  public LoadedMount getLoadedMountFromData(int data) {
    for (LoadedMount l : loadedMounts.values()) {
      if (l.getCustomModelData() == data) {
        return l;
      }
    }
    return null;
  }

  private void createMount(Player player, LoadedMount loadedMount) {

    Pig pig = player.getWorld().spawn(player.getLocation(), Pig.class);
    pig.setAdult();
    pig.setBreed(false);
    pig.setSilent(true);
    pig.setCustomName(player.getName() + "'s Mount");
    pig.setCustomNameVisible(false);

    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(pig);
    mob.setOwner(plugin.getStrifeMobManager().getStatMob(player));
    mob.forceSetStat(StrifeStat.MOVEMENT_SPEED, loadedMount.getSpeed());
    mob.forceSetStat(StrifeStat.HEALTH, 20 + player.getLevel());
    mob.forceSetStat(StrifeStat.HEALTH_MULT, 0);
    mob.forceSetStat(StrifeStat.WEIGHT, 200);
    mob.forceSetStat(StrifeStat.ARMOR, 250);
    mob.forceSetStat(StrifeStat.WARDING, 250);
    mob.forceSetStat(StrifeStat.ALL_RESIST, 80);
    mob.forceSetStat(StrifeStat.DAMAGE_REDUCTION, -5);
    plugin.getStatUpdateManager().updateAllAttributes(mob);
    ChunkUtil.setDespawnOnUnload(pig);

    ActiveModel model = null;
    if (loadedMount.getModelId() != null) {
      model = ModelEngineAPI.createActiveModel(loadedMount.getModelId());
      if (model == null) {
        Bukkit.getLogger().warning("[Strife] (Mounts) No valid model for " + loadedMount.getId());
      } else {
        ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(pig);
        if (modeledEntity != null) {
          modeledEntity.addModel(model, true);
          modeledEntity.setBaseEntityVisible(false);
          modeledEntity.getMountManager().setCanSteer(true);
          modeledEntity.getMountManager().setCanRide(true);
          FacelandMountController c = new FacelandMountController(model, loadedMount);
          player.leaveVehicle();
          modeledEntity.getMountManager().setDriver(player, c);
          c.setFlying(modeledEntity);
        }
      }
    } else {
      pig.addPassenger(player);
    }

    MountTask mountTask = new MountTask(this, player, mob, model);
    ownerMap.put(player.getUniqueId(), mountTask);
    Champion champion = plugin.getChampionManager().getChampion(player);
    champion.getSaveData().setOnMount(true);
  }

  public void despawn(Player player) {
    UUID uuid = player.getUniqueId();
    MountTask task = ownerMap.get(uuid);
    if (task == null) {
      return;
    }
    Champion champion = plugin.getChampionManager().getChampion(player);
    champion.getSaveData().setOnMount(false);
    player.leaveVehicle();
    task.getMount().get().getEntity().eject();
    if (task.getModel() != null) {
      task.getModel().getModeledEntity().getMountManager().dismountAll();
      task.getModel().destroy();
    }
    task.getMount().get().getEntity().remove();
    ownerMap.remove(task.getPlayer().get().getUniqueId());
    if (!task.isCancelled()) {
      task.cancel();
    }
  }

  public void clearAll() {
    for (MountTask task : ownerMap.values()) {
      if (task.getModel() != null) {
        task.getModel().getModeledEntity().getMountManager().dismountAll();
        task.getModel().destroy();
      }
      task.getMount().get().getEntity().remove();
      task.cancel();
    }
    ownerMap.clear();
  }

  public void loadMount(LoadedMount loadedMount) {
    loadedMounts.put(loadedMount.getId(), loadedMount);
  }
}
