package land.face.strife.managers;

import com.sentropic.guiapi.GUIAPI;
import com.sentropic.guiapi.gui.Alignment;
import com.sentropic.guiapi.gui.GUI;
import com.sentropic.guiapi.gui.GUIComponent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import land.face.dinvy.DeluxeInvyPlugin;
import land.face.dinvy.pojo.PlayerData;
import land.face.dinvy.windows.equipment.EquipmentMenu.DeluxeSlot;
import land.face.strife.StrifePlugin;
import land.face.strife.data.NoticeData;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GuiManager {

  private final StrifePlugin plugin;

  private final Map<Player, GUI> guiMap = new WeakHashMap<>();
  private final Map<Player, NoticeData> noticeMap = new WeakHashMap<>();

  private final GUIComponent healthBase = new GUIComponent("status-base", new TextComponent("➲"),
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

  private final ChatColor levelColorBukkit = ChatColor.of("#72D92D");

  public static final TextComponent EMPTY = new TextComponent("");
  public static final TextComponent NOTICE_COOLDOWN = new TextComponent("᳥");
  public static final TextComponent NOTICE_ENERGY = new TextComponent("᳣");
  public static final TextComponent NOTICE_REQUIREMENT = new TextComponent("᳤");
  public static final TextComponent NOTICE_INVALID_TARGET = new TextComponent("᳢");
  public static final TextComponent WING_TING = new TextComponent("䶰");
  public static final TextComponent WING_TING_EMPTY = new TextComponent("䎘");
  public static final TextComponent EARTH_RUNE = new TextComponent("㆞");

  private final List<TextComponent> xpBar = List.of(
      new TextComponent("⒈"),
      new TextComponent("⒉"),
      new TextComponent("⒊"),
      new TextComponent("⒋"),
      new TextComponent("⒌"),
      new TextComponent("⒍"),
      new TextComponent("⒎"),
      new TextComponent("⒏"),
      new TextComponent("⒐"),
      new TextComponent("⒑")
  );

  public static final Map<Integer, TextComponent> HP_BAR = new HashMap<>();
  public static final Map<Integer, TextComponent> ENERGY_BAR = new HashMap<>();

  public GuiManager(StrifePlugin plugin) {
    this.plugin = plugin;
    if (HP_BAR.isEmpty()) {
      buildHealthAndEnergy();
    }
  }

  public void postNotice(Player player, NoticeData data) {
    noticeMap.put(player, data);
    guiMap.get(player).update(new GUIComponent("notices", data.getTextComponent(), data.getWidth(), 0, Alignment.CENTER));
  }

  public void tickNotices(Player player) {
    if (noticeMap.containsKey(player)) {
      NoticeData data = noticeMap.get(player);
      data.setDurationTicks(data.getDurationTicks() - 1);
      if (data.getDurationTicks() == 0) {
        guiMap.get(player).update(new GUIComponent("notices", EMPTY, 0, 0, Alignment.CENTER));
        noticeMap.remove(player);
      }
    }
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
    gui.putOnTop(new GUIComponent("notices", new TextComponent(""), 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("rage-bar", new TextComponent(""), 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("block-ind", new TextComponent(""), 0, 0, Alignment.CENTER));

    gui.putOnTop(new GUIComponent("dura-helmet", new TextComponent(""), 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("dura-body", new TextComponent(""), 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("dura-legs", new TextComponent(""), 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("dura-boots", new TextComponent(""), 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("dura-weapon", new TextComponent(""), 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("dura-offhand", new TextComponent(""), 0, 0, Alignment.CENTER));

    gui.putOnTop(new GUIComponent("wing-1", EMPTY, 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("wing-2", EMPTY, 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("wing-3", EMPTY, 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("wing-4", EMPTY, 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("wing-5", EMPTY, 0, 0, Alignment.CENTER));

    gui.putOnTop(new GUIComponent("rune-display", EMPTY, 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("rune-amount", EMPTY, 0, 0, Alignment.CENTER));

    guiMap.put(p, gui);
  }

  public GUI getGui(Player p) {
    return guiMap.get(p);
  }

  public void updateComponent(Player player, GUIComponent component) {
    guiMap.get(player).update(component);
  }

  public void updateLevelDisplay(Player player) {
    GUI gui = guiMap.get(player);
    String originalLevelString = Integer.toString(player.getLevel());
    String levelString = plugin.getGuiManager().convertToLevelFont(player.getLevel());
    gui.update(new GUIComponent("level-display",
        new TextComponent(levelString), originalLevelString.length() * 12, -106,
        Alignment.CENTER));

    int xpProgress = (int) (9 * player.getExp());
    gui.update(new GUIComponent("xp-base", xpBar.get(xpProgress), 15, 98, Alignment.CENTER));
  }

  public void updateEquipmentDisplay(Player player) {
    updateEquipmentDisplay(player,
        DeluxeInvyPlugin.getInstance().getPlayerManager().getPlayerData(player));
  }

  public void updateEquipmentDisplay(Player player, PlayerData data) {
    GUI gui = guiMap.get(player);
    if (data != null) {
      gui.update(new GUIComponent("dura-helmet",
          new TextComponent(duraString(data.getEquipmentItem(DeluxeSlot.HELMET), "০")),
          45, 233, Alignment.RIGHT));
      gui.update(new GUIComponent("dura-body",
          new TextComponent(duraString(data.getEquipmentItem(DeluxeSlot.BODY), "১")),
          45, 233, Alignment.RIGHT));
      gui.update(new GUIComponent("dura-legs",
          new TextComponent(duraString(data.getEquipmentItem(DeluxeSlot.LEGS), "২")),
          45, 233, Alignment.RIGHT));
      gui.update(new GUIComponent("dura-boots",
          new TextComponent(duraString(data.getEquipmentItem(DeluxeSlot.BOOTS), "৩")),
          45, 233, Alignment.RIGHT));
      gui.update(new GUIComponent("dura-weapon",
          new TextComponent(duraString(player.getEquipment().getItemInMainHand(), "৪")),
          45, 233, Alignment.RIGHT));
      gui.update(new GUIComponent("dura-offhand",
          new TextComponent(duraString(player.getEquipment().getItemInOffHand(), "৫")),
          45, 233, Alignment.RIGHT));
    }
  }

  public static String duraString(ItemStack stack, String string) {
    if (stack == null || stack.getType().getMaxDurability() < 5) {
      return org.bukkit.ChatColor.GRAY + string;
    }
    float percent = 1 - ((float) stack.getDurability() / stack.getType().getMaxDurability());
    if (percent > 0.6) {
      return org.bukkit.ChatColor.WHITE + string;
    }
    if (percent > 0.4) {
      return org.bukkit.ChatColor.YELLOW + string;
    }
    if (percent > 0.2) {
      return org.bukkit.ChatColor.GOLD + string;
    }
    return org.bukkit.ChatColor.DARK_RED + string;
  }


  public static void buildHealthAndEnergy() {
    Bukkit.getLogger().info("[Strife] Building GUI missing life/energy");
    Bukkit.getLogger().info("[Strife] This could take a bit...");
    for (int i = 0; i <= 178; i++) {
      int remainder = i;
      String hpBar = "";
      String eBar = "";
      if (remainder >= 128) {
        hpBar += "❺\uF801";
        eBar += "❿\uF801";
        remainder -= 128;
      }
      if (remainder >= 64) {
        hpBar += "❹\uF801";
        eBar += "❾\uF801";
        remainder -= 64;
      }
      if (remainder >= 32) {
        hpBar += "❸\uF801";
        eBar += "❽\uF801";
        remainder -= 32;
      }
      if (remainder >= 16) {
        hpBar += "❷\uF801";
        eBar += "❼\uF801";
        remainder -= 16;
      }
      if (remainder >= 8) {
        hpBar += "❶\uF801";
        eBar += "❻\uF801";
        remainder -= 8;
      }
      while (remainder > 0) {
        hpBar += "\uD801\uDCA0\uF801";
        eBar += "੦\uF801";
        remainder--;
      }
      HP_BAR.put(i, new TextComponent(ChatColor.DARK_RED + hpBar + ChatColor.RESET));
      ENERGY_BAR.put(i, new TextComponent(ChatColor.GOLD + eBar + ChatColor.RESET));
    }
    Bukkit.getLogger().info("[Strife] Missing life/energy bars built!");
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
    s = ChatColor.GOLD + s + ChatColor.RESET;
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
