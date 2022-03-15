package land.face.strife.util;

import land.face.strife.StrifePlugin;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.FlagWatcher;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.inventivetalent.glow.GlowAPI;

public class GlowUtil {

  public static void setGlow(Player viewer, Entity target, ChatColor chatColor) {
    if (target == null || !StrifePlugin.isGlowEnabled()) {
      return;
    }
    if (!StrifePlugin.getInstance().getChampionManager().getChampion(viewer).getSaveData()
        .isGlowEnabled()) {
      return;
    }
    GlowAPI.Color color = null;
    if (chatColor != null) {
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
      watcher.setGlowColor(ChatColor.valueOf(color.name()));
    }
  }
}
