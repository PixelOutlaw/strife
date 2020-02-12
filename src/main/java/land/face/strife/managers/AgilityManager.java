package land.face.strife.managers;

import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.StringUtils;
import io.pixeloutlaw.minecraft.spigot.config.SmartYamlConfiguration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import land.face.strife.StrifePlugin;
import land.face.strife.data.AgilityLocationContainer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

public class AgilityManager {

  private StrifePlugin plugin;
  private SmartYamlConfiguration agilityYML;

  private final Map<String, Set<AgilityLocationContainer>> agilityMap = new HashMap<>();

  public AgilityManager(StrifePlugin plugin, SmartYamlConfiguration agilityYML) {
    this.plugin = plugin;
    this.agilityYML = agilityYML;
  }

  public boolean createAgilityContainer(String name, Location location, float difficulty, float xp) {
    if (!agilityMap.containsKey(location.getWorld().getName())) {
      agilityMap.put(location.getWorld().getName(), new HashSet<>());
    }
    for (AgilityLocationContainer cont : agilityMap.get(location.getWorld().getName())) {
      if (cont.getName().equals(name)) {
        return false;
      }
    }

    AgilityLocationContainer cont = new AgilityLocationContainer();
    cont.setExp(xp);
    cont.setName(name);
    cont.setDifficulty(difficulty);
    cont.getLocations().add(location);

    if (!agilityMap.containsKey(location.getWorld().getName())) {
      agilityMap.put(location.getWorld().getName(), new HashSet<>());
    }
    agilityMap.get(location.getWorld().getName()).add(cont);
    return true;
  }

  public boolean createAgilityContainer(String name, List<Location> locations, float difficulty, float xp) {
    World world = locations.get(0).getWorld();
    if (!agilityMap.containsKey(world.getName())) {
      agilityMap.put(world.getName(), new HashSet<>());
    }
    for (AgilityLocationContainer cont : agilityMap.get(world.getName())) {
      if (cont.getName().equals(name)) {
        return false;
      }
    }

    AgilityLocationContainer cont = new AgilityLocationContainer();
    cont.setExp(xp);
    cont.setName(name);
    cont.setDifficulty(difficulty);
    cont.getLocations().addAll(locations);

    if (!agilityMap.containsKey(world.getName())) {
      agilityMap.put(world.getName(), new HashSet<>());
    }
    agilityMap.get(world.getName()).add(cont);
    return true;
  }

  public boolean addAgilityLocation(String name, Location location) {
    for (AgilityLocationContainer cont : agilityMap.get(location.getWorld().getName())) {
      if (!cont.getName().equals(name)) {
        continue;
      }
      cont.getLocations().add(location);
      return true;
    }
    return false;
  }

  public Set<AgilityLocationContainer> getInWorld(String world) {
    return agilityMap.getOrDefault(world, new HashSet<>());
  }

  public void saveLocations() {
    for (String world : agilityMap.keySet()) {
      for (AgilityLocationContainer cont : agilityMap.get(world)) {
        agilityYML.set(cont.getName() + ".experience", cont.getExp());
        agilityYML.set(cont.getName() + ".difficulty", cont.getDifficulty());
        agilityYML.set(cont.getName() + ".world", cont.getLocations().get(0).getWorld().getName());
        List<String> coords = new ArrayList<>();
        for (Location loc : cont.getLocations()) {
          coords.add(loc.getX() + "~" + loc.getY() + "~" + loc.getZ());
        }
        agilityYML.set(cont.getName() + ".locations", coords);
      }
    }
    agilityYML.save();
  }

  public void loadAgilityContainers() {
    for (String key : agilityYML.getKeys(false)) {
      if (!agilityYML.isConfigurationSection(key)) {
        continue;
      }
      ConfigurationSection cs = agilityYML.getConfigurationSection(key);
      String world = cs.getString("world");
      if (StringUtils.isBlank(world)) {
        Bukkit.getLogger().warning("Cannot load agility container with blank world");
        continue;
      }
      World realWorld = Bukkit.getServer().getWorld(world);
      if (realWorld == null) {
        Bukkit.getLogger().warning("Cannot load agility container with invalid world");
        continue;
      }
      List<String> locs = cs.getStringList("locations");
      float xp = (float) cs.getDouble("experience", 1);
      float difficulty = (float) cs.getDouble("difficulty", 10);
      List<Location> locations = new ArrayList<>();
      for (String s : locs) {
        String[] coords = s.split("~");
        Location location = new Location(realWorld, Double.parseDouble(coords[0]),
            Double.parseDouble(coords[1]), Double.parseDouble(coords[2]));
        locations.add(location);
      }
      createAgilityContainer(key, locations, difficulty, xp);
    }
  }
}
