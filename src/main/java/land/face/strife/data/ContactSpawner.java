package land.face.strife.data;

import com.tealcube.minecraft.bukkit.facecore.utilities.ChunkUtil;
import com.tealcube.minecraft.bukkit.facecore.utilities.MoveUtil;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.entity.Dummy;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import land.face.strife.StrifePlugin;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

@Getter
@Setter
public class ContactSpawner {

  private final String id;
  private final ContactPrefab prefab;

  private Location location;
  private Dummy<?> activeDummy;
  private Dummy<?> inactiveDummy;
  private ActiveModel activeModel;
  private ActiveModel inactiveModel;
  private int cooldownTicks;
  private List<Integer> variables;
  private Set<UUID> inactivePlayers = new HashSet<>();
  private String chunkKey = "";
  private BukkitTask detectionTask;
  private int tickId = 0;
  private Set<Player> tempPlayers = new HashSet<>();
  private boolean destroyed = false;

  private static Map<UUID, String> LAST_COIN = new HashMap<>();

  public ContactSpawner(String id, ContactPrefab prefab, Location location) {
    this.id = id;
    this.prefab = prefab;
    this.location = location;
    create();
  }

  public void create() {
    activeDummy = new Dummy<>();
    activeDummy.setLocation(location);
    ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(activeDummy);
    activeModel = ModelEngineAPI.createActiveModel(prefab.getActiveModel());
    modeledEntity.addModel(activeModel, true);
    activeDummy.getBodyRotationController().setRotationDuration(0);
    activeDummy.getBodyRotationController().setRotationDelay(0);
    activeDummy.setYBodyRot(location.getYaw());
    activeDummy.setYHeadRot(location.getYaw());
    if (StringUtils.isNotBlank(prefab.getInactiveModel())) {
      inactiveDummy = new Dummy<>();
      inactiveDummy.setLocation(location);
      ModeledEntity modeledEntity2 = ModelEngineAPI.createModeledEntity(inactiveDummy);
      inactiveModel = ModelEngineAPI.createActiveModel(prefab.getInactiveModel());
      modeledEntity2.addModel(inactiveModel, true);
      inactiveDummy.getBodyRotationController().setRotationDuration(0);
      inactiveDummy.getBodyRotationController().setRotationDelay(0);
      inactiveDummy.setYBodyRot(location.getYaw());
      inactiveDummy.setYHeadRot(location.getYaw());
    } else {
      inactiveDummy = null;
      inactiveModel = null;
    }
    for (var p : Bukkit.getOnlinePlayers()) {
      processPlayer(p);
    }
    detectionTask = Bukkit.getScheduler().runTaskTimer(StrifePlugin.getInstance(), this::detect, 22L, 1L);
    destroyed = false;
  }

  public void contact(Player player) {
    if (inactivePlayers.contains(player.getUniqueId())) {
      return;
    }
    inactivePlayers.add(player.getUniqueId());
    if (!id.equals(LAST_COIN.getOrDefault(player.getUniqueId(), "")) && MoveUtil.hasMoved(player, 1000 * 60)) {
      executeCommands(player);
    }
    var effectLoc = location.clone();
    effectLoc.add(0, prefab.getEffectOffset(), 0);
    for (var s : prefab.getSounds()) {
      s.play(player, effectLoc);
    }
    for (var s : prefab.getParticles()) {
      s.play(player, effectLoc);
    }
    LAST_COIN.put(player.getUniqueId(), id);

    activeDummy.setForceHidden(player, true);

    var tempDummy = new Dummy<>();
    tempDummy.setLocation(location);
    tempDummy.setDetectingPlayers(false);
    ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(tempDummy);
    var tempModel = ModelEngineAPI.createActiveModel(prefab.getActiveModel());
    modeledEntity.addModel(tempModel, true);
    tempDummy.getBodyRotationController().setRotationDuration(0);
    tempDummy.getBodyRotationController().setRotationDelay(0);
    tempDummy.setYBodyRot(location.getYaw());
    tempDummy.setYHeadRot(location.getYaw());
    tempDummy.setForceViewing(player, true);
    tempModel.getAnimationHandler().playAnimation(prefab.getContactAnimation(), 0, 0, 1, true);

    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), () -> {
      tempDummy.setRemoved(true);
      processPlayer(player);
    }, prefab.getModelSwapDelay());

    Bukkit.getScheduler().runTaskLater(StrifePlugin.getInstance(), (task) -> {
      inactivePlayers.remove(player.getUniqueId());
      processPlayer(player);
    }, cooldownTicks);
  }

  public void processPlayer(Player p) {
    if (destroyed) {
      return;
    }
    if (inactivePlayers.contains(p.getUniqueId())) {
      activeDummy.setForceHidden(p, true);
      if (inactiveDummy != null) {
        inactiveDummy.setForceHidden(p, false);
      }
    } else {
      activeDummy.setForceHidden(p, false);
      if (inactiveDummy != null) {
        inactiveDummy.setForceHidden(p, true);
      }
    }
  }

  public void detect() {
    if (tickId == 40) {
      tempPlayers.clear();
      if (ChunkUtil.isChuckLoaded(chunkKey)) {
        tempPlayers.addAll(location.getWorld().getNearbyPlayers(location, 80));
        tempPlayers.removeIf(p -> inactivePlayers.contains(p.getUniqueId()));
      }
      tickId = StrifePlugin.RNG.nextInt(6);
    }
    for (var p : tempPlayers) {
      if (p.getWorld() != location.getWorld()) {
        continue;
      }
      if (p.getLocation().distanceSquared(location) < prefab.getContactSize()) {
        contact(p);
      }
    }
    tickId++;
  }

  public void executeCommands(Player player) {
    for (String commandString : prefab.getCommands()) {
      commandString = commandString.replace("%player%", player.getName());
      commandString = commandString.replace("%x%", Double.toString(location.getX()));
      commandString = commandString.replace("%y%", Double.toString(location.getY()));
      commandString = commandString.replace("%z%", Double.toString(location.getZ()));
      int index = 1;
      for (var i : variables) {
        String placeholder = "%var" + index + "%";
        commandString = commandString.replace(placeholder, i.toString());
        index++;
      }
      Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), commandString);
    }
  }

  public void destroy() {
    if (activeDummy != null) {
      activeDummy.setRemoved(true);
    }
    if (inactiveDummy != null) {
      inactiveDummy.setRemoved(true);
    }
    if (detectionTask != null && !detectionTask.isCancelled()) {
      detectionTask.cancel();
    }
    destroyed = true;
  }
}
