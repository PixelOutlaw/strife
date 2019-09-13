package info.faceland.strife.data.effects;

import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import info.faceland.strife.StrifePlugin;
import info.faceland.strife.data.StrifeMob;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class Lightning extends Effect {

  private static ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
  private static int thunderMaxDistance;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    target.getEntity().getWorld().strikeLightningEffect(target.getEntity().getLocation());
  }

  public static void setupLightningPacketListener() {
    thunderMaxDistance = Bukkit.getViewDistance() * 16 - 2;
    protocolManager.addPacketListener(
        new PacketAdapter(StrifePlugin.getInstance(), Server.NAMED_SOUND_EFFECT) {
          public void onPacketSending(PacketEvent event) {
            if (event.getPacketType() == Server.NAMED_SOUND_EFFECT) {
              PacketContainer packet = event.getPacket();
              Player p = event.getPlayer();
              Sound sound = packet.getSoundEffects().read(0);
              Sound sound2 = Sound.ENTITY_LIGHTNING_BOLT_THUNDER;
              if (sound.compareTo(sound2) == 1) {
                int x = packet.getIntegers().read(0) / 8;
                int z = packet.getIntegers().read(2) / 8;
                int distance = distanceBetweenPoints(x, p.getLocation().getBlockX(), z,
                    p.getLocation().getBlockZ());
                if (distance > thunderMaxDistance) {
                  event.setCancelled(true);
                }
              }
            }
          }
        });
  }

  public static int distanceBetweenPoints(int x1, int x2, int y1, int y2) {
    return (int) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
  }
}