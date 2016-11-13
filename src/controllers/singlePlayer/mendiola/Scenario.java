package controllers.singlePlayer.mendiola;

import controllers.singlePlayer.mendiola.helpers.Consts;
import controllers.singlePlayer.mendiola.helpers.SHash;
import controllers.singlePlayer.mendiola.helpers.Utility;
import core.game.Observation;
import tools.Vector2d;

import java.util.ArrayList;

/**
 * Created by julian on 23/10/16.
 */
public class Scenario {

    public int[][] board;

    public SHash hash = new SHash();

    public Vector2d playerPos;

    public Scenario(ArrayList<Observation>[][] grid, Vector2d playerPos){
        mapGrid(grid, playerPos, 5);
    }

    private void mapGrid(ArrayList<Observation>[][] grid, Vector2d fromCenter, int boxSize){
        int high = (boxSize > 0) ? boxSize : grid.length;
        int width = (boxSize > 0) ? boxSize : grid[0].length;
        int top, bottom, startX, startY;
        startX = Math.max(0, (int)fromCenter.x -  boxSize / 2);
        startY = Math.max(0, (int)fromCenter.y -  boxSize / 2);
        top = Math.min(startX + high, grid.length);
        bottom = Math.min(startY + width, grid[0].length);

        String hash = "";
        board = new int[width][high];
        for (int i = startX; i < top; i++) {
            for (int j = startY; j < bottom; j++) {
                int value = Consts.OBS_ITYPE_DN;
                for(int k = 0; k < grid[i][j].size(); k++){
                    Observation obs = grid[i][j].get(k);
                    switch(obs.itype){
                        case Consts.OBS_ITYPE_DN: //is always
                            break;
                        case Consts.OBS_ITYPE_PLAYER:
                            playerPos = new Vector2d(obs.position.x / 40, obs.position.y / 40);
                        case Consts.OBS_ITYPE_WALL: //is wall
                        case Consts.OBS_ITYPE_HOLE: //is hole
                        case Consts.OBS_ITYPE_BOX: //is box
                            value = obs.itype;
                            break;
                        default:
                            break;
                    }
                }
                board[j-startY][i-startX] = value;
                //if(i > 0 && i < high - 1 && j > 0 && j < width -1 ) {
                //    if(j == width - 1)
                //        hash += ',';
                    hash += String.valueOf(value);
                //}
            }
        }
        this.hash.hash = hash;
    }

    public String toString(){
        String result = this.hash.hash.replaceAll(String.valueOf(Consts.OBS_ITYPE_DN), "_,");
        result = result.replaceAll(String.valueOf(Consts.OBS_ITYPE_WALL), "+,");
        result = result.replaceAll(String.valueOf(Consts.OBS_ITYPE_HOLE), "O,");
        result = result.replaceAll(String.valueOf(Consts.OBS_ITYPE_BOX), "#,");
        return result;
    }

    public Utility compare(Scenario scenario, Double threshold){
        double playerDist = this.playerPos.dist(scenario.playerPos);
        Utility result = new Utility(0, playerDist);
        if(this.hash.equals(scenario.hash))
            return result;
        double acumm = 0;
        for (int i = 0; i < this.board.length; i++) {
            int[] row = this.board[i];
            int[] row2 = scenario.board[i];
            for (int j = 0; j < row.length; j++) {
                int val1 = row[j];
                int val2 = row2[j];
                // Ignores player position
                val1 = (val1 == Consts.OBS_ITYPE_PLAYER) ? Consts.OBS_ITYPE_DN : val1;
                val2 = (val2 == Consts.OBS_ITYPE_PLAYER) ? Consts.OBS_ITYPE_DN : val2;
                acumm += Math.abs(val1 - val2);
                if(threshold != null && threshold < acumm) {
                    result.boxDist = acumm;
                    return result;
                }
            }
        }
        result.boxDist = acumm;
        return result;
    }

    public boolean isExact(Scenario scenario) {
        return this.hash.equals(scenario.hash);
    }
}
