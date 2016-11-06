package controllers.singlePlayer.mendiola;

import core.game.Observation;
import tools.Vector2d;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by julian on 30/10/16.
 */
public class Printer {

    public static void printKnowledge(Knowledge knowledge, boolean withScenarios, boolean includeUseless) {
        System.out.println("/******** KNOWLEDGE START ********/");
        for (Theory t : knowledge.theories) {
            if(includeUseless && t.getUtility() == 0d)
                continue;
            printTheory(t, withScenarios);
        }
        System.out.println("/******** KNOWLEDGE END ********/");
    }

    public static void printKnowledgeByPlayerPos(Knowledge knowledge, Vector2d playerPos) {
        System.out.println("/******** KNOWLEDGE START ********/");
        List<Theory> toPrint = new ArrayList<Theory>();
        for (Theory t : knowledge.theories) {
            Vector2d tPlayerPos = t.getScenario().playerPos;
            if(tPlayerPos.dist(playerPos) == 0)
                toPrint.add(t);
        }
        for (Theory t : toPrint) {
            printTheory(t, true);
        }
        System.out.println("/******** KNOWLEDGE END ********/");
    }

    public static void printTheory(Theory theory, boolean withScenario){
        System.out.println(theory);
        if(withScenario) {
            printGrid(theory.getScenario(), theory.prediction);
        }
    }

    private static void printRow(int j,int[] grid, Vector2d playerPos){
        System.out.print("[");
        for (int i = 0; i < grid.length; i++) {
            if(playerPos.dist(i,j) == 0.0)
                System.out.print("X,");
            else
                System.out.print(toPrintObs(grid[i]) + ",");
        }
    }

    public static void printGrid(Scenario grid, Scenario grid2)
    {
        int size = grid.board[0].length;

        for (int j = 0; j < size; j++) {
            printRow(j, grid.board[j], grid.playerPos);
            if(grid2 != null) {
                System.out.print("]  ====>  ");
                printRow(j, grid.board[j], grid2.playerPos);
            }
            System.out.println("]");
        }
        System.out.println("----------------------------");
    }

    public static String toPrintObs(int value) {
        String result = "";
        switch(value){
            case Consts.OBS_ITYPE_DN: //is always
                result = " ";
                break;
            case Consts.OBS_ITYPE_WALL: //is wall
                result += '+';
                break;
            case Consts.OBS_ITYPE_PLAYER: //is player
                result += 'X';
                break;
            case Consts.OBS_ITYPE_HOLE: //is hole
                result += 'O';
                break;
            case Consts.OBS_ITYPE_BOX: //is box
                result += '#';
                break;
            default:
                result += ' ';
                break;
        }
        return result;
    }
}
