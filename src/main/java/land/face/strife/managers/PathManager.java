package land.face.strife.managers;

import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import io.pixeloutlaw.minecraft.spigot.hilt.ItemStackExtensionsKt;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import land.face.strife.data.LevelPath;
import land.face.strife.data.LevelPath.Choice;
import land.face.strife.data.LevelPath.Path;
import land.face.strife.data.champion.Champion;
import land.face.strife.data.champion.ChampionSaveData;
import land.face.strife.stats.StrifeStat;
import land.face.strife.stats.StrifeTrait;
import land.face.strife.util.LogUtil;
import land.face.strife.util.StatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class PathManager {

  private final Map<Path, Map<Choice, LevelPath>> pathMegaMap;

  public PathManager() {
    pathMegaMap = new HashMap<>();
    for (Path path : LevelPath.PATH_VALUES) {
      pathMegaMap.put(path, new HashMap<>());
    }
  }

  public LevelPath getLevelPath(Path path, Choice choice) {
    return pathMegaMap.get(path).get(choice);
  }

  public ItemStack getIcon(Path path, Choice choice) {
    return pathMegaMap.get(path).get(choice).getDisplayItem().clone();
  }

  public Map<StrifeStat, Float> getStats(Path path, Choice choice) {
    return pathMegaMap.get(path).get(choice).getStats();
  }

  public void buildPathBonus(Champion champion) {
    ChampionSaveData data = champion.getSaveData();
    Map<StrifeStat, Float> stats = new HashMap<>();
    Set<StrifeTrait> traits = new HashSet<>();
    for (Path path : data.getPathMap().keySet()) {
      LevelPath levelPath = pathMegaMap.get(path).get(data.getPathMap().get(path));
      stats.putAll(StatUpdateManager.combineMaps(stats, levelPath.getStats()));
      traits.addAll(levelPath.getTraits());
    }
    champion.setPathStats(stats);
    champion.setPathTraits(traits);
  }

  public void loadPath(String key, ConfigurationSection cs) {
    if (cs == null) {
      return;
    }
    Path path;
    try {
      path = Path.valueOf(key);
    } catch (Exception e) {
      Bukkit.getLogger().warning("Invalid path key " + key);
      return;
    }
    pathMegaMap.get(path).put(Choice.OPTION_1, buildChoiceSection(cs.getConfigurationSection("option-one")));
    pathMegaMap.get(path).put(Choice.OPTION_2, buildChoiceSection(cs.getConfigurationSection("option-two")));
    pathMegaMap.get(path).put(Choice.OPTION_3, buildChoiceSection(cs.getConfigurationSection("option-three")));
    LogUtil.printDebug("Loaded path " + key);
  }

  private LevelPath buildChoiceSection(ConfigurationSection choiceSection) {

    if (choiceSection == null) {
      Bukkit.getLogger().warning("Null choice segment!");
      return null;
    }

    String itemName = choiceSection.getString("name", "NO PATH NAME");
    List<String> itemLore = choiceSection.getStringList("lore");

    Material material;
    try {
      material = Material.getMaterial(choiceSection.getString("material"));
    } catch (Exception e) {
      material = Material.DIRT;
      LogUtil.printWarning("Invalid choice material!");
    }

    assert material != null;
    ItemStack stack = new ItemStack(material);

    ItemStackExtensionsKt.setDisplayName(stack,  StringExtensionsKt.chatColorize(itemName));
    List<String> iconLore = new ArrayList<>(itemLore);
    iconLore.add("&8&oYou may only choose");
    iconLore.add("&8&oone of these paths...");
    iconLore.add("&8&oPick wisely!");
    TextUtils.setLore(stack, iconLore, true);
    ItemStackExtensionsKt.setCustomModelData(stack, choiceSection.getInt("model-data", 0));

    Map<StrifeStat, Float> statMap = StatUtil.getStatMapFromSection(choiceSection.getConfigurationSection("stats"));

    Set<StrifeTrait> traits = new HashSet<>();
    List<String> traitStrings = choiceSection.getStringList("traits");

    for (String s : traitStrings) {
      try {
        traits.add(StrifeTrait.valueOf(s));
      } catch (Exception e) {
        Bukkit.getLogger().warning("Invalid path trait " + s);
      }
    }

    return new LevelPath(stack, itemLore, statMap, traits);
  }

}
