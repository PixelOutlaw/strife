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
import land.face.strife.data.champion.Champion;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MainHand;

public class GuiManager {

  private final StrifePlugin plugin;

  // Must be static, to persist through reloads
  private static final Map<Player, GUI> guiMap = new WeakHashMap<>();
  private final Map<Player, NoticeData> noticeMap = new WeakHashMap<>();

  private final GUIComponent healthBase = new GUIComponent("status-base", new TextComponent("➲"),
      178, 0, Alignment.CENTER);
  private final GUIComponent levelBase = new GUIComponent("level-base", new TextComponent("⅟"), 27,
      -105, Alignment.CENTER);
  private final GUIComponent bitsBase = new GUIComponent("bits-base", new TextComponent("₿"), 14, 199, Alignment.CENTER);
  private final GUIComponent gemsBase = new GUIComponent("gems-base", new TextComponent("௹") , 15, 198, Alignment.CENTER);

  private final Map<Integer, String> hpStringNumbers = new HashMap<>();
  private final Map<Integer, String> energyStringNumbers = new HashMap<>();
  private final Map<Integer, String> middleStringNumbers = new HashMap<>();
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
  public static final TextComponent FROST_ICON = new TextComponent("凚");
  public static final TextComponent CORRUPT_ICON = new TextComponent("黑");

  public static final TextComponent NO_GOD = new TextComponent("᮰");
  public static final TextComponent GOD_FACEGUY = new TextComponent("᮱");
  public static final TextComponent GOD_AURORA = new TextComponent("᮲");
  public static final TextComponent GOD_ZEXIR = new TextComponent("᮳");
  public static final TextComponent GOD_ANYA = new TextComponent("᮴");

  @SuppressWarnings("deprecation")
  private final List<TextComponent> xpBar = List.of(
      new TextComponent("䷀"),
      new TextComponent("䷁"),
      new TextComponent("䷂"),
      new TextComponent("䷃"),
      new TextComponent("䷄"),
      new TextComponent("䷅"),
      new TextComponent("䷆"),
      new TextComponent("䷇"),
      new TextComponent("䷈"),
      new TextComponent("䷉"),
      new TextComponent("䷊"),
      new TextComponent("䷋"),
      new TextComponent("䷌"),
      new TextComponent("䷍"),
      new TextComponent("䷎"),
      new TextComponent("䷏"),
      new TextComponent("䷐"),
      new TextComponent("䷑"),
      new TextComponent("䷒"),
      new TextComponent("䷓"),
      new TextComponent("䷔"),
      new TextComponent("䷕"),
      new TextComponent("䷖"),
      new TextComponent("䷗"),
      new TextComponent("䷘")
  );

  @SuppressWarnings("deprecation")
  private final List<TextComponent> oxygenBar = List.of(
      new TextComponent("䷙"),
      new TextComponent("䷚"),
      new TextComponent("䷛"),
      new TextComponent("䷜"),
      new TextComponent("䷝"),
      new TextComponent("䷞"),
      new TextComponent("䷟"),
      new TextComponent("䷠"),
      new TextComponent("䷡"),
      new TextComponent("䷢"),
      new TextComponent("䷣"),
      new TextComponent("䷤"),
      new TextComponent("䷥"),
      new TextComponent("䷦"),
      new TextComponent("䷧"),
      new TextComponent("䷨"),
      new TextComponent("䷩"),
      new TextComponent("䷪"),
      new TextComponent("䷫"),
      new TextComponent("䷬"),
      new TextComponent("䷭"),
      new TextComponent("䷮"),
      new TextComponent("䷯"),
      new TextComponent("䷰"),
      new TextComponent("䷱")
  );

  public static final Map<Integer, TextComponent> HP_BAR = new HashMap<>();
  public static final Map<Integer, TextComponent> ENERGY_BAR = new HashMap<>();
  public static final Map<Integer, TextComponent> BARRIER_BAR_1 = new HashMap<>();
  public static final Map<Integer, TextComponent> BARRIER_BAR_2 = new HashMap<>();
  public static final Map<Integer, TextComponent> BARRIER_BAR_3 = new HashMap<>();
  public static final Map<Integer, String> HEALTH_BAR_TARGET = new HashMap<>();
  public static final Map<Integer, String> BARRIER_BAR_TARGET = new HashMap<>();

  public GuiManager(StrifePlugin plugin) {
    this.plugin = plugin;
    if (HP_BAR.isEmpty()) {
      buildHealthEnergyAndBarrier();
      buildTargetHealthBars();
    }
  }

  public void postNotice(Player player, NoticeData data) {
    noticeMap.put(player, data);
    guiMap.get(player).update(
        new GUIComponent("notices", data.getTextComponent(), data.getWidth(), 0, Alignment.CENTER));
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
    if (guiMap.containsKey(p)) {
      return;
    }
    GUI gui = GUIAPI.getGUIManager().getGUI(p);
    gui.putUnderneath(healthBase);
    gui.putUnderneath(levelBase);
    gui.putUnderneath(bitsBase);
    gui.putUnderneath(gemsBase);

    gui.putOnTop(new GUIComponent("missing-life", new TextComponent(""), 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("missing-energy", new TextComponent(""), 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("barrier-bar", new TextComponent(""), 0, 0, Alignment.CENTER));

    /*
    gui.putOnTop(new GUIComponent("dura-helmet", new TextComponent(""), 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("dura-body", new TextComponent(""), 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("dura-legs", new TextComponent(""), 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("dura-boots", new TextComponent(""), 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("dura-weapon", new TextComponent(""), 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("dura-offhand", new TextComponent(""), 0, 0, Alignment.CENTER));
    */

    gui.putOnTop(new GUIComponent("invincible", new TextComponent(""), 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("life-display", new TextComponent(""), 0, 0, Alignment.CENTER));

    gui.putOnTop(new GUIComponent("xp-base", new TextComponent(""), 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("air-base", new TextComponent(""), 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("energy-display", new TextComponent(""), 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("money-display", new TextComponent(""), 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("gem-display", new TextComponent(""), 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("level-display", new TextComponent(""), 0, 0, Alignment.CENTER));

    gui.putOnTop(new GUIComponent("attack-bar", new TextComponent(""), 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("notices", new TextComponent(""), 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("rage-bar", new TextComponent(""), 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("block-ind", new TextComponent(""), 0, 0, Alignment.CENTER));

    gui.putOnTop(new GUIComponent("wing-1", EMPTY, 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("wing-2", EMPTY, 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("wing-3", EMPTY, 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("wing-4", EMPTY, 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("wing-5", EMPTY, 0, 0, Alignment.CENTER));

    gui.putOnTop(new GUIComponent("food-bar-1", EMPTY, 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("food-bar-2", EMPTY, 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("food-bar-3", EMPTY, 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("food-bar-4", EMPTY, 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("food-bar-5", EMPTY, 0, 0, Alignment.CENTER));

    gui.putOnTop(new GUIComponent("food-icon-FULLNESS", EMPTY, 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("food-icon-PROTEIN", EMPTY, 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("food-icon-FAT", EMPTY, 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("food-icon-CARBOHYDRATE", EMPTY, 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("food-icon-VITAMINS", EMPTY, 0, 0, Alignment.CENTER));

    gui.putOnTop(new GUIComponent("frost-display", EMPTY, 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("corrupt-display", EMPTY, 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("rune-display", EMPTY, 0, 0, Alignment.CENTER));

    gui.putOnTop(new GUIComponent("rune-amount", EMPTY, 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("frost-amount", EMPTY, 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("corrupt-amount", EMPTY, 0, 0, Alignment.CENTER));

    gui.putOnTop(new GUIComponent("slot-a-charges", EMPTY, 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("slot-b-charges", EMPTY, 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("slot-c-charges", EMPTY, 0, 0, Alignment.CENTER));

    gui.putOnTop(new GUIComponent("god-slot", NO_GOD, 0, 0, Alignment.CENTER));

    guiMap.put(p, gui);

    updateGodDisplay(p, p.getMainHand() == MainHand.RIGHT);
  }

  public GUI getGui(Player p) {
    return guiMap.get(p);
  }

  public void updateGodDisplay(Player player, boolean right) {
    Champion champion = plugin.getChampionManager().getChampion(player);

    TextComponent gc;
    if (champion.getSaveData().getSelectedGod() == null) {
      gc = NO_GOD;
    } else {
      gc = switch (champion.getSaveData().getSelectedGod()) {
        case NONE -> NO_GOD;
        case FACEGUY -> GOD_FACEGUY;
        case AURORA -> GOD_AURORA;
        case ZEXIR -> GOD_ZEXIR;
        case ANYA -> GOD_ANYA;
      };
    }
    if (right) {
      updateComponent(player, new GUIComponent("god-slot", gc, 19, 99, Alignment.LEFT));
    } else {
      updateComponent(player, new GUIComponent("god-slot", gc, 19, -100, Alignment.RIGHT));
    }
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

    int xpProgress = (int) (24 * player.getExp());
    gui.update(new GUIComponent("xp-base", xpBar.get(xpProgress), 11, 96, Alignment.CENTER));
  }

  public void updateAir(GUI gui, Player player) {
    if (player.getRemainingAir() == player.getMaximumAir()) {
      gui.update(new GUIComponent("air-base", EMPTY, 0, 0, Alignment.CENTER));
    } else if (player.getRemainingAir() < 1) {
      gui.update(new GUIComponent("air-base", oxygenBar.get(0), 11, 96, Alignment.CENTER));
    } else {
      int progress = (int) Math.ceil(
          24 * ((float) player.getRemainingAir()) / player.getMaximumAir());
      gui.update(new GUIComponent("air-base", oxygenBar.get(progress), 11, 96, Alignment.CENTER));
    }
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

  public static void buildTargetHealthBars() {
    Bukkit.getLogger().info("[Strife] Building Target GUI missing life/energy");
    Bukkit.getLogger().info("[Strife] This could take a bit...");
    for (int i = 0; i <= 138; i++) {
      int remainder = i;
      StringBuilder prefix = new StringBuilder();
      StringBuilder hpBar = new StringBuilder();
      StringBuilder barrierBar = new StringBuilder();
      boolean kickBack = false;
      while (remainder >= 138) {
        hpBar.append("Ũ\uF801");
        barrierBar.append("Ù\uF801");
        prefix.append("\uF80C\uF808\uF802");
        remainder -= 138;
      }
      while (remainder >= 64) {
        hpBar.append("ũ\uF801");
        barrierBar.append("ú\uF801");
        prefix.append("\uF80B");
        remainder -= 64;
      }
      while (remainder >= 32) {
        hpBar.append("Ū\uF801");
        barrierBar.append("Ú\uF801");
        prefix.append("\uF80A");
        remainder -= 32;
      }
      while (remainder >= 16) {
        hpBar.append("ū\uF801");
        barrierBar.append("ù\uF801");
        prefix.append("\uF809");
        remainder -= 16;
      }
      while (remainder >= 8) {
        hpBar.append("Ŭ\uF801");
        barrierBar.append("Ҋ\uF801");
        prefix.append("\uF808");
        remainder -= 8;
      }
      while (remainder > 0) {
        hpBar.append("ŭ\uF801");
        barrierBar.append("ҋ\uF801");
        prefix.append("\uF801");
        remainder--;
      }
      HEALTH_BAR_TARGET.put(i, "\uF801" + prefix + hpBar);
      BARRIER_BAR_TARGET.put(i, "\uF80C\uF808\uF802" + barrierBar + prefix + "\uF83C\uF838\uF822");
    }
    Bukkit.getLogger().info("[Strife] Missing target life/barrier bars built!");
  }

  public static void buildHealthEnergyAndBarrier() {
    Bukkit.getLogger().info("[Strife] Building GUI missing life/energy");
    Bukkit.getLogger().info("[Strife] This could take a bit...");
    for (int i = 0; i <= 178; i++) {
      int remainder = i;
      StringBuilder hpBar = new StringBuilder();
      StringBuilder eBar = new StringBuilder();
      StringBuilder barrierBar1 = new StringBuilder();
      StringBuilder barrierBar2 = new StringBuilder();
      StringBuilder barrierBar3 = new StringBuilder();
      if (remainder >= 128) {
        hpBar.append("❺\uF801");
        eBar.append("❿\uF801");
        barrierBar1.append("⒃\uF801");
        barrierBar2.append("⒄\uF801");
        barrierBar3.append("⒅\uF801");
        remainder -= 128;
      }
      if (remainder >= 64) {
        hpBar.append("❹\uF801");
        eBar.append("❾\uF801");
        barrierBar1.append("⒀\uF801");
        barrierBar2.append("⒁\uF801");
        barrierBar3.append("⒂\uF801");
        remainder -= 64;
      }
      if (remainder >= 32) {
        hpBar.append("❸\uF801");
        eBar.append("❽\uF801");
        barrierBar1.append("⑽\uF801");
        barrierBar2.append("⑾\uF801");
        barrierBar3.append("⑿\uF801");
        remainder -= 32;
      }
      if (remainder >= 16) {
        hpBar.append("❷\uF801");
        eBar.append("❼\uF801");
        barrierBar1.append("⑺\uF801");
        barrierBar2.append("⑻\uF801");
        barrierBar3.append("⑼\uF801");
        remainder -= 16;
      }
      if (remainder >= 8) {
        hpBar.append("❶\uF801");
        eBar.append("❻\uF801");
        barrierBar1.append("⑷\uF801");
        barrierBar2.append("⑸\uF801");
        barrierBar3.append("⑹\uF801");
        remainder -= 8;
      }
      while (remainder > 0) {
        hpBar.append("\uD801\uDCA0\uF801");
        eBar.append("੦\uF801");
        barrierBar1.append("⑴\uF801");
        barrierBar2.append("⑵\uF801");
        barrierBar3.append("⑶\uF801");
        remainder--;
      }
      HP_BAR.put(i, new TextComponent(ChatColor.DARK_RED + hpBar.toString() + ChatColor.RESET));
      ENERGY_BAR.put(i, new TextComponent(ChatColor.GOLD + eBar.toString() + ChatColor.RESET));
      BARRIER_BAR_1.put(i, new TextComponent(barrierBar1.toString()));
      BARRIER_BAR_2.put(i, new TextComponent(barrierBar2.toString()));
      BARRIER_BAR_3.put(i, new TextComponent(barrierBar3.toString()));
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

  public String convertToMiddleString(int i) {
    if (middleStringNumbers.containsKey(i)) {
      return middleStringNumbers.get(i);
    }
    String s = Integer.toString(i);
    s = s
        .replaceAll("0", "➓")
        .replaceAll("1", "➊")
        .replaceAll("2", "➋")
        .replaceAll("3", "➌")
        .replaceAll("4", "➍")
        .replaceAll("5", "➎")
        .replaceAll("6", "➏")
        .replaceAll("7", "➐")
        .replaceAll("8", "➑")
        .replaceAll("9", "➒");
    middleStringNumbers.put(i, s);
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

  public String convertToMoneyFont(int i, ChatColor color) {
    if (moneyStringNumbers.containsKey(i)) {
      return color + moneyStringNumbers.get(i) + ChatColor.RESET;
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
    moneyStringNumbers.put(i, s);
    return color + s + ChatColor.RESET;
  }

  public String convertToGemFont(int i, ChatColor color) {
    if (gemStringNumbers.containsKey(i)) {
      return color + gemStringNumbers.get(i) + ChatColor.RESET;
    }
    String s = Integer.toString(i);
    s = s
        .replaceAll("1", "⓫\uF801")
        .replaceAll("2", "⓬\uF801")
        .replaceAll("3", "⓭\uF801")
        .replaceAll("4", "⓮\uF801")
        .replaceAll("5", "⓯\uF801")
        .replaceAll("6", "⓰\uF801")
        .replaceAll("7", "⓱\uF801")
        .replaceAll("8", "⓲\uF801")
        .replaceAll("9", "⓳\uF801")
        .replaceAll("0", "⓴\uF801");
    gemStringNumbers.put(i, s);
    return color + s + ChatColor.RESET;
  }
}
