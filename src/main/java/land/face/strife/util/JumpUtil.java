package land.face.strife.util;

import com.sentropic.guiapi.gui.Alignment;
import com.sentropic.guiapi.gui.GUIComponent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import land.face.strife.StrifePlugin;
import land.face.strife.data.HoverData;
import land.face.strife.data.StrifeMob;
import land.face.strife.managers.GuiManager;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class JumpUtil {

  private final static Map<UUID, Integer> JUMP_MAP = new HashMap<>();
  private final static Map<Player, HoverData> HOVER_MAP = new WeakHashMap<>();

  public static int getMaxJumps(StrifeMob mob) {
    return mob.getMaxAirJumps();
  }

  public static void setJumps(StrifeMob mob, int amount) {
    if (amount == 0) {
      JUMP_MAP.remove(mob.getEntity().getUniqueId());
      StrifePlugin.getInstance().getGuiManager().updateComponent((Player) mob.getEntity(),
          new GUIComponent("jump-wings", GuiManager.EMPTY, 0, 0, Alignment.LEFT));
    }
    JUMP_MAP.put(mob.getEntity().getUniqueId(), amount);
    amount = Math.min(5, amount);
    int maxAmount = Math.min(5, mob.getMaxAirJumps());
    int empty = maxAmount - amount;
    int totalElements = amount + empty;
    StringBuilder str = new StringBuilder();
    while (amount > 0) {
      str.append("䶰"); // Wing yes
      amount--;
    }
    while (empty > 0) {
      str.append("䎘"); // wing no
      empty--;
    }
    TextComponent textComponent = GuiManager.noShadow(new TextComponent(str.toString()));
    StrifePlugin.getInstance().getGuiManager().updateComponent((Player) mob.getEntity(),
        new GUIComponent("jump-wings", textComponent, totalElements * 13, 86, Alignment.CENTER));
  }

  public static int getJumps(Player player) {
    return JUMP_MAP.getOrDefault(player.getUniqueId(), 0);
  }

  public static double determineHoverPower(Player player) {
    if (!player.isGliding()) {
      HOVER_MAP.remove(player);
      return 0;
    }
    Location checkerLocation = player.getLocation().clone();
    if (!HOVER_MAP.containsKey(player)) {
      while (checkerLocation.getY() > 0 && checkerLocation.getBlock().getType() == Material.AIR) {
        checkerLocation.setY(checkerLocation.getY() - 0.5);
      }
      HoverData data = new HoverData((int) player.getLocation().getX(),
          (int) player.getLocation().getZ(),
          checkerLocation.getY());
      HOVER_MAP.put(player, data);
      return 20 - Math.min(distanceFromHoverGround(player, data), 20);
    }
    HoverData data = HOVER_MAP.get(player);
    if ((int) checkerLocation.getX() == data.getBlockX()
        && (int) checkerLocation.getZ() == data.getBlockZ()) {
      return 20 - Math.min(distanceFromHoverGround(player, data), 20);
    }
    data.setBlockX(player.getLocation().getBlockX());
    data.setBlockZ(player.getLocation().getBlockZ());
    while (checkerLocation.getY() > 0 && checkerLocation.getBlock().getType() == Material.AIR) {
      checkerLocation.setY(checkerLocation.getY() - 0.5);
    }
    data.setGroundBlockY(checkerLocation.getY());
    return 20 - Math.min(distanceFromHoverGround(player, data), 20);
  }

  public static double distanceFromHoverGround(Player player, HoverData data) {
    return player.getLocation().getY() - data.getGroundBlockY();
  }

}
