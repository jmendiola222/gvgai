package controllers.singlePlayer.mendiola.helpers;

/**
 * Created by julian on 12/11/16.
 */
public class Utility {
    public double boxDist;
    public double playerDist;
    public Utility(double boxDist, double playerDist) { this.boxDist = boxDist; this.playerDist = playerDist; }
    public double value() { return boxDist * 10 + playerDist; }
}
