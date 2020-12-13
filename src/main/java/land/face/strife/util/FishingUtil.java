package land.face.strife.util;

import land.face.strife.reflection.BobberReflectionHandler;
import org.bukkit.entity.FishHook;

public class FishingUtil {

  public static void setBiteTime(FishHook hook, int time) {
    BobberReflectionHandler.setBiteTime(hook, time);
  }
}

