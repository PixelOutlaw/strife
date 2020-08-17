package land.face.strife.util;

import java.lang.reflect.Field;
import net.minecraft.server.v1_16_R1.EntityFishingHook;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.entity.FishHook;

public class FishingUtil {

  static Field fishCatchTime = null;

  public static int getBiteTime(FishHook hook) {
    EntityFishingHook hookCopy = (EntityFishingHook) ((CraftEntity) hook).getHandle();

    if (fishCatchTime == null) {
      try {
        fishCatchTime = EntityFishingHook.class.getDeclaredField("ap");
      } catch (NoSuchFieldException | SecurityException e) {
        e.printStackTrace();
      }
    }

    fishCatchTime.setAccessible(true);
    int amount = 0;

    try {
      amount = fishCatchTime.getInt(hookCopy);
      return amount;
    } catch (IllegalArgumentException | IllegalAccessException e) {
      e.printStackTrace();
    }
    fishCatchTime.setAccessible(false);
    return amount;
  }

  public static void setBiteTime(FishHook hook, int time) {
    EntityFishingHook hookCopy = (EntityFishingHook) ((CraftEntity) hook).getHandle();

    if (fishCatchTime == null) {
      try {
        fishCatchTime = EntityFishingHook.class.getDeclaredField("ap");
      } catch (NoSuchFieldException | SecurityException e) {
        e.printStackTrace();
      }
    }

    fishCatchTime.setAccessible(true);

    try {
      fishCatchTime.setInt(hookCopy, time);
    } catch (IllegalArgumentException | IllegalAccessException e) {
      e.printStackTrace();
    }

    fishCatchTime.setAccessible(false);
  }
}

