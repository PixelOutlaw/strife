package land.face.strife.listeners;

import io.pixeloutlaw.minecraft.spigot.garbage.StringExtensionsKt;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.stats.StrifeStat;
import org.bukkit.Bukkit;
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
    bobberVelocity.multiply(0.65f * (1 + mob.getStat(StrifeStat.PROJECTILE_SPEED) / 100));
    event.getHook().setVelocity(bobberVelocity);

    float fishMult = (float) Math.pow(0.5, mob.getStat(StrifeStat.FISHING_SPEED) / 100);

    int minFishTime = (int) (150D * fishMult);
    int maxFishTime = minFishTime + (int) (150D * Math.random() * fishMult);

    //Bukkit.getLogger().info("aaa" + minFishTime + " " + maxFishTime);

    //Bukkit.getLogger().info("beoreMax" + event.getHook().getMaxWaitTime());
    event.getHook().setMaxWaitTime(maxFishTime);
    //Bukkit.getLogger().info("afterMax" + event.getHook().getMaxWaitTime());
    //Bukkit.getLogger().info("beforeMin" + event.getHook().getMinWaitTime());
    event.getHook().setMinWaitTime(minFishTime);
    //Bukkit.getLogger().info("afterMin" + event.getHook().getMinWaitTime());
  }
}
