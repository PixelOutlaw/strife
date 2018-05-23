package info.faceland.strife.util;

import info.faceland.strife.StrifePlugin;
import info.faceland.strife.events.StrifeCraftEvent;
import info.faceland.strife.events.StrifeEnchantEvent;
import info.faceland.strife.events.StrifeFishEvent;
import org.bukkit.entity.Player;

public class SkillExperienceUtil {

  public static void addCraftExperience(Player player, double amount) {
    StrifeCraftEvent craftEvent = new StrifeCraftEvent(player, (float) amount);
    StrifePlugin.getInstance().getServer().getPluginManager().callEvent(craftEvent);
    StrifePlugin.getInstance().getCraftExperienceManager().addCraftExperience(player, craftEvent.getAmount());
  }

  public static void addEnchantExperience(Player player, double amount) {
    StrifeEnchantEvent enchantEvent = new StrifeEnchantEvent(player, (float) amount);
    StrifePlugin.getInstance().getServer().getPluginManager().callEvent(enchantEvent);
    StrifePlugin.getInstance().getEnchantExperienceManager().addExperience(player, enchantEvent.getAmount());
  }

  public static void addFishExperience(Player player, double amount) {
    StrifeFishEvent fishEvent = new StrifeFishEvent(player, (float) amount);
    StrifePlugin.getInstance().getServer().getPluginManager().callEvent(fishEvent);
    StrifePlugin.getInstance().getFishExperienceManager().addExperience(player, fishEvent.getAmount());
  }

}

