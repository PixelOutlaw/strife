package land.face.strife.data.champion;

import java.awt.Color;
import net.md_5.bungee.api.ChatColor;

public enum LifeSkillType {

  CRAFTING("Crafting", "crafting", ChatColor.YELLOW),
  ENCHANTING("Enchanting", "enchant", ChatColor.LIGHT_PURPLE),
  FISHING("Fishing", "fishing", ChatColor.AQUA),
  MINING("Mining", "mining", ChatColor.DARK_GREEN),
  FARMING("Farming", "farming", ChatColor.GOLD),
  COOKING("Cooking", "cooking", ChatColor.YELLOW),
  ALCHEMY("Alchemy", "alchemy", ChatColor.GREEN),
  SNEAK("Sneak", "sneak", ChatColor.GRAY),
  AGILITY("Agility", "agility", ChatColor.DARK_AQUA),
  TRADING("Trading", "trading", ChatColor.DARK_GREEN),
  SWORDSMANSHIP("Swordsmanship", "sword", ChatColor.RED),
  AXE_MASTERY("Axe Mastery", "axe", ChatColor.RED),
  BLUNT_WEAPONS("Blunt Weapons", "blunt", ChatColor.RED),
  DUAL_WIELDING("Dual Wielding", "dual", ChatColor.GREEN),
  SHIELD_MASTERY("Shield Mastery", "shield", ChatColor.YELLOW),
  ARCHERY("Archery", "archery", ChatColor.DARK_GREEN),
  MARKSMANSHIP("Marksmanship", "marksmanship", ChatColor.DARK_GREEN),
  ARCANE_MAGICS("Arcane Magics", "arcane-magic", ChatColor.BLUE),
  NATURAL_MAGICS("Natural Magics", "natural-magic", ChatColor.DARK_AQUA),
  BLACK_MAGICS("Black Magics", "dark-magics", ChatColor.DARK_PURPLE),
  CELESTIAL_MAGICS("Celestial Magics", "light-magic", ChatColor.of(new Color(249, 233, 226)));

  public final static LifeSkillType[] types = LifeSkillType.values();

  private final String prettyName;
  private final String dataName;
  private final ChatColor color;

  LifeSkillType(String prettyName, String dataName, ChatColor color) {
    this.prettyName = prettyName;
    this.dataName = dataName;
    this.color = color;
  }

  public String getName() {
    return prettyName;
  }

  public String getDataName() {
    return dataName;
  }

  public ChatColor getColor() {
    return color;
  }
}
