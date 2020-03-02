package land.face.strife.listeners;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

  @EventHandler(priority = EventPriority.LOWEST)
  public void onChat(AsyncPlayerChatEvent event) {
    String msg = ChatColor.stripColor(event.getMessage());
    if (event.isCancelled() || !ChatColor.stripColor(msg).startsWith("==ability==")) {
      return;
    }
    event.setMessage(event.getMessage().replace("==ability==", ""));
    List<Player> invalidPlayers = new ArrayList<>();
    for (Player p : event.getRecipients()) {
      if (event.getPlayer().getWorld() != p.getWorld()) {
        invalidPlayers.add(p);
        continue;
      }
      if (event.getPlayer().getLocation().distanceSquared(p.getLocation()) > 256) {
        invalidPlayers.add(p);
      }
    }
    event.getRecipients().removeAll(invalidPlayers);
  }
}
