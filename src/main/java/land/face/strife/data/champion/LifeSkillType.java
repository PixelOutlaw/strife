package land.face.strife.data.champion;

import java.awt.Color;
import net.md_5.bungee.api.ChatColor;

public enum LifeSkillType {

  CRAFTING("Crafting", "crafting", ChatColor.YELLOW),
  ENCHANTING("Enchanting", "enchant", ChatColor.of(new Color(128, 93, 255))),
  FISHING("Fishing", "fishing", ChatColor.AQUA),
  MINING("Mining", "mining", ChatColor.DARK_GREEN),
  FARMING("Gathering", "farming", ChatColor.of(new Color(255, 192, 87))),
  COOKING("Cooking", "cooking", ChatColor.of(new Color(219, 117, 74))),
  ALCHEMY("Alchemy", "alchemy", ChatColor.GREEN),
  SNEAK("Sneak", "sneak", ChatColor.GRAY),
  AGILITY("Agility", "agility", ChatColor.DARK_AQUA),
  TRADING("Trading", "trading", ChatColor.DARK_GREEN),
  SWORDSMANSHIP("Swordsmanship", "sword", ChatColor.RED, true),
  DAGGER_MASTERY("Dagger Mastery", "dagger", ChatColor.of(new Color(204, 246, 102)), true),
  AXE_MASTERY("Axe Mastery", "axe", ChatColor.RED, true),
  BLUNT_WEAPONS("Blunt Weapons", "blunt", ChatColor.RED, true),
  DUAL_WIELDING("Dual Wielding", "dual", ChatColor.GREEN, true),
  SHIELD_MASTERY("Shield Mastery", "shield", ChatColor.YELLOW, true),
  ARCHERY("Archery", "archery", ChatColor.DARK_GREEN, true),
  MARKSMANSHIP("Marksmanship", "marksmanship", ChatColor.of(new Color(99, 231, 113)), true),
  ARCANE_MAGICS("Arcane Magics", "arcane-magic", ChatColor.BLUE, true),
  NATURAL_MAGICS("Natural Magics", "natural-magic", ChatColor.DARK_AQUA, true),
  BLACK_MAGICS("Black Magics", "dark-magics", ChatColor.DARK_PURPLE, true),
  CELESTIAL_MAGICS("Celestial Magics", "light-magic", ChatColor.of(new Color(255, 235, 214)), true);

  public final static LifeSkillType[] types = LifeSkillType.values();

  private final String prettyName;
  private final String dataName;
  private final ChatColor color;
  private final boolean combat;

  LifeSkillType(String prettyName, String dataName, ChatColor color, boolean combat) {
    this.prettyName = prettyName;
    this.dataName = dataName;
    this.color = color;
    this.combat = combat;
  }

  LifeSkillType(String prettyName, String dataName, ChatColor color) {
    this.prettyName = prettyName;
    this.dataName = dataName;
    this.color = color;
    this.combat = false;
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

  public boolean isComnbat() {
    return combat;
  }
}
