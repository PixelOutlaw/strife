package land.face.strife.data.champion;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import java.awt.Color;
import land.face.strife.data.StrifeMob;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;

public enum LifeSkillType {

  CRAFTING("Crafting", "crafting", "冷", FaceColor.YELLOW.getColor()),
  ENCHANTING("Enchanting", "enchant", "冽", ChatColor.of(new Color(113, 79, 236))),
  FISHING("Fishing", "fishing", "冾", FaceColor.CYAN.getColor()),
  MINING("Mining", "mining", "凄", FaceColor.GRAY.getColor()),
  FARMING("Gathering", "farming", "况", FaceColor.ORANGE.getColor()),
  COOKING("Cooking", "cooking", "净", FaceColor.BROWN.getColor()),
  ALCHEMY("Alchemy", "alchemy", "冿", FaceColor.TEAL.getColor()),
  SNEAK("Sneak", "sneak", "凃", FaceColor.GRAY.getColor()),
  AGILITY("Agility", "agility", "凅", FaceColor.TEAL.getColor()),
  TRADING("Trading", "trading", "凁", FaceColor.GREEN.getColor()),
  FLYING("Flying", "flying", "冶", ChatColor.of(new Color(114, 187, 255))),
  PRAYER("Prayer", "prayer", "凂", ChatColor.of(new Color(202, 255, 245))),

  SWORDSMANSHIP("Swordsmanship", "sword", "冰", FaceColor.ORANGE.getColor(), true),
  DAGGER_MASTERY("Dagger Mastery", "dagger", "凇", FaceColor.LIME.getColor(), true),
  AXE_MASTERY("Axe Mastery", "axe", "冱", FaceColor.RED.getColor(), true),
  BLUNT_WEAPONS("Blunt Weapons", "blunt", "冸", FaceColor.YELLOW.getColor(), true),
  DUAL_WIELDING("Dual Wielding", "dual", "冴", FaceColor.LIME.getColor(), true),
  SHIELD_MASTERY("Shield Mastery", "shield", "准", FaceColor.BROWN.getColor(), true),
  ARCHERY("Archery", "archery", "冲", FaceColor.GREEN.getColor(), true),
  MARKSMANSHIP("Marksmanship", "marksmanship", "决", FaceColor.LIME.getColor(), true),
  ARCANE_MAGICS("Arcane Magics", "arcane-magic", "冻", FaceColor.BLUE.getColor(), true),
  NATURAL_MAGICS("Natural Magics", "natural-magic", "冺", FaceColor.GREEN.getColor(), true),
  BLACK_MAGICS("Black Magics", "dark-magics", "冹", FaceColor.PURPLE.getColor(), true),
  CELESTIAL_MAGICS("Holy Magic", "light-magic", "冼", FaceColor.WHITE.getColor(), true);

  public final static LifeSkillType[] types = LifeSkillType.values();

  @Getter
  private final String prettyName;
  @Getter
  private final String dataName;
  @Getter
  private final String character;
  @Getter
  private final ChatColor color;
  @Getter
  private final boolean combat;

  LifeSkillType(String prettyName, String dataName, String character, ChatColor color, boolean combat) {
    this.prettyName = prettyName;
    this.dataName = dataName;
    this.character = character;
    this.color = color;
    this.combat = combat;
  }

  LifeSkillType(String prettyName, String dataName, String character, ChatColor color) {
    this.prettyName = prettyName;
    this.dataName = dataName;
    this.character = character;
    this.color = color;
    this.combat = false;
  }
}

