package controllers.singlePlayer.mendiola;

import core.game.Observation;

import java.util.ArrayList;

/**
 * Created by julian on 23/10/16.
 */
public class Scenario {

    private ArrayList<Observation>[][] grid;

    public Scenario(ArrayList<Observation>[][] grid){
        this.grid = grid;
    }

    /**
     * Zero means equal
     * @param scenario same size
     * @return
     */
    public float compare(Scenario scenario){
        float result = 0;
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                for(int k = 0; k < grid[i][j].size(); k++){
                    if(scenario.grid[i][j].size() > k){
                        result += Math.abs(grid[i][j].get(k).compareTo(scenario.grid[i][j].get(k)));
                    } else {
                        result += 2;
                    }
                }
            }
        }
        return result;
    }

}
