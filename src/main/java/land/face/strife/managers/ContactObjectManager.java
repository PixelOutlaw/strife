package land.face.strife.managers;

import com.tealcube.minecraft.bukkit.facecore.utilities.ChunkUtil;
import io.pixeloutlaw.minecraft.spigot.config.SmartYamlConfiguration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import land.face.strife.StrifePlugin;
import land.face.strife.data.ContactPrefab;
import land.face.strife.data.ContactSpawner;
import land.face.strife.data.pojo.KeyframeParticle;
import land.face.strife.data.pojo.KeyframeSound;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class ContactObjectManager {

  private StrifePlugin plugin;
  private final SmartYamlConfiguration spawnsYaml;
  private final Map<String, ContactPrefab> contactPrefabsMap = new HashMap<>();
  private final Map<String, ContactSpawner> contactSpawners = new HashMap<>();

  public ContactObjectManager(StrifePlugin plugin, SmartYamlConfiguration spawnerYaml) {
    this.plugin = plugin;
    this.spawnsYaml = spawnerYaml;
  }

  public ContactPrefab getPrefab(String id) {
    return contactPrefabsMap.getOrDefault(id, null);
  }

  public ContactSpawner getSpawner(String id) {
    return contactSpawners.getOrDefault(id, null);
  }

  public void loginSync(Player player) {
    for (var s : contactSpawners.values()) {
      s.processPlayer(player);
    }
  }

  public void deleteSpawner(String id) {
    ContactSpawner spawner = contactSpawners.get(id);
    if (spawner != null) {
      spawner.destroy();
      contactSpawners.remove(id);
    }
  }

  public void createSpawner(String id, ContactPrefab prefab, Location location, int cooldownTicks, List<Integer> args) {
    var spawner = new ContactSpawner(id, prefab, location);
    spawner.setLocation(location);
    spawner.setChunkKey(ChunkUtil.buildChunkKey(spawner.getLocation().getChunk()));
    spawner.setCooldownTicks(cooldownTicks);
    spawner.setVariables(args);
    contactSpawners.put(id, spawner);
  }

  public void destroyAll() {
    for (var co : contactSpawners.values()) {
      co.destroy();
    }
  }

  public void loadPrefabs(SmartYamlConfiguration spawnsYaml) {
    contactPrefabsMap.clear();
    for (String key : spawnsYaml.getKeys(false)) {
      if (!spawnsYaml.isConfigurationSection(key)) {
        continue;
      }
      ConfigurationSection cs = spawnsYaml.getConfigurationSection(key);
      var prefab = new ContactPrefab(
          key,
          cs.getString("active-model", null),
          cs.getString("inactive-model", null),
          cs.getString("contact-animation", "contact"),
          cs.getInt("contact-model-swap-delay", 20),
          Math.pow(cs.getDouble("hitbox-size", 2), 2),
          cs.getDouble("effect-offset", 0),
          cs.getStringList("commands"),
          buildParticles(cs.getStringList("particles")),
          buildSounds(cs.getStringList("sounds"))
      );
      contactPrefabsMap.put(key, prefab);
    }
  }

  private List<KeyframeParticle> buildParticles(List<String> list) {
    List<KeyframeParticle> response = new ArrayList<>();
    for (String s : list) {
      try {
        String[] strs = s.split("\\|");
        if (strs.length != 4) {
          continue;
        }
        var particle = new KeyframeParticle();
        particle.setParticle(strs[0]);
        particle.setAmount(Integer.parseInt(strs[1]));
        particle.setSpread(Double.parseDouble(strs[2]));
        particle.setSpeed(Double.parseDouble(strs[3]));
        response.add(particle);
      } catch (Exception ignored) {

      }
    }
    return response;
  }

  private List<KeyframeSound> buildSounds(List<String> list) {
    List<KeyframeSound> response = new ArrayList<>();
    for (String s : list) {
      try {
        String[] strs = s.split("\\|");
        if (strs.length != 3) {
          continue;
        }
        var sound = new KeyframeSound();
        sound.setSound(strs[0]);
        sound.setVolume((float) Double.parseDouble(strs[1]));
        sound.setPitch((float) Double.parseDouble(strs[2]));
        response.add(sound);
      } catch (Exception ignored) {

      }
    }
    return response;
  }

  public void loadSpawners(SmartYamlConfiguration spawnsYaml) {
    contactSpawners.clear();
    for (String key : spawnsYaml.getKeys(false)) {
      if (!spawnsYaml.isConfigurationSection(key)) {
        continue;
      }
      ConfigurationSection cs = spawnsYaml.getConfigurationSection(key);
      var spawner = new ContactSpawner(key, contactPrefabsMap.get(cs.getString("prefab")), cs.getLocation("location"));
      spawner.setLocation(cs.getLocation("location"));
      spawner.setChunkKey(ChunkUtil.buildChunkKey(spawner.getLocation().getChunk()));
      spawner.setCooldownTicks(cs.getInt("cooldown-ticks", 30 * 20));
      spawner.setVariables(cs.getIntegerList("variables"));

      contactSpawners.put(key, spawner);
    }
  }

  public void saveLocations() {
    for (var entry : contactSpawners.entrySet()) {
      Bukkit.getLogger().info("[Strife] Saving contact object " + entry.getKey());
      spawnsYaml.set(entry.getKey() + ".cooldown-ticks", entry.getValue().getCooldownTicks());
      spawnsYaml.set(entry.getKey() + ".variables", entry.getValue().getVariables());
      spawnsYaml.set(entry.getKey() + ".prefab", entry.getValue().getPrefab().getId());
      spawnsYaml.set(entry.getKey() + ".location", entry.getValue().getLocation());
    }
    spawnsYaml.save();
  }
}
