package land.face.strife.util;

public class GlowUtil {

  /*
  public static void setGlow(Player viewer, Entity target, ChatColor color) {
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
  */

}
