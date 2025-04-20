package land.face.strife.data.champion;

import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.LIME;
import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.color;
import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.noShadow;
import static com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor.reShadow;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import java.awt.Color;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.md_5.bungee.api.ChatColor;

@Getter
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
  LOREMASTER("Loremaster", "lorem", "拯", ChatColor.of(new Color(157, 107, 246))),

  SWORDSMANSHIP("Swordsmanship", "sword", "冰", FaceColor.ORANGE.getColor(), true),
  DAGGER_MASTERY("Dagger Mastery", "dagger", "凇", LIME.getColor(), true),
  AXE_MASTERY("Axe Mastery", "axe", "冱", FaceColor.RED.getColor(), true),
  BLUNT_WEAPONS("Blunt Weapons", "blunt", "冸", FaceColor.YELLOW.getColor(), true),
  SPEAR_MASTERY("Spear Mastery", "spear", "拰", FaceColor.ORANGE.getColor(), true),
  FISTING("Fisticuffs", "fisting", "拦", FaceColor.BROWN.getColor(), true),
  DUAL_WIELDING("Dual Wielding", "dual", "冴", LIME.getColor(), true),
  SHIELD_MASTERY("Shield Mastery", "shield", "准", FaceColor.BROWN.getColor(), true),
  ARCHERY("Archery", "archery", "冲", FaceColor.GREEN.getColor(), true),
  MARKSMANSHIP("Marksmanship", "marksmanship", "决", LIME.getColor(), true),
  ARCANE_MAGICS("Arcane Magics", "arcane-magic", "冻", FaceColor.BLUE.getColor(), true),
  NATURAL_MAGICS("Natural Magics", "natural-magic", "冺", FaceColor.GREEN.getColor(), true),
  BLACK_MAGICS("Black Magics", "dark-magics", "冹", FaceColor.PURPLE.getColor(), true),
  CELESTIAL_MAGICS("Holy Magic", "light-magic", "冼", FaceColor.WHITE.getColor(), true);

  public final static LifeSkillType[] types = LifeSkillType.values();

  private final String prettyName;
  private final String dataName;
  private final String character;
  private final TextComponent characterComponent;
  private final TextComponent nameComponent;
  private final ChatColor color;
  private final boolean combat;

  LifeSkillType(String prettyName, String dataName, String character, ChatColor color, boolean combat) {
    this.prettyName = prettyName;
    this.dataName = dataName;
    this.character = character;
    this.color = color;
    this.combat = combat;

    nameComponent = reShadow(color(Component.text(prettyName), LIME));
    characterComponent = noShadow(Component.text(character));
  }

  LifeSkillType(String prettyName, String dataName, String character, ChatColor color) {
    this.prettyName = prettyName;
    this.dataName = dataName;
    this.character = character;
    this.color = color;
    this.combat = false;

    nameComponent = reShadow(color(Component.text(prettyName), LIME));
    characterComponent = noShadow(Component.text(character));
  }
}

