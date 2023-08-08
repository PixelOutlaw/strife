package land.face.strife.listeners;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ModeledEntity;
import java.util.HashMap;
import java.util.Map;
import land.face.strife.StrifePlugin;
import land.face.strife.data.NpcModelData;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.event.SpawnReason;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class CitizenModelListener implements Listener {

  private final StrifePlugin plugin;
  private final Map<Integer, NpcModelData> npcModelMap = new HashMap<>();

  public CitizenModelListener(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onNpcSpawn(NPCSpawnEvent event) {
    if (event.isCancelled() || event.getReason() != SpawnReason.CHUNK_LOAD) {
      return;
    }
    NpcModelData npcModelData = npcModelMap.get(event.getNPC().getId());
    if (npcModelData != null) {
      applyModel(event.getNPC(), npcModelData);
    }
  }

  public void reloadModels(ConfigurationSection section) {
    for (String key : section.getKeys(false)) {
      try {
        ConfigurationSection npcSection = section.getConfigurationSection(key);
        NpcModelData npcModelData = new NpcModelData();
        npcModelData.setModel(npcSection.getString("model-id"));
        npcModelData.setShowBaseEntity(npcSection.getBoolean("show-base-entity", false));
        npcModelMap.put(Integer.valueOf(key), npcModelData);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    Bukkit.getLogger().info("[Strife] Loaded " + npcModelMap.size() + " npc models");
    for (int i : npcModelMap.keySet()) {
      NPC npc = CitizensAPI.getNPCRegistry().getById(i);
      if (!npc.isSpawned()) {
        continue;
      }
      NpcModelData npcModelData = npcModelMap.get(i);
      applyModel(npc, npcModelData);
    }
  }

  private void applyModel(NPC npc, NpcModelData npcModelData) {
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      if (npc.getEntity() != null && npc.getEntity().isValid()) {
        if (!ModelEngineAPI.getModelTicker().isModeledEntity(npc.getEntity().getUniqueId())) {
          ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(npc.getEntity());
          if (modeledEntity != null) {
            modeledEntity.addModel(ModelEngineAPI.createActiveModel(npcModelData.getModel()), true);
            modeledEntity.setBaseEntityVisible(npcModelData.isShowBaseEntity());
          }
        }
      }
    }, 10L);
  }
}