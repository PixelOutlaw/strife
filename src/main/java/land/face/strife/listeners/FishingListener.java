package land.face.strife.listeners;

import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import land.face.strife.util.FishingUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;
import org.bukkit.util.Vector;

public class FishingListener implements Listener {

  private final StrifePlugin plugin;

  public FishingListener(StrifePlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onCastFishingRod(PlayerFishEvent event) {
    if (event.getState() != State.FISHING) {
      return;
    }
    StrifeMob mob = plugin.getStrifeMobManager().getStatMob(event.getPlayer());

    event.getHook().setCustomName(StringExtensionsKt.chatColorize("&b&l<><"));
    event.getHook().setCustomNameVisible(true);

    Vector bobberVelocity = event.getHook().getVelocity().clone();
    bobberVelocity.multiply(0.7f * (1 + mob.getStat(StrifeStat.PROJECTILE_SPEED) / 100));
    event.getHook().setVelocity(bobberVelocity);

    float fishSpeed = mob.getStat(StrifeStat.FISHING_SPEED);
    float fishTime = 150 + (float) Math.random() * 150;
    float fishMult = 100 / (100 + fishSpeed);
    FishingUtil.setBiteTime(event.getHook(), (int) Math.max(fishTime * fishMult, 1));
  }
}
