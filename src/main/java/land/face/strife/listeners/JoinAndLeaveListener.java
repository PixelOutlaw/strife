package land.face.strife.listeners;

import com.tealcube.minecraft.bukkit.facecore.utilities.FaceColor;
import com.tealcube.minecraft.bukkit.facecore.utilities.ItemUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import com.tealcube.minecraft.bukkit.facecore.utilities.ToastUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.UnicodeUtil;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import land.face.announce.ModernAnnouncerPlugin;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.data.champion.Champion;
import land.face.strife.stats.AbilitySlot;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinAndLeaveListener implements Listener {

  private final StrifePlugin plugin;

  private final static String UNUSED_POINTS = PaletteUtil.color(
      """
          |white|
          |dorange|+-- |orange||b|You Leveled Up! |dorange|--+
          |yellow| You have |white|{0} |yellow|attribute points to spend!
          |yellow| Use |white|/levelup |yellow|to spend them!
          |white|"""
  );
  private final static String PATH = PaletteUtil.color(
      """
          |white|
          |pink|+-- |white||b|Choose A Path! |pink|--+
          |white| You have a a path that needs to be chosen!
          |white| Use |pink|/levelup |white|to make your choice!
          |white|"""
  );

  private final Set<UUID> mounted = new HashSet<>();

  public JoinAndLeaveListener(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.LOW)
  public void onPlayerJoinLow(final PlayerJoinEvent event) {
    plugin.getGuiManager().setupGui(event.getPlayer());
    plugin.getTopBarManager().setupPlayer(event.getPlayer());
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onPlayerJoin(final PlayerJoinEvent event) {
    event.getPlayer().setGravity(true);

    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(event.getPlayer());
    mob.restartTimers();

    Champion champion = mob.getChampion();
    plugin.getAbilityManager().loadPlayerCooldowns(event.getPlayer());
    plugin.getBoostManager().updateGlobalBoostStatus(event.getPlayer());

    if (champion.getUnusedStatPoints() > 0) {
      Bukkit.getScheduler().runTaskLater(plugin, () ->
          plugin.getExperienceManager().notifyUnusedPoints(event.getPlayer()), 20L * 30);
    }
    if (event.getPlayer().getLevel() / 10 > champion.getSaveData().getPathMap().size()) {
      notifyUnusedPaths(event.getPlayer());
    }

    plugin.getCounterManager().clearCounters(event.getPlayer());
    ensureAbilitiesDontInstantCast(event.getPlayer());

    plugin.getChampionManager().verifyStatValues(champion);

    if (champion.getSaveData().isOnMount()) {
      plugin.getPlayerMountManager().spawnMount(event.getPlayer());
    }

    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      plugin.getStrifeMobManager().updateCollisions(event.getPlayer());
      plugin.getPrayerManager().sendPrayerUpdate(event.getPlayer(),
          mob.getPrayer() / mob.getMaxPrayer(), false);
    }, 15L);

    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      if (mounted.contains(event.getPlayer().getUniqueId())) {
        plugin.getPlayerMountManager().spawnMount(event.getPlayer());
        mounted.remove(event.getPlayer().getUniqueId());
      }
    }, 20L);

    event.getPlayer().getAttribute(Attribute.ATTACK_DAMAGE).setBaseValue(200);
    event.getPlayer().getAttribute(Attribute.ATTACK_SPEED).setBaseValue(1000);

    champion.recombineCache(plugin);
    plugin.getStatUpdateManager().updateAllAttributes(event.getPlayer());
    plugin.getAttackSpeedManager().getAttackMultiplier(mob, 1);

    if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
      Bukkit.getScheduler().runTaskLater(plugin,
          () -> plugin.getAbilityIconManager().setAllAbilityIcons(event.getPlayer()), 1L);
      Bukkit.getScheduler().runTaskLater(plugin,
          () -> plugin.getAbilityIconManager().setAllAbilityIcons(event.getPlayer()), 10L);
    }

    plugin.getBossBarManager().createBars(event.getPlayer());
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      plugin.getChampionManager().pushCloseSkills(champion);
      plugin.getTopBarManager().updateSkills(event.getPlayer(),
          plugin.getSkillExperienceManager().updateSkillString(champion));
      }, 33L);

    plugin.getGuiManager().updateLevelDisplay(mob);
    //plugin.getGuiManager().updateEquipmentDisplay(event.getPlayer());
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onPlayerJoinLater(final PlayerJoinEvent event) {
    plugin.getPlayerMountManager().updateAllMountCollisions();
    plugin.getContactObjectManager().loginSync(event.getPlayer());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerQuit(final PlayerQuitEvent event) {
    doPlayerLeave(event.getPlayer());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerKick(final PlayerKickEvent event) {
    doPlayerLeave(event.getPlayer());
  }

  private void doPlayerLeave(Player player) {
    if (plugin.getPlayerMountManager().isMounted(player)) {
      mounted.add(player.getUniqueId());
    }
    plugin.getAttackSpeedManager().wipeAttackRecord(player);
    plugin.getPlayerMountManager().despawn(player);
    plugin.getAbilityManager().unToggleAll(player);
    plugin.getStrifeMobManager().saveEnergy(player);
    plugin.getStrifeMobManager().despawnMinions(player);
    plugin.getBoostManager().removeBooster(player.getUniqueId());
    plugin.getAbilityManager().savePlayerCooldowns(player);
    plugin.getAbilityIconManager().removeIconItem(player, AbilitySlot.SLOT_A);
    plugin.getAbilityIconManager().removeIconItem(player, AbilitySlot.SLOT_B);
    plugin.getAbilityIconManager().removeIconItem(player, AbilitySlot.SLOT_C);
    plugin.getAbilityIconManager().removeIconItem(player, AbilitySlot.SLOT_D);
    plugin.getCounterManager().clearCounters(player);
    plugin.getBossBarManager().clearBars(player);
  }

  private void notifyUnusedPaths(final Player player) {
    Bukkit.getScheduler().runTaskLater(plugin, () ->
        player.sendMessage(PATH), 20L * 35);
  }

  private void ensureAbilitiesDontInstantCast(Player player) {
    plugin.getAbilityManager().setGlobalCooldown(player, 30);
    if (player.getInventory().getHeldItemSlot() < 3) {
      player.getInventory().setHeldItemSlot(3);
    }
  }
}
