package land.face.strife.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.bukkit.Bukkit;
import org.bukkit.entity.FishHook;

public class BobberReflectionHandler {

  private static final Class<?> NMS_FISH_HOOK = getFishHookClass();
  private static final Field CATCH_TIME_FIELD = getCatchTimeField();
  private static final Method NMS_GET_HANDLE = getHandleMethod();
  private static final Method NMS_GET_HANDLE2 = getHandleMethod2();

  public static void setBiteTime(FishHook fishHook, int value) {
    Object nmsHook;
    try {
      nmsHook = NMS_GET_HANDLE.invoke(fishHook);
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }
    if (nmsHook.getClass() != NMS_FISH_HOOK) {
      Bukkit.getLogger().warning("Invalid usage! Supplied object is not a EntityFishHook class!");
      return;
    }
    if (NMS_FISH_HOOK == null) {
      Bukkit.getLogger().warning("Reflection for bobbers failed! Null hook class or fish time field!");
      return;
    }
    if (CATCH_TIME_FIELD == null) {
      Bukkit.getLogger().warning("Reflection for bobbers failed! Null fish time field!");
      return;
    }
    try {
      CATCH_TIME_FIELD.setAccessible(true);
      CATCH_TIME_FIELD.set(fishHook, value);
    } catch (Exception e) {
      e.printStackTrace();
    }
    CATCH_TIME_FIELD.setAccessible(false);
  }

  private static Class<?> getFishHookClass() {
    try {
      return Class.forName(
          "net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3]
              + ".EntityFishingHook");
    } catch (ClassNotFoundException e) {
      Bukkit.getLogger().warning("Failed to get CraftEntity class. EX:" + e.getMessage());
      e.printStackTrace();
      return null;
    }
  }

  private static Class<?> getCraftEntity() {
    try {
      return Class.forName(
          "org.bukkit.craftbukkit." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3]
              + ".entity.CraftEntity");
    } catch (ClassNotFoundException e) {
      Bukkit.getLogger().warning("Failed to get CraftEntity class. EX:" + e.getMessage());
      e.printStackTrace();
      return null;
    }
  }

  private static Field getCatchTimeField() {
    try {
      for (Field f : NMS_FISH_HOOK.getDeclaredFields()) {
        Bukkit.getLogger().warning("f: " + f.toGenericString());
      }
      return NMS_FISH_HOOK.getDeclaredField("ai");
    } catch (Exception e) {
      Bukkit.getLogger().warning("Failed to find catch time field. EX:" + e.getMessage());
      e.printStackTrace();
      return null;
    }
  }

  private static Method getHandleMethod() {
    try {
      return Class.forName(
          "org.bukkit.craftbukkit." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3]
              + ".entity.CraftEntity").getMethod("getHandle");
    } catch (Exception e) {
      Bukkit.getLogger().warning("Failed to find getHandle method on craftEntity. EX:" + e.getMessage());
      e.printStackTrace();
      return null;
    }
  }

  private static Method getHandleMethod2() {
    try {
      return Class.forName(
          "org.bukkit.craftbukkit." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3]
              + ".entity.CraftFishHook").getMethod("getHandle");
    } catch (Exception e) {
      Bukkit.getLogger().warning("Failed to find getHandle method on craftFishHook. EX:" + e.getMessage());
      e.printStackTrace();
      return null;
    }
  }
}