package land.face.strife.data.effects;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitTask;

public class Riptide extends Effect {

  public static Object RIPTIDE_POSE_ENUM = null;
  public static Object STANDING_POSE_ENUM = null;
  private static final Map<LivingEntity, Integer> RIPTIDE_MAP = new WeakHashMap<>();
  private static BukkitTask TASK = null;

  @Setter
  private int ticks;

  @Override
  public void apply(StrifeMob caster, StrifeMob target) {
    if (RIPTIDE_POSE_ENUM == null || STANDING_POSE_ENUM == null) {
      return;
    }
    RIPTIDE_MAP.put(target.getEntity(), ticks);
    try {
      PacketContainer fakeSpin = ProtocolLibrary.getProtocolManager()
          .createPacket(PacketType.Play.Server.ENTITY_METADATA);

      WrappedDataWatcher w = WrappedDataWatcher.getEntityWatcher(target.getEntity());
      w.setObject(6, RIPTIDE_POSE_ENUM);
      w.setObject(8, (byte) 4);

      fakeSpin.getWatchableCollectionModifier().write(0, w.getWatchableObjects());

      ProtocolLibrary.getProtocolManager().broadcastServerPacket(fakeSpin);
    } catch (Exception e) {
      sendCancelPacket(target.getEntity());
      e.printStackTrace();
    }
  }

  private static void tickRiptide() {
    Iterator<LivingEntity> iterator = RIPTIDE_MAP.keySet().iterator();
    while (iterator.hasNext()) {
      LivingEntity le = iterator.next();
      if (!le.isValid()) {
        Bukkit.getLogger().info("CANCELED INVALID");
        sendCancelPacket(le);
        iterator.remove();
        continue;
      }
      if (le.getVelocity().getY() < 0.1 && le.isOnGround()) {
        Bukkit.getLogger().info("CANCELED GROUND");
        iterator.remove();
        continue;
      }
      if (RIPTIDE_MAP.get(le) < 1) {
        Bukkit.getLogger().info("CANCELED TIMEOUT");
        sendCancelPacket(le);
        iterator.remove();
        continue;
      }
      RIPTIDE_MAP.put(le, RIPTIDE_MAP.get(le) - 1);
    }
  }

  public static boolean isRiptideAnimationPlaying(LivingEntity target) {
    return RIPTIDE_MAP.containsKey(target);
  }

  public static void sendCancelPacket(LivingEntity target) {
    RIPTIDE_MAP.remove(target);
    try {
      PacketContainer restoreStanding = ProtocolLibrary.getProtocolManager()
          .createPacket(PacketType.Play.Server.ENTITY_METADATA);

      WrappedDataWatcher w = WrappedDataWatcher.getEntityWatcher(target);
      w.setObject(6, STANDING_POSE_ENUM);
      w.setObject(8, (byte) 0);

      restoreStanding.getWatchableCollectionModifier().write(0, w.getWatchableObjects());

      ProtocolLibrary.getProtocolManager().broadcastServerPacket(restoreStanding);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void startTask(StrifePlugin plugin) {
    if (TASK != null) {
      TASK.cancel();
    }
    if (TASK == null || TASK.isCancelled()) {
      TASK = Bukkit.getScheduler().runTaskTimer(plugin, Riptide::tickRiptide, 200L, 2L);
    }
  }

  public static void buildNMSEnum(StrifePlugin plugin) {
    try {
      RIPTIDE_POSE_ENUM = StrifePlugin.getPoseClass().getField("e").get(null);
      STANDING_POSE_ENUM = StrifePlugin.getPoseClass().getField("a").get(null);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}