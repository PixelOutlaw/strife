package land.face.strife.util;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.gmail.filoghost.holographicdisplays.api.VisibilityManager;
import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import land.face.strife.StrifePlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class SoulUtil {

  public static Hologram createSoul(Player player, String text, Location location) {
    Hologram hologram = HologramsAPI.createHologram(StrifePlugin.getInstance(), location);
    hologram.clearLines();
    ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
    SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
    skullMeta.setOwningPlayer(player);
    itemStack.setItemMeta(skullMeta);

    hologram.appendTextLine(StringExtensionsKt.chatColorize(text));
    hologram.appendItemLine(itemStack);
    VisibilityManager visibilityManager = hologram.getVisibilityManager();
    visibilityManager.setVisibleByDefault(false);

    return hologram;
  }
}
