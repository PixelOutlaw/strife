package land.face.strife.listeners;

import static org.bukkit.attribute.Attribute.GENERIC_ATTACK_SPEED;

import com.tealcube.minecraft.bukkit.facecore.utilities.PaletteUtil;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
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
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onPlayerJoin(final PlayerJoinEvent event) {
    event.getPlayer().setGravity(true);

    StrifeMob playerMob = plugin.getStrifeMobManager().getStatMob(event.getPlayer());
    Champion champion = playerMob.getChampion();
    plugin.getAbilityManager().loadPlayerCooldowns(event.getPlayer());
    plugin.getBoostManager().updateGlobalBoostStatus(event.getPlayer());

    if (champion.getUnusedStatPoints() > 0) {
      notifyUnusedPoints(event.getPlayer(), champion.getUnusedStatPoints());
    }
    if (event.getPlayer().getLevel() / 10 > champion.getSaveData().getPathMap().size()) {
      notifyUnusedPaths(event.getPlayer());
    }

    plugin.getCounterManager().clearCounters(event.getPlayer());
    ensureAbilitiesDontInstantCast(event.getPlayer());

    plugin.getChampionManager().verifyStatValues(champion);
    plugin.getChampionManager().update(event.getPlayer());

    if (champion.getSaveData().isOnMount()) {
      plugin.getPlayerMountManager().spawnMount(event.getPlayer());
    }

    Bukkit.getScheduler().runTaskLater(plugin, () ->
        plugin.getStrifeMobManager().updateCollisions(event.getPlayer()), 20L);

    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      plugin.getPlayerMountManager().updateSelectedMount(event.getPlayer());
      if (mounted.contains(event.getPlayer().getUniqueId())) {
        plugin.getPlayerMountManager().spawnMount(event.getPlayer());
        mounted.remove(event.getPlayer().getUniqueId());
      }
    }, 40L);

    event.getPlayer().getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(200);
    event.getPlayer().getAttribute(GENERIC_ATTACK_SPEED).setBaseValue(1000);
    plugin.getAttackSpeedManager().getAttackMultiplier(playerMob, 1);

    if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
      Bukkit.getScheduler().runTaskLater(plugin,
          () -> plugin.getAbilityIconManager().setAllAbilityIcons(event.getPlayer()), 1L);
      Bukkit.getScheduler().runTaskLater(plugin,
          () -> plugin.getAbilityIconManager().setAllAbilityIcons(event.getPlayer()), 10L);
    }

    plugin.getBossBarManager().createBars(event.getPlayer());
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
      plugin.getChampionManager().pushCloseSkills(champion);
      plugin.getBossBarManager().updateBar(event.getPlayer(), 1, 0,
          plugin.getSkillExperienceManager().updateSkillString(champion), 0);
    }, 33L);

    plugin.getGuiManager().updateLevelDisplay(playerMob);
    //plugin.getGuiManager().updateEquipmentDisplay(event.getPlayer());
  }

  @EventHandler(priority = EventPriority.HIGH)
  public void onPlayerJoinLater(final PlayerJoinEvent event) {
    plugin.getPlayerMountManager().updateAllMountCollisions();
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
    plugin.getCounterManager().clearCounters(player);
    plugin.getBossBarManager().clearBars(player);
  }

  private void notifyUnusedPoints(final Player player, final int unused) {
    Bukkit.getScheduler().runTaskLater(plugin, () ->
        player.sendMessage(UNUSED_POINTS.replace("{0}", String.valueOf(unused))), 20L * 15);
  }

  private void notifyUnusedPaths(final Player player) {
    Bukkit.getScheduler().runTaskLater(plugin, () ->
        player.sendMessage(PATH), 20L * 17);
  }

  private void ensureAbilitiesDontInstantCast(Player player) {
    plugin.getAbilityManager().setGlobalCooldown(player, 30);
    if (player.getInventory().getHeldItemSlot() < 3) {
      player.getInventory().setHeldItemSlot(3);
    }
  }
}
