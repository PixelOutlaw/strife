package land.face.strife.util;

import land.face.strife.StrifePlugin;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.PlayerWatcher;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.inventivetalent.glow.GlowAPI;

public class GlowUtil {

  public static void setGlow(Player viewer, Entity target, GlowAPI.Color color) {
    if (target == null) {
      return;
    }
    if (!StrifePlugin.getInstance().getChampionManager().getChampion(viewer).getSaveData()
        .isGlowEnabled()) {
      return;
    }
    if (color == null) {
      GlowAPI.setGlowing(target, false, viewer);
      return;
    }
    GlowAPI.setGlowing(target, true, viewer);
    Disguise disguise = DisguiseAPI.getDisguise(target);
    if (disguise instanceof PlayerDisguise) {
      PlayerWatcher watcher = ((PlayerDisguise) disguise).getWatcher();
      watcher.setGlowColor(ChatColor.valueOf(color.name()));
    }
    GlowAPI.setGlowing(target, color, viewer);
  }

}
