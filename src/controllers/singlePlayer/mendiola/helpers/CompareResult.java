package controllers.singlePlayer.mendiola.helpers;

/**
 * Created by julian on 12/11/16.
 */
public class CompareResult {
    public double scenarioDist;
    public double playerDist;
    public CompareResult(double scenarioDist, double playerDist) { this.scenarioDist = scenarioDist; this.playerDist = playerDist; }
    public double value() {
        return scenarioDist * 10 + playerDist;
    }

    @Override
    public String toString() { return "s: " + scenarioDist + " | p: " + playerDist ; }
}
