package land.face.strife.data;

import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import io.pixeloutlaw.minecraft.spigot.config.SmartYamlConfiguration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class LoreBooks {

  private static final Map<String, List<String>> GROUPS = new HashMap<>();
  private static final Map<String, ItemStack> BOOKS = new HashMap<>();

  public static void buildStuff(SmartYamlConfiguration smartYamlConfiguration) {
    GROUPS.clear();
    BOOKS.clear();
    for (String key : smartYamlConfiguration.getKeys(false)) {
      if (!smartYamlConfiguration.isConfigurationSection(key)) {
        continue;
      }
      if ("sections".equals(key)) {
        var cs = smartYamlConfiguration.getConfigurationSection(key);
        for (String key2 : cs.getKeys(false)) {
          addGroup(key2, cs.getStringList(key2));
        }
      }
      if ("books".equals(key)) {
        var cs = smartYamlConfiguration.getConfigurationSection(key);
        for (String key2 : cs.getKeys(false)) {
          addBook(key2, cs.getStringList(key2));
        }
      }
    }
  }

  public static void openBook(Player player, String id) {
    var options = GROUPS.getOrDefault(id, null);
    if (options == null) {
      Bukkit.getLogger().info("[Strife] Failed to open lorebook category " + id + " for " + player);
      return;
    }
    Collections.shuffle(options);
    var selection = options.getFirst();
    var book = BOOKS.getOrDefault(selection, null);
    if (book == null) {
      Bukkit.getLogger().info("[Strife] Failed to open lorebook " + selection + " for " + player);
      return;
    }
    player.openBook(book);
  }

  public static void addGroup(String key, List<String> strs) {
    GROUPS.put(key, strs);
  }

  public static void addBook(String key, List<String> pages) {
    ItemStack stack = new ItemStack(Material.WRITTEN_BOOK);
    var meta = (BookMeta) stack.getItemMeta();
    meta.setAuthor("Faceguy");
    meta.setTitle("sneed");
    for (String s : pages) {
      meta.addPage(PaletteUtil.color(s));
    }
    stack.setItemMeta(meta);
    BOOKS.put(key, stack);
  }

}
