package land.face.strife.listeners;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.model.ActiveModel;
import com.ticxo.modelengine.api.model.ModeledEntity;
import com.ticxo.modelengine.api.model.bone.BoneBehaviorTypes;
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
        npcModelData.setNameTag(npcSection.getBoolean("show-name-tag", false));
        npcModelData.setScale((float) npcSection.getDouble("scale", 1.0));
        npcModelData.setViewRange((float) npcSection.getDouble("view-range", 36));
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
        if (!ModelEngineAPI.isModeledEntity(npc.getEntity().getUniqueId())) {
          ModeledEntity modeledEntity = ModelEngineAPI.createModeledEntity(npc.getEntity());
          if (modeledEntity != null) {
            ActiveModel activeModel = ModelEngineAPI.createActiveModel(npcModelData.getModel());
            activeModel.setShadowVisible(true);
            modeledEntity.addModel(activeModel, true);
            modeledEntity.setBaseEntityVisible(npcModelData.isShowBaseEntity());
            activeModel.setScale(npcModelData.getScale());
            modeledEntity.getBase().setRenderRadius((int) npcModelData.getViewRange());
            if (npcModelData.isNameTag()) {
              activeModel.getBone("name").ifPresent(modelBone -> {
                modelBone.getBoneBehavior(BoneBehaviorTypes.NAMETAG).ifPresent(head -> {
                  head.setVisible(true);
                  head.setString(npc.getFullName());
                });
              });
            }
          }
        }
      }
    }, 10L);
  }
}