package land.face.strife.managers;

import com.sentropic.guiapi.GUIAPI;
import com.sentropic.guiapi.gui.Alignment;
import com.sentropic.guiapi.gui.GUI;
import com.sentropic.guiapi.gui.GUIComponent;
import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import land.face.dinvy.DeluxeInvyPlugin;
import land.face.dinvy.entity.PlayerData;
import land.face.dinvy.windows.equipment.EquipmentMenu.DeluxeSlot;
import land.face.strife.StrifePlugin;
import land.face.strife.data.NoticeData;
import land.face.strife.data.StrifeMob;
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

  private static final DecimalFormat XP_FORMAT = new DecimalFormat("0.0");
  // #FBFBFB
  public static final ChatColor NO_SHADOW = FaceColor.NO_SHADOW.getColor();

  private final GUIComponent healthBase = new GUIComponent("status-base",
      noShadow(new TextComponent("➲")), 178, 0, Alignment.CENTER);
  private final GUIComponent levelBase = new GUIComponent("level-base",
      noShadow(new TextComponent("⅟")), 27, -105, Alignment.CENTER);
  private final GUIComponent bitsBase = new GUIComponent("bits-base",
      noShadow(new TextComponent("₿")), 14, 199, Alignment.CENTER);
  private final GUIComponent gemsBase = new GUIComponent("gems-base",
      noShadow(new TextComponent("௹")) , 15, 198, Alignment.CENTER);
  private final GUIComponent notifsBase = new GUIComponent("notifs-base",
      noShadow(new TextComponent("偀")) , 53, -181, Alignment.CENTER);

  private final Map<Integer, String> hpStringNumbers = new HashMap<>();
  private final Map<Integer, String> energyStringNumbers = new HashMap<>();
  private final Map<Integer, String> middleStringNumbers = new HashMap<>();
  private final Map<Integer, TextComponent> levelStringNumbers = new HashMap<>();
  private final Map<Integer, TextComponent> moneyStringNumbers = new HashMap<>();
  private final Map<Integer, TextComponent> gemStringNumbers = new HashMap<>();

  public static final TextComponent EMPTY = new TextComponent("");
  public static final TextComponent NOTICE_COOLDOWN = noShadow(new TextComponent("᳥"));
  public static final TextComponent NOTICE_ENERGY = noShadow(new TextComponent("᳣"));
  public static final TextComponent NOTICE_REQUIREMENT = noShadow(new TextComponent("᳤"));
  public static final TextComponent NOTICE_INVALID_TARGET = noShadow(new TextComponent("᳢"));

  public static final TextComponent NO_GOD =  noShadow(new TextComponent("᮰"));
  public static final TextComponent GOD_FACEGUY =  noShadow(new TextComponent("᮱"));
  public static final TextComponent GOD_AURORA = noShadow(new TextComponent("᮲"));
  public static final TextComponent GOD_ZEXIR = noShadow(new TextComponent("᮳"));
  public static final TextComponent GOD_ANYA = noShadow(new TextComponent("᮴"));

  public static final TextComponent LIFE_SEPERATOR = noShadow(new TextComponent("拾"));
  public static final TextComponent ENERGY_SEPERATOR = noShadow(new TextComponent("拿"));

  private final Map<Integer, GUIComponent> builtXpFont = buildXpFont();

  @SuppressWarnings("deprecation")
  private final List<GUIComponent> xpBar = List.of(
       new GUIComponent("xp-bar", noShadow(new TextComponent("䷀")),25, 104, Alignment.CENTER),
       new GUIComponent("xp-bar", noShadow(new TextComponent("䷁")),25, 104, Alignment.CENTER),
       new GUIComponent("xp-bar", noShadow(new TextComponent("䷂")),25, 104, Alignment.CENTER),
       new GUIComponent("xp-bar", noShadow(new TextComponent("䷃")),25, 104, Alignment.CENTER),
       new GUIComponent("xp-bar", noShadow(new TextComponent("䷄")),25, 104, Alignment.CENTER),
       new GUIComponent("xp-bar", noShadow(new TextComponent("䷅")),25, 104, Alignment.CENTER),
       new GUIComponent("xp-bar", noShadow(new TextComponent("䷆")),25, 104, Alignment.CENTER),
       new GUIComponent("xp-bar", noShadow(new TextComponent("䷇")),25, 104, Alignment.CENTER),
       new GUIComponent("xp-bar", noShadow(new TextComponent("䷈")),25, 104, Alignment.CENTER),
       new GUIComponent("xp-bar", noShadow(new TextComponent("䷉")),25, 104, Alignment.CENTER),
       new GUIComponent("xp-bar", noShadow(new TextComponent("䷊")),25, 104, Alignment.CENTER),
       new GUIComponent("xp-bar", noShadow(new TextComponent("䷋")),25, 104, Alignment.CENTER),
       new GUIComponent("xp-bar", noShadow(new TextComponent("䷌")),25, 104, Alignment.CENTER),
       new GUIComponent("xp-bar", noShadow(new TextComponent("䷍")),25, 104, Alignment.CENTER),
       new GUIComponent("xp-bar", noShadow(new TextComponent("䷎")),25, 104, Alignment.CENTER),
       new GUIComponent("xp-bar", noShadow(new TextComponent("䷏")),25, 104, Alignment.CENTER),
       new GUIComponent("xp-bar", noShadow(new TextComponent("䷐")),25, 104, Alignment.CENTER),
       new GUIComponent("xp-bar", noShadow(new TextComponent("䷑")),25, 104, Alignment.CENTER),
       new GUIComponent("xp-bar", noShadow(new TextComponent("䷒")),25, 104, Alignment.CENTER),
       new GUIComponent("xp-bar", noShadow(new TextComponent("䷓")),25, 104, Alignment.CENTER),
       new GUIComponent("xp-bar", noShadow(new TextComponent("䷔")),25, 104, Alignment.CENTER),
       new GUIComponent("xp-bar", noShadow(new TextComponent("䷕")),25, 104, Alignment.CENTER),
       new GUIComponent("xp-bar", noShadow(new TextComponent("䷖")),25, 104, Alignment.CENTER),
       new GUIComponent("xp-bar", noShadow(new TextComponent("䷗")),25, 104, Alignment.CENTER),
       new GUIComponent("xp-bar", noShadow(new TextComponent("䷘")),25, 104, Alignment.CENTER)
  );

  @SuppressWarnings("deprecation")
  private final List<GUIComponent> catchupXpBar = List.of(
      new GUIComponent("cxp-bar", noShadow(new TextComponent("䷙")), 25, 104, Alignment.CENTER),
      new GUIComponent("cxp-bar", noShadow(new TextComponent("䷚")), 25, 104, Alignment.CENTER),
      new GUIComponent("cxp-bar", noShadow(new TextComponent("䷛")), 25, 104, Alignment.CENTER),
      new GUIComponent("cxp-bar", noShadow(new TextComponent("䷜")), 25, 104, Alignment.CENTER),
      new GUIComponent("cxp-bar", noShadow(new TextComponent("䷝")), 25, 104, Alignment.CENTER),
      new GUIComponent("cxp-bar", noShadow(new TextComponent("䷞")), 25, 104, Alignment.CENTER),
      new GUIComponent("cxp-bar", noShadow(new TextComponent("䷟")), 25, 104, Alignment.CENTER),
      new GUIComponent("cxp-bar", noShadow(new TextComponent("䷠")), 25, 104, Alignment.CENTER),
      new GUIComponent("cxp-bar", noShadow(new TextComponent("䷡")), 25, 104, Alignment.CENTER),
      new GUIComponent("cxp-bar", noShadow(new TextComponent("䷢")), 25, 104, Alignment.CENTER),
      new GUIComponent("cxp-bar", noShadow(new TextComponent("䷣")), 25, 104, Alignment.CENTER),
      new GUIComponent("cxp-bar", noShadow(new TextComponent("䷤")), 25, 104, Alignment.CENTER),
      new GUIComponent("cxp-bar", noShadow(new TextComponent("䷥")), 25, 104, Alignment.CENTER),
      new GUIComponent("cxp-bar", noShadow(new TextComponent("䷦")), 25, 104, Alignment.CENTER),
      new GUIComponent("cxp-bar", noShadow(new TextComponent("䷧")), 25, 104, Alignment.CENTER),
      new GUIComponent("cxp-bar", noShadow(new TextComponent("䷨")), 25, 104, Alignment.CENTER),
      new GUIComponent("cxp-bar", noShadow(new TextComponent("䷩")), 25, 104, Alignment.CENTER),
      new GUIComponent("cxp-bar", noShadow(new TextComponent("䷪")), 25, 104, Alignment.CENTER),
      new GUIComponent("cxp-bar", noShadow(new TextComponent("䷫")), 25, 104, Alignment.CENTER),
      new GUIComponent("cxp-bar", noShadow(new TextComponent("䷬")), 25, 104, Alignment.CENTER),
      new GUIComponent("cxp-bar", noShadow(new TextComponent("䷭")), 25, 104, Alignment.CENTER),
      new GUIComponent("cxp-bar", noShadow(new TextComponent("䷮")), 25, 104, Alignment.CENTER),
      new GUIComponent("cxp-bar", noShadow(new TextComponent("䷯")), 25, 104, Alignment.CENTER),
      new GUIComponent("cxp-bar", noShadow(new TextComponent("䷰")), 25, 104, Alignment.CENTER),
      new GUIComponent("cxp-bar", noShadow(new TextComponent("䷱")), 25, 104, Alignment.CENTER)
  );

  @SuppressWarnings("deprecation")
  private final List<GUIComponent> oxygenBar = List.of(
      new GUIComponent("air-bar", noShadow(new TextComponent("懀")), 25, 104, Alignment.CENTER),
      new GUIComponent("air-bar", noShadow(new TextComponent("懁")), 25, 104, Alignment.CENTER),
      new GUIComponent("air-bar", noShadow(new TextComponent("懂")), 25, 104, Alignment.CENTER),
      new GUIComponent("air-bar", noShadow(new TextComponent("懃")), 25, 104, Alignment.CENTER),
      new GUIComponent("air-bar", noShadow(new TextComponent("懄")), 25, 104, Alignment.CENTER),
      new GUIComponent("air-bar", noShadow(new TextComponent("懅")), 25, 104, Alignment.CENTER),
      new GUIComponent("air-bar", noShadow(new TextComponent("懆")), 25, 104, Alignment.CENTER),
      new GUIComponent("air-bar", noShadow(new TextComponent("懇")), 25, 104, Alignment.CENTER),
      new GUIComponent("air-bar", noShadow(new TextComponent("懈")), 25, 104, Alignment.CENTER),
      new GUIComponent("air-bar", noShadow(new TextComponent("應")), 25, 104, Alignment.CENTER),
      new GUIComponent("air-bar", noShadow(new TextComponent("懊")), 25, 104, Alignment.CENTER),
      new GUIComponent("air-bar", noShadow(new TextComponent("懋")), 25, 104, Alignment.CENTER),
      new GUIComponent("air-bar", noShadow(new TextComponent("懌")), 25, 104, Alignment.CENTER),
      new GUIComponent("air-bar", noShadow(new TextComponent("懍")), 25, 104, Alignment.CENTER),
      new GUIComponent("air-bar", noShadow(new TextComponent("懎")), 25, 104, Alignment.CENTER),
      new GUIComponent("air-bar", noShadow(new TextComponent("懏")), 25, 104, Alignment.CENTER),
      new GUIComponent("air-bar", noShadow(new TextComponent("懐")), 25, 104, Alignment.CENTER),
      new GUIComponent("air-bar", noShadow(new TextComponent("懑")), 25, 104, Alignment.CENTER),
      new GUIComponent("air-bar", noShadow(new TextComponent("懒")), 25, 104, Alignment.CENTER),
      new GUIComponent("air-bar", noShadow(new TextComponent("懓")), 25, 104, Alignment.CENTER),
      new GUIComponent("air-bar", noShadow(new TextComponent("懔")), 25, 104, Alignment.CENTER),
      new GUIComponent("air-bar", noShadow(new TextComponent("懕")), 25, 104, Alignment.CENTER),
      new GUIComponent("air-bar", noShadow(new TextComponent("懖")), 25, 104, Alignment.CENTER),
      new GUIComponent("air-bar", noShadow(new TextComponent("懗")), 25, 104, Alignment.CENTER)
  );

  public static final Map<Integer, TextComponent> HP_BAR = new HashMap<>();
  public static final Map<Integer, TextComponent> ENERGY_BAR = new HashMap<>();
  public static final Map<Integer, TextComponent> BARRIER_BAR_1 = new HashMap<>();
  public static final Map<Integer, TextComponent> BARRIER_BAR_2 = new HashMap<>();
  public static final Map<Integer, TextComponent> BARRIER_BAR_3 = new HashMap<>();
  public static final Map<Integer, String> HEALTH_BAR_TARGET = new HashMap<>();
  public static final Map<Integer, String> BARRIER_BAR_TARGET = new HashMap<>();

  public static final TextComponent notifMail = noShadow(new TextComponent("偁"));
  public static final TextComponent notifMarket = noShadow(new TextComponent("偂"));
  public static final TextComponent notifDaily = noShadow(new TextComponent("偃"));
  public static final TextComponent notifVote = noShadow(new TextComponent("偄"));

  public GuiManager(StrifePlugin plugin) {
    this.plugin = plugin;
    if (HP_BAR.isEmpty()) {
      buildHealthEnergyAndBarrier();
      buildTargetHealthBars();
    }
  }

  private Map<Integer, GUIComponent> buildXpFont() {
    Map<Integer, GUIComponent> values = new HashMap<>();
    for (int i = 0; i < 1000; i++) {
      int width = i < 100 ? 18 : 23;
      String s = XP_FORMAT.format(((double) i) / 10);
      s = s.replaceAll("1", "懠\uF802");
      s = s.replaceAll("2", "懡\uF802");
      s = s.replaceAll("3", "懢\uF802");
      s = s.replaceAll("4", "懣\uF802");
      s = s.replaceAll("5", "懤\uF802");
      s = s.replaceAll("6", "懥\uF802");
      s = s.replaceAll("7", "懦\uF802");
      s = s.replaceAll("8", "懧\uF802");
      s = s.replaceAll("9", "懨\uF802");
      s = s.replaceAll("0", "懩\uF802");
      s = s.replaceAll("\\.", "懫\uF802");
      s += "懪";
      GUIComponent component = new GUIComponent("xp-text", noShadow(new TextComponent(s)), width, 105, Alignment.CENTER);
      values.put(i, component);
    }
    return values;
  }

  public static TextComponent shadow(TextComponent t) {
    t.setColor(ChatColor.RESET);
    return t;
  }

  public static TextComponent noShadow(TextComponent t) {
    t.setColor(NO_SHADOW);
    return t;
  }

  public static TextComponent color(TextComponent t, ChatColor c) {
    t.setColor(c);
    return t;
  }

  public void postNotice(Player player, NoticeData data) {
    noticeMap.put(player, data);
    guiMap.get(player).update(
        new GUIComponent("notices", data.getTextComponent(), 171, 0, Alignment.CENTER));
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
    gui.putUnderneath(notifsBase);

    gui.putOnTop(new GUIComponent("life-segments", EMPTY, 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("energy-segments", EMPTY, 0, 0, Alignment.CENTER));

    gui.putOnTop(new GUIComponent("missing-life", EMPTY, 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("missing-energy", EMPTY, 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("barrier-bar", EMPTY, 0, 0, Alignment.CENTER));

    gui.putOnTop(new GUIComponent("invincible", EMPTY, 0, 0, Alignment.CENTER));

    // gui.putOnTop(new GUIComponent("life-display", EMPTY, 0, 0, Alignment.CENTER));
    // gui.putOnTop(new GUIComponent("energy-display", EMPTY, 0, 0, Alignment.CENTER));

    gui.putOnTop(new GUIComponent("xp-base", noShadow(new TextComponent("懚")), 27, 104, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("cxp-bar", EMPTY, 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("xp-bar", EMPTY, 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("xp-text", EMPTY, 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("air-bar", EMPTY, 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("money-display", EMPTY, 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("gem-display", EMPTY, 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("level-display", EMPTY, 0, 0, Alignment.CENTER));

    gui.putOnTop(new GUIComponent("attack-bar", EMPTY, 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("rage-bar", EMPTY, 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("block-ind", EMPTY, 0, 0, Alignment.CENTER));

    gui.putOnTop(new GUIComponent("notif-mail", EMPTY, 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("notif-market", EMPTY, 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("notif-vote", EMPTY, 0, 0, Alignment.CENTER));
    gui.putOnTop(new GUIComponent("notif-daily", EMPTY, 0, 0, Alignment.CENTER));

    gui.putOnTop(new GUIComponent("jump-wings", EMPTY, 0, 0, Alignment.LEFT));

    gui.putOnTop(new GUIComponent("rune-display", EMPTY, 0, 0, Alignment.CENTER));

    gui.putOnTop(new GUIComponent("notices", EMPTY, 0, 0, Alignment.CENTER));

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

  public void updateLevelDisplay(StrifeMob playerMob) {
    Player player = (Player) playerMob.getEntity();
    GUI gui = guiMap.get(player);
    String originalLevelString = Integer.toString(player.getLevel());
    TextComponent tc = plugin.getGuiManager().convertToLevelFont(player.getLevel());
    gui.update(new GUIComponent("level-display", tc, originalLevelString.length() * 12, -106,
        Alignment.CENTER));

    // Normal green XP bar
    int xpProgress = (int) Math.floor(24f * player.getExp());
    gui.update(xpBar.get(xpProgress));

    // Purple catchup underlay
    float catchupXpRemaining = (float) plugin.getExperienceManager()
        .getRemainingCatchupXp(playerMob.getChampion().getSaveData().getCatchupExpUsed());
    if (catchupXpRemaining > 0) {
      float nuPercent = catchupXpRemaining / plugin.getExperienceManager().getMaxFaceExp(player.getLevel());
      nuPercent *= 2;
      int catchupProgress = xpProgress + (int) (26f * nuPercent);
      if (catchupProgress >= 24) {
        gui.update(catchupXpBar.get(24));
      } else {
        gui.update(catchupXpBar.get(catchupProgress));
      }
    } else {
      gui.update(catchupXpBar.get(0));
    }

    gui.update(builtXpFont.get((int) Math.floor(1000 * player.getExp())));
  }

  public void updateAir(GUI gui, Player player) {
    if (player.getRemainingAir() == player.getMaximumAir()) {
      gui.update(new GUIComponent("air-bar", EMPTY, 0, 0, Alignment.CENTER));
    } else if (player.getRemainingAir() < 1) {
      gui.update(oxygenBar.get(0));
    } else {
      int progress = (int) Math.ceil(23 * (
          (float) player.getRemainingAir()) / player.getMaximumAir());
      gui.update(oxygenBar.get(progress));
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

  public void updateMailNotif(Player player, boolean enabled) {
    if (!enabled) {
      getGui(player).update(new GUIComponent("notif-mail", EMPTY, 0, 0, Alignment.CENTER));
      return;
    }
    getGui(player).update(new GUIComponent("notif-mail", notifMail, 12, -202, Alignment.CENTER));
  }

  public void updateMarketNotif(Player player, boolean enabled) {
    if (!enabled) {
      getGui(player).update(new GUIComponent("notif-market", EMPTY, 0, 0, Alignment.CENTER));
      return;
    }
    getGui(player).update(new GUIComponent("notif-market", notifMarket, 13, -188, Alignment.CENTER));
  }

  public void updateVoteNotif(Player player, boolean enabled) {
    if (!enabled) {
      getGui(player).update(new GUIComponent("notif-vote", EMPTY, 0, 0, Alignment.CENTER));
      return;
    }
    getGui(player).update(new GUIComponent("notif-vote", notifVote, 12, -174, Alignment.CENTER));
  }

  public void updateDailyNotif(Player player, boolean enabled) {
    if (!enabled) {
      getGui(player).update(new GUIComponent("notif-daily", EMPTY, 0, 0, Alignment.CENTER));
      return;
    }
    getGui(player).update(new GUIComponent("notif-daily", notifDaily, 12, -161, Alignment.CENTER));
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
      HP_BAR.put(i, noShadow(new TextComponent(hpBar.toString())));
      ENERGY_BAR.put(i, noShadow(new TextComponent(eBar.toString())));
      BARRIER_BAR_1.put(i, noShadow(new TextComponent(barrierBar1.toString())));
      BARRIER_BAR_2.put(i, noShadow(new TextComponent(barrierBar2.toString())));
      BARRIER_BAR_3.put(i, noShadow(new TextComponent(barrierBar3.toString())));
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

  public TextComponent convertToLevelFont(int i) {
    if (levelStringNumbers.containsKey(i)) {
      return levelStringNumbers.get(i);
    }
    String s = Integer.toString(i);
    s = s
        .replaceAll("1", "慠\uF801")
        .replaceAll("2", "慡\uF801")
        .replaceAll("3", "慢\uF801")
        .replaceAll("4", "慣\uF801")
        .replaceAll("5", "慤\uF801")
        .replaceAll("6", "慥\uF801")
        .replaceAll("7", "慦\uF801")
        .replaceAll("8", "慧\uF801")
        .replaceAll("9", "慨\uF801")
        .replaceAll("0", "慩\uF801");
    TextComponent tc = noShadow(new TextComponent(s));
    levelStringNumbers.put(i, tc);
    return tc;
  }

  public TextComponent convertToMoneyFont(int i) {
    if (moneyStringNumbers.containsKey(i)) {
      return moneyStringNumbers.get(i);
    }
    String s = Integer.toString(i);
    s = s
        .replaceAll("1", "①")
        .replaceAll("2", "②")
        .replaceAll("3", "③")
        .replaceAll("4", "④")
        .replaceAll("5", "⑤")
        .replaceAll("6", "⑥")
        .replaceAll("7", "⑦")
        .replaceAll("8", "⑧")
        .replaceAll("9", "⑨")
        .replaceAll("0", "⑩");
    TextComponent tc = color(new TextComponent(s), FaceColor.YELLOW.getColor());
    moneyStringNumbers.put(i, tc);
    return tc;
  }

  public TextComponent convertToGemFont(int i) {
    if (gemStringNumbers.containsKey(i)) {
      return gemStringNumbers.get(i);
    }
    String s = Integer.toString(i);
    s = s
        .replaceAll("1", "⓫")
        .replaceAll("2", "⓬")
        .replaceAll("3", "⓭")
        .replaceAll("4", "⓮")
        .replaceAll("5", "⓯")
        .replaceAll("6", "⓰")
        .replaceAll("7", "⓱")
        .replaceAll("8", "⓲")
        .replaceAll("9", "⓳")
        .replaceAll("0", "⓴");
    TextComponent tc = color(new TextComponent(s), FaceColor.PURPLE.getColor());
    gemStringNumbers.put(i, tc);
    return tc;
  }
}
