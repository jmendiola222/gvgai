package controllers.singlePlayer.mendiola;

import core.game.Observation;
import tools.Vector2d;

import java.util.ArrayList;

/**
 * Created by julian on 23/10/16.
 */
public class Scenario {

    public ArrayList<Observation>[][] grid;

    public int[][] board;

    public String hash = "";

    public Vector2d playerPos;

    public Scenario(ArrayList<Observation>[][] grid){
        this.grid = grid;
        mapGrid(grid);
    }

    private void mapGrid(ArrayList<Observation>[][] grid){
        board = new int[grid.length][grid[0].length];
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                int value = Consts.OBS_ITYPE_DN;
                for(int k = 0; k < grid[i][j].size(); k++){
                    Observation obs = grid[i][j].get(k);
                    switch(obs.itype){
                        case Consts.OBS_ITYPE_DN: //is always
                            break;
                        case Consts.OBS_ITYPE_PLAYER:
                            playerPos = new Vector2d(obs.position.x / 40, obs.position.y / 40);
                            break;
                        case Consts.OBS_ITYPE_WALL: //is wall
                        case Consts.OBS_ITYPE_HOLE: //is hole
                        case Consts.OBS_ITYPE_BOX: //is box
                            value = obs.itype;
                            break;
                        default:
                            break;
                    }
                }
                board[j][i] = value;
                hash += String.valueOf(value);
            }
        }
    }

    public float compare(Scenario scenario){
        int playerDist = (int) this.playerPos.dist(scenario.playerPos);
        if(this.hash.equals(scenario.hash))
            return playerDist;
        float result = 0;
        for (int i = 0; i < this.board.length; i++) {
            int[] row = this.board[i];
            int[] row2 = scenario.board[i];
            for (int j = 0; j < row.length; j++) {
                int val1 = row[j];
                int val2 = row2[j];
                result += Math.abs(val1 - val2);
            }
        }
        return playerDist + result;
    }

}
