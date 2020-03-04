package land.face.strife.util;

import land.face.strife.data.EloResponse;

public class EloUtil {

  private static float calcProbability(float rating1, float rating2) {
    return 1.0f / (1.0f + (float) Math.pow(10f, ((rating2 - rating1) / 400f)));
  }

  public static EloResponse getEloChange(float winningPlayerElo, float losingPlayerElo, float K) {

    //System.out.println("startEloWin " + winningPlayerElo);
    //System.out.println("startEloLose " + losingPlayerElo);

    float winProbabilityA = calcProbability(winningPlayerElo, losingPlayerElo);
    float winProbabilityB = 1f - winProbabilityA;

    //System.out.println("winProbA " + winProbabilityA);
    //System.out.println("winProbB " + winProbabilityB);

    winningPlayerElo = winningPlayerElo + K * (1f - winProbabilityA);
    losingPlayerElo = losingPlayerElo + K * (0f - winProbabilityB);

    //System.out.println("newWinELO " + winningPlayerElo);
    //System.out.println("newLoseELO " + losingPlayerElo);

    return new EloResponse(winningPlayerElo, losingPlayerElo);
  }
}
