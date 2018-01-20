package info.faceland.strife.util;

import info.faceland.strife.attributes.StrifeAttribute;
import info.faceland.strife.data.Champion;

public class StatUtil {

  public static double GetStat(Champion champ, StrifeAttribute stat) {
    return champ.getCache().getAttribute(stat);
  }
}
