package info.faceland.strife.util;

import info.faceland.strife.StrifePlugin;
import org.bukkit.entity.Player;

public class PlayerDataUtil {
  private final StrifePlugin plugin;

  public PlayerDataUtil(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  public int getCraftLevel(Player player) {
    return plugin.getChampionManager().getChampion(player.getUniqueId()).getSaveData().getCraftingLevel();
  }

  public int getMaxItemDestroyLevel(Player player) {
    return getMaxItemDestroyLevel(plugin.getChampionManager().getChampion(player.getUniqueId()).getCraftingLevel());
  }

  public int getMaxItemDestroyLevel(int craftLvl) {
    return 10 + (int)Math.floor((double)craftLvl/3) * 5;
  }

  public int getMaxCraftItemLevel(Player player) {
    return getMaxCraftItemLevel(plugin.getChampionManager().getChampion(player.getUniqueId()).getCraftingLevel());
  }

  public int getMaxCraftItemLevel(int craftLvl) {
    return 5 + (int)Math.floor((double)craftLvl/5) * 8;
  }
}
