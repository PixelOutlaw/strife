package land.face.strife.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BorderEffectUtil {

  private static Method handle, sendPacket;
  private static Method center, distance, time, movement;
  private static Field player_connection;
  private static Constructor<?> constructor, border_constructor;
  private static Object constant;

  static {
    try {
      handle = getClass("org.bukkit.craftbukkit", "entity.CraftPlayer").getMethod("getHandle");
      player_connection = getClass("net.minecraft.server", "EntityPlayer")
          .getField("playerConnection");
      for (Method m : getClass("net.minecraft.server", "PlayerConnection").getMethods()) {
        if (m.getName().equals("sendPacket")) {
          sendPacket = m;
          break;
        }
      }
      Class<?> enumclass;
      try {
        enumclass = getClass("net.minecraft.server", "EnumWorldBorderAction");
      } catch (ClassNotFoundException x) {
        enumclass = getClass("net.minecraft.server",
            "PacketPlayOutWorldBorder$EnumWorldBorderAction");
      }
      constructor = getClass("net.minecraft.server", "PacketPlayOutWorldBorder")
          .getConstructor(getClass("net.minecraft.server", "WorldBorder"), enumclass);
      border_constructor = getClass("net.minecraft.server", "WorldBorder").getConstructor();

      String setCenter = "setCenter";
      String setWarningDistance = "setWarningDistance";
      String setWarningTime = "setWarningTime";
      String transitionSizeBetween = "transitionSizeBetween";

      center = getClass("net.minecraft.server", "WorldBorder")
          .getMethod(setCenter, double.class, double.class);
      distance = getClass("net.minecraft.server", "WorldBorder")
          .getMethod(setWarningDistance, int.class);
      time = getClass("net.minecraft.server", "WorldBorder").getMethod(setWarningTime, int.class);
      movement = getClass("net.minecraft.server", "WorldBorder")
          .getMethod(transitionSizeBetween, double.class, double.class, long.class);

      for (Object o : enumclass.getEnumConstants()) {
        if (o.toString().equals("INITIALIZE")) {
          constant = o;
          break;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static Class<?> getClass(String prefix, String name) throws Exception {
    String subString = Bukkit.getServer().getClass().getPackage().getName()
        .substring(Bukkit.getServer().getClass().getPackage().getName().lastIndexOf(".") + 1);
    return Class.forName(prefix + "." + subString + "." + name);
  }

  private static void sendWorldBorderPacket(Player p, int dist, double oldradius, double newradius,
      long delay) {
    try {
      Object wb = border_constructor.newInstance();

      Method worldServer = getClass("org.bukkit.craftbukkit", "CraftWorld").getMethod("getHandle");
      Field world = getClass("net.minecraft.server", "WorldBorder").getField("world");
      world.set(wb, worldServer.invoke(p.getWorld()));

      center.invoke(wb, p.getLocation().getX(), p.getLocation().getY());
      distance.invoke(wb, dist);
      time.invoke(wb, 15);
      movement.invoke(wb, oldradius, newradius, delay);

      Object packet = constructor.newInstance(wb, constant);
      sendPacket.invoke(player_connection.get(handle.invoke(p)), packet);
    } catch (Exception x) {
      x.printStackTrace();
    }
  }

  public static void sendBorder(Player p, double percentage, int fadeTime) {
    percentage = Math.max(0, Math.min(0.95, percentage));
    setBorder(p, percentage);
    fadeBorder(p, percentage, fadeTime);
  }

  private static void fadeBorder(Player p, double percentage, long time) {
    int dist = (int) (2000000 * percentage);
    //Add 4000 to make sure the "security" zone does not count in the fade time
    sendWorldBorderPacket(p, 0, 200000D, dist, time + 4000);
  }

  public static void removeBorder(Player p) {
    sendWorldBorderPacket(p, 0, 200000D, 200000D, 0);
  }

  private static void setBorder(Player p, double percentage) {
    int dist = (int) (2000000 * percentage);
    sendWorldBorderPacket(p, dist, 200000D, 200000D, 0);
  }
}
