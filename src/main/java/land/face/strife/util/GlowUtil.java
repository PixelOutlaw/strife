package land.face.strife.util;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class GlowUtil {

  public static void setGlow(Player viewer, Entity target, ChatColor chatColor) {
    /*
    if (target == null || !StrifePlugin.isGlowEnabled()) {
      return;
    }
    if (!StrifePlugin.getInstance().getChampionManager().getChampion(viewer).getSaveData()
        .isGlowEnabled()) {
      return;
    }
    Glow glow;
    if (chatColor != null) {
      glow = Glow.builder().color(chatColor).name("sneed").build();
      color = GlowAPI.Color.valueOf(chatColor.toString());
    }
    if (color == null || (target instanceof LivingEntity
        && ((LivingEntity) target).hasPotionEffect(PotionEffectType.INVISIBILITY))) {
      GlowAPI.setGlowing(target, false, viewer);
      return;
    }
    GlowAPI.setGlowing(target, true, viewer);
    GlowAPI.setGlowing(target, color, viewer);

    Disguise disguise = DisguiseAPI.getDisguise(target);
    if (disguise != null) {
      FlagWatcher watcher = disguise.getWatcher();
      watcher.setGlowColor(chatColor);
    }
     */
  }
}
