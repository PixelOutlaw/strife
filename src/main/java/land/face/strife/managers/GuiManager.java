package land.face.strife.managers;

import com.sentropic.guiapi.GUIAPI;
import com.sentropic.guiapi.gui.Alignment;
import com.sentropic.guiapi.gui.GUI;
import com.sentropic.guiapi.gui.GUIComponent;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import land.face.strife.StrifePlugin;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class GuiManager {

  private final StrifePlugin plugin;

  private final Map<Player, GUI> guiMap = new WeakHashMap<>();

  private final GUIComponent healthBase = new GUIComponent("status-base", new TextComponent("❶"),
      178, 0, Alignment.CENTER);
  private final GUIComponent levelBase = new GUIComponent("level-base", new TextComponent("⅟"), 27,
      -105, Alignment.CENTER);
  private final GUIComponent bitsBase = new GUIComponent("bits-base", new TextComponent("錢"),
      102, 180, Alignment.CENTER);

  private final Map<Integer, String> hpStringNumbers = new HashMap<>();
  private final Map<Integer, String> energyStringNumbers = new HashMap<>();
  private final Map<Integer, String> levelStringNumbers = new HashMap<>();
  private final Map<Integer, String> moneyStringNumbers = new HashMap<>();
  private final Map<Integer, String> gemStringNumbers = new HashMap<>();

  public GuiManager(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  public void setupGui(Player p) {
    GUI gui = GUIAPI.getGUIManager().getGUI(p);
    gui.putUnderneath(healthBase);
    gui.putUnderneath(levelBase);
    gui.putUnderneath(bitsBase);

    gui.putOnTop(new GUIComponent("missing-life", new TextComponent(""), 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("missing-energy", new TextComponent(""), 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("barrier-bar", new TextComponent(""), 0, 0, Alignment.CENTER));

    gui.putOnTop(new GUIComponent("life-display", new TextComponent(""), 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("xp-base", new TextComponent(""), 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("energy-display", new TextComponent(""), 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("money-display", new TextComponent(""), 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("level-display", new TextComponent(""), 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("gem-display", new TextComponent(""), 0, 0, Alignment.CENTER));

    gui.putOnTop(new GUIComponent("attack-bar", new TextComponent(""), 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("rage-bar", new TextComponent(""), 0, 0, Alignment.CENTER));

    gui.putOnTop(new GUIComponent("dura-helmet", new TextComponent(""), 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("dura-body", new TextComponent(""), 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("dura-legs", new TextComponent(""), 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("dura-boots", new TextComponent(""), 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("dura-weapon", new TextComponent(""), 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("dura-offhand", new TextComponent(""), 0, 0, Alignment.CENTER));

    guiMap.put(p, gui);
  }

  public GUI getGui(Player p) {
    return guiMap.get(p);
  }

  public void updateComponent(Player player, GUIComponent component) {
    guiMap.get(player).update(component);
  }

  public String convertToHpDisplay(int i) {
    if (hpStringNumbers.containsKey(i)) {
      return hpStringNumbers.get(i);
    }
    String s = Integer.toString(i);
    s = s
        .replaceAll("0", "➉")
        .replaceAll("1", "➀")
        .replaceAll("2", "➁")
        .replaceAll("3", "➂")
        .replaceAll("4", "➃")
        .replaceAll("5", "➄")
        .replaceAll("6", "➅")
        .replaceAll("7", "➆")
        .replaceAll("8", "➇")
        .replaceAll("9", "➈");
    hpStringNumbers.put(i, s);
    return s;
  }

  public String convertToEnergyDisplayFont(int i) {
    if (energyStringNumbers.containsKey(i)) {
      return energyStringNumbers.get(i);
    }
    String s = Integer.toString(i);
    s = s
        .replaceAll("0", "⓾")
        .replaceAll("1", "⓵")
        .replaceAll("2", "⓶")
        .replaceAll("3", "⓷")
        .replaceAll("4", "⓸")
        .replaceAll("5", "⓹")
        .replaceAll("6", "⓺")
        .replaceAll("7", "⓻")
        .replaceAll("8", "⓼")
        .replaceAll("9", "⓽");
    energyStringNumbers.put(i, s);
    return s;
  }

  public String convertToLevelFont(int i) {
    if (levelStringNumbers.containsKey(i)) {
      return levelStringNumbers.get(i);
    }
    String s = Integer.toString(i);
    s = s
        .replaceAll("1", "०\uF801")
        .replaceAll("2", "१\uF801")
        .replaceAll("3", "२\uF801")
        .replaceAll("4", "३\uF801")
        .replaceAll("5", "४\uF801")
        .replaceAll("6", "५\uF801")
        .replaceAll("7", "६\uF801")
        .replaceAll("8", "७\uF801")
        .replaceAll("9", "८\uF801")
        .replaceAll("0", "९\uF801");
    s = net.md_5.bungee.api.ChatColor.of(new Color(0x72D92D)) + s + ChatColor.RESET;
    levelStringNumbers.put(i, s);
    return s;
  }

  public String convertToMoneyFont(int i) {
    if (moneyStringNumbers.containsKey(i)) {
      return moneyStringNumbers.get(i);
    }
    String s = Integer.toString(i);
    s = s
        .replaceAll("1", "①\uF801")
        .replaceAll("2", "②\uF801")
        .replaceAll("3", "③\uF801")
        .replaceAll("4", "④\uF801")
        .replaceAll("5", "⑤\uF801")
        .replaceAll("6", "⑥\uF801")
        .replaceAll("7", "⑦\uF801")
        .replaceAll("8", "⑧\uF801")
        .replaceAll("9", "⑨\uF801")
        .replaceAll("0", "⑩\uF801");
    s = ChatColor.YELLOW + s + ChatColor.RESET;
    moneyStringNumbers.put(i, s);
    return s;
  }

  public String convertToGemFont(int i) {
    if (gemStringNumbers.containsKey(i)) {
      return gemStringNumbers.get(i);
    }
    String s = Integer.toString(i);
    s = s
        .replaceAll("1", "①\uF801")
        .replaceAll("2", "②\uF801")
        .replaceAll("3", "③\uF801")
        .replaceAll("4", "④\uF801")
        .replaceAll("5", "⑤\uF801")
        .replaceAll("6", "⑥\uF801")
        .replaceAll("7", "⑦\uF801")
        .replaceAll("8", "⑧\uF801")
        .replaceAll("9", "⑨\uF801")
        .replaceAll("0", "⑩\uF801");
    s = ChatColor.LIGHT_PURPLE + s + ChatColor.RESET;
    gemStringNumbers.put(i, s);
    return s;
  }
}
