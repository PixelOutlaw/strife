package land.face.strife.util;

import land.face.strife.StrifePlugin;

public class LogUtil {

  public static void printError(String message) {
    StrifePlugin.getInstance().getLogger().severe(message);
  }

  public static void printWarning(String message) {
    if (StrifePlugin.getInstance().getLogLevel().ordinal() > 2) {
      return;
    }
    StrifePlugin.getInstance().getLogger().warning(message);
  }

  public static void printDebug(String message) {
    if (StrifePlugin.getInstance().getLogLevel().ordinal() > 1) {
      return;
    }
    StrifePlugin.getInstance().getLogger().info(message);
  }

  public static void printInfo(String message) {
    StrifePlugin.getInstance().getLogger().info(message);
  }

  public enum LogLevel {
    INFO,
    DEBUG,
    WARNING,
    ERROR
  }
}
