package controllers.singlePlayer.mendiola;

import controllers.singlePlayer.mendiola.helpers.Consts;
import controllers.singlePlayer.mendiola.helpers.SHash;
import controllers.singlePlayer.mendiola.helpers.CompareResult;
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

    private Scenario(int[][] board, Vector2d playerPos, String hash) {
        this.board = board;
        this.playerPos = new Vector2d(playerPos.x, playerPos.y);
        this.hash = new SHash();
        this.hash.hash = hash;
    }

    private void mapGrid(ArrayList<Observation>[][] grid, Vector2d fromCenter, int boxSize){
        int high = (boxSize > 0) ? boxSize : grid.length;
        int width = (boxSize > 0) ? boxSize : grid[0].length;
        int top, bottom, startX, startY,fixX, fixY;
        int deltaX = (int)fromCenter.x -  boxSize / 2;
        int deltaY = (int)fromCenter.y -  boxSize / 2;
        fixX = Math.min(deltaX, 0);
        fixY = Math.min(deltaY, 0);
        startX = Math.max(0, deltaX);
        startY = Math.max(0, deltaY);
        top = Math.min(high + fixX, grid.length);
        bottom = Math.min(width + fixY, grid[0].length);

        // 5 = IGNORES
        board = new int[][] {{5,5,5,5,5},{5,5,5,5,5},{5,5,5,5,5},{5,5,5,5,5},{5,5,5,5,5}};
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
                board[j-fixY][i-fixX] = value;
            }
        }
        this.hash.hash = buildHash(board);
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("[[");
        for(int i = 0; i < this.hash.hash.length(); i ++){
            int value = Character.getNumericValue(this.hash.hash.charAt(i));
            if(value == Consts.OBS_ITYPE_DN) {
                sb.append("_");
            } else if(value == Consts.OBS_IGNORE) {
               sb.append("?");
            } else if(value == Consts.OBS_ITYPE_WALL) {
               sb.append("+");
            } else if(value == Consts.OBS_ITYPE_HOLE) {
               sb.append("0");
            } else if(value == Consts.OBS_ITYPE_BOX) {
               sb.append("#");
            }
            if(i+1 < this.hash.hash.length())
                sb.append(((i + 1) % this.board.length == 0) ? "],[" : ",");
        }
       sb.append("]]");
        return sb.toString();
    }

    public CompareResult compare(Scenario scenario, Double threshold){
        double playerDist = this.playerPos.dist(scenario.playerPos);
        CompareResult result = new CompareResult(0, playerDist);
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
                if( (val1 != Consts.OBS_IGNORE) && (val2 != Consts.OBS_IGNORE))
                    acumm += val1 != val2 ? 1 : 0;
                if(threshold != null && threshold < acumm) {
                    result.scenarioDist = acumm;
                    return result;
                }
            }
        }
        result.scenarioDist = acumm;
        return result;
    }

    public boolean isExact(Scenario scenario) {
        return this.hash.equals(scenario.hash);
    }

    public int getHigh(){
        return this.board.length;
    }

    public int getWidht(){
        return this.board[0].length;
    }

    private String buildHash(int[][] board){
        String result = "";
        for(int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[0].length; j++) {
                result += String.valueOf(board[i][j]);
            }
        }
        return result;
    }

    private static int[] emptyOf(int size){
        int[] result = new int[size];
        for(int i = 0; i < size; i++) {
            result[i] = Consts.OBS_IGNORE;
        }
        return result;
    }

    public static Scenario mutate(Scenario scenario1, Scenario scenario2){
        int high = Math.max(scenario1.getHigh(), scenario2.getHigh());
        int width = Math.max(scenario1.getWidht(), scenario2.getWidht());
        int[][] grid = new int[width][high];
        String hash = "";
        for(int i = 0; i < high; i++) {
            int[] row1 = (scenario1.board.length > i) ? scenario1.board[i] : emptyOf(width);
            int[] row2 = (scenario2.board.length > i) ? scenario2.board[i] : emptyOf(width);
            for(int j = 0; j < width; j++) {
                int value1 = (row1.length > j) ? row1[j] : Consts.OBS_IGNORE;
                int value2 = (row2.length > j) ? row2[j] : Consts.OBS_IGNORE;
                int fillWith = (value1 == value2) ? value1 : Consts.OBS_IGNORE;
                grid[i][j] = fillWith;
                hash += String.valueOf(fillWith);
            }
        }
        Scenario result = new Scenario(grid, scenario1.playerPos, hash);
        return result;
    }
}
